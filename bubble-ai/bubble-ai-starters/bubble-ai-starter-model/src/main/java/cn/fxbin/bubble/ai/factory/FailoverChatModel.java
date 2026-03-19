package cn.fxbin.bubble.ai.factory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.client.ResourceAccessException;
import reactor.core.publisher.Flux;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * ChatModel failover wrapper.
 *
 * <p>When the selected model is unavailable, this wrapper retries the request
 * against other members in the same logical model group.</p>
 *
 * @author fxbin
 */
@Slf4j
public class FailoverChatModel implements ChatModel {

    private static final long DEFAULT_COOLDOWN_MILLIS = 30_000L;

    private static final Map<String, Long> CANDIDATE_COOLDOWNS = new ConcurrentHashMap<>();

    private final String targetId;
    private final List<Candidate> candidates;
    private final long cooldownMillis;
    private final LongSupplier clock;

    public FailoverChatModel(String targetId, List<Candidate> candidates) {
        this(targetId, candidates, DEFAULT_COOLDOWN_MILLIS, System::currentTimeMillis);
    }

    public FailoverChatModel(String targetId, List<Candidate> candidates, long cooldownMillis) {
        this(targetId, candidates, cooldownMillis, System::currentTimeMillis);
    }

    FailoverChatModel(String targetId, List<Candidate> candidates, long cooldownMillis, LongSupplier clock) {
        this.targetId = targetId;
        this.candidates = List.copyOf(candidates);
        this.cooldownMillis = cooldownMillis;
        this.clock = clock;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        RuntimeException lastException = null;
        List<Candidate> orderedCandidates = getAttemptOrder();
        for (int i = 0; i < orderedCandidates.size(); i++) {
            Candidate candidate = orderedCandidates.get(i);
            try {
                ChatResponse response = candidate.getModel().call(prompt);
                clearCooldown(candidate);
                return response;
            } catch (RuntimeException ex) {
                lastException = ex;
                if (isRetryable(ex)) {
                    markCooldown(candidate);
                }
                if (!isRetryable(ex) || i == orderedCandidates.size() - 1) {
                    throw ex;
                }
                log.warn("ChatModel [{}] failed on candidate [{}], fallback to next candidate. reason={}",
                        targetId, candidate.getId(), ex.getMessage());
            }
        }
        throw lastException != null ? lastException : new IllegalStateException("No available candidates for target: " + targetId);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return streamWithFailover(prompt, 0);
    }

    private Flux<ChatResponse> streamWithFailover(Prompt prompt, int index) {
        List<Candidate> orderedCandidates = getAttemptOrder();
        Candidate candidate = orderedCandidates.get(index);
        AtomicBoolean emitted = new AtomicBoolean(false);
        return candidate.getModel().stream(prompt)
                .doOnNext(ignored -> emitted.set(true))
                .doOnComplete(() -> clearCooldown(candidate))
                .onErrorResume(ex -> {
                    if (!(ex instanceof RuntimeException runtimeException)) {
                        return Flux.error(ex);
                    }
                    if (isRetryable(runtimeException)) {
                        markCooldown(candidate);
                    }
                    if (emitted.get() || !isRetryable(runtimeException) || index >= orderedCandidates.size() - 1) {
                        return Flux.error(ex);
                    }
                    log.warn("ChatModel stream [{}] failed on candidate [{}], fallback to next candidate. reason={}",
                            targetId, candidate.getId(), ex.getMessage());
                    return streamWithFailover(prompt, index + 1);
                });
    }

    private List<Candidate> getAttemptOrder() {
        long now = clock.getAsLong();
        List<Candidate> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator
                .comparing((Candidate candidate) -> isCoolingDown(candidate, now))
                .thenComparing(candidate -> cooldownUntil(candidate, now)));
        return ordered;
    }

    private boolean isCoolingDown(Candidate candidate, long now) {
        return cooldownUntil(candidate, now) > now;
    }

    private long cooldownUntil(Candidate candidate, long now) {
        Long until = CANDIDATE_COOLDOWNS.get(cooldownKey(candidate));
        if (until == null) {
            return 0L;
        }
        if (until <= now) {
            CANDIDATE_COOLDOWNS.remove(cooldownKey(candidate), until);
            return 0L;
        }
        return until;
    }

    private void markCooldown(Candidate candidate) {
        long until = clock.getAsLong() + cooldownMillis;
        CANDIDATE_COOLDOWNS.put(cooldownKey(candidate), until);
    }

    private void clearCooldown(Candidate candidate) {
        CANDIDATE_COOLDOWNS.remove(cooldownKey(candidate));
    }

    private String cooldownKey(Candidate candidate) {
        return targetId + "::" + candidate.getId();
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof UnknownHostException
                    || current instanceof SocketException
                    || current instanceof ResourceAccessException) {
                return true;
            }

            Integer statusCode = extractStatusCode(current);
            if (statusCode != null) {
                if (statusCode == 429 || statusCode >= 500) {
                    return true;
                }
                if (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 404) {
                    return false;
                }
            }

            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("timeout")
                        || normalized.contains("timed out")
                        || normalized.contains("connection reset")
                        || normalized.contains("connection refused")
                        || normalized.contains("service unavailable")
                        || normalized.contains("too many requests")) {
                    return true;
                }
                if (normalized.contains("invalid api key")
                        || normalized.contains("unauthorized")
                        || normalized.contains("forbidden")
                        || normalized.contains("authentication failed")) {
                    return false;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private Integer extractStatusCode(Throwable throwable) {
        try {
            Object status = throwable.getClass().getMethod("getStatusCode").invoke(throwable);
            if (status instanceof Number number) {
                return number.intValue();
            }
            Object value = status.getClass().getMethod("value").invoke(status);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {
            // Not an HTTP status carrying exception.
        }
        return null;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Candidate {

        private final String id;

        private final ChatModel model;
    }
}
