package cn.fxbin.bubble.plugin.satoken.task;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.StpUtil;
import cn.fxbin.bubble.plugin.satoken.context.SaTokenContextForTtl;
import cn.fxbin.bubble.plugin.satoken.context.SaTokenContextForTtlStaff;
import cn.fxbin.bubble.plugin.satoken.model.SaRequestForTtl;
import cn.fxbin.bubble.plugin.satoken.model.SaResponseForTtl;
import cn.fxbin.bubble.plugin.satoken.model.SaStorageForTtl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

/**
 * SaTokenTaskDecorator
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/5 9:52
 */
@Slf4j
public class SaTokenTaskDecorator implements TaskDecorator {

    private static final String CONTEXT_PROPAGATION_ERROR = "Sa-Token上下文传播失败";

    private static final String TOKEN_EXTRACTION_ERROR = "Token提取失败";

    @Override
    public Runnable decorate(Runnable runnable) {
        // 使用更清晰的上下文快照结构
        SaTokenContextSnapshot contextSnapshot = captureCurrentContext();

        return () -> {
            String taskName = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();

            try {
                // 恢复上下文
                restoreContext(contextSnapshot);

                if (log.isDebugEnabled()) {
                    log.debug("Sa-Token上下文已恢复到异步任务线程: {}", taskName);
                }

                // 执行业务逻辑
                runnable.run();

            } catch (Exception e) {
                log.error("异步任务执行失败，线程: {}, 耗时: {}ms",
                        taskName, System.currentTimeMillis() - startTime, e);
                throw e;
            } finally {
                // 清理上下文
                cleanupContext();

                if (log.isDebugEnabled()) {
                    log.debug("Sa-Token上下文已清理，线程: {}, 总耗时: {}ms",
                            taskName, System.currentTimeMillis() - startTime);
                }
            }
        };
    }

    /**
     * 捕获当前线程的Sa-Token上下文快照
     *
     * @return 上下文快照对象
     */
    private SaTokenContextSnapshot captureCurrentContext() {
        SaTokenContextSnapshot snapshot = new SaTokenContextSnapshot();

        try {
            // 尝试获取Web环境上下文
            snapshot.request = SaHolder.getRequest();
            snapshot.response = SaHolder.getResponse();
            snapshot.storage = SaHolder.getStorage();
            snapshot.token = StpUtil.getTokenValue();
            snapshot.isWebContext = true;

            if (log.isTraceEnabled()) {
                log.trace("已捕获Web环境Sa-Token上下文，Token: {}",
                        maskToken(snapshot.token));
            }

        } catch (Exception e) {
            // 非Web环境，创建自定义实现
            log.debug("当前非Web环境，创建自定义Sa-Token上下文: {}", e.getMessage());

            snapshot.request = new SaRequestForTtl();
            snapshot.response = new SaResponseForTtl();
            snapshot.storage = new SaStorageForTtl();
            snapshot.isWebContext = false;

            // 尝试从其他途径获取Token
            try {
                snapshot.token = StpUtil.getTokenValue();
            } catch (Exception tokenException) {
                log.warn("{}，将在异步任务中使用空Token: {}",
                        TOKEN_EXTRACTION_ERROR, tokenException.getMessage());
                snapshot.token = null;
            }
        }

        return snapshot;
    }

    /**
     * 在异步线程中恢复Sa-Token上下文
     *
     * @param snapshot 上下文快照
     */
    private void restoreContext(SaTokenContextSnapshot snapshot) {
        try {
            // 设置Sa-Token上下文
            SaTokenContext context = new SaTokenContextForTtl();
            SaManager.setSaTokenContext(context);

            // 绑定请求响应存储对象
            SaTokenContextForTtlStaff.setModelBox(
                    snapshot.request, snapshot.response, snapshot.storage);

            // 恢复Token
            if (snapshot.token != null) {
                StpUtil.setTokenValue(snapshot.token);
            }

        } catch (Exception e) {
            log.error("{}: {}", CONTEXT_PROPAGATION_ERROR, e.getMessage(), e);
            throw new RuntimeException(CONTEXT_PROPAGATION_ERROR, e);
        }
    }

    /**
     * 清理当前线程的Sa-Token上下文
     */
    private void cleanupContext() {
        try {
            SaTokenContextForTtlStaff.clearModelBox();
            // 恢复默认上下文（如果需要）
            // SaManager.setSaTokenContext(defaultContext);
        } catch (Exception e) {
            log.warn("Sa-Token上下文清理时发生异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 脱敏Token用于日志输出
     *
     * @param token 原始Token
     * @return 脱敏后的Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    /**
     * Sa-Token上下文快照内部类
     */
    private static class SaTokenContextSnapshot {
        SaRequest request;
        SaResponse response;
        SaStorage storage;
        String token;
        boolean isWebContext;
    }
}
