package cn.fxbin.bubble.ai.skills.impl;

import cn.fxbin.bubble.ai.skills.SkillDescriptor;
import cn.fxbin.bubble.ai.autoconfigure.SkillProperties;
import cn.fxbin.bubble.ai.skills.SkillRegistry;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 基于文件的技能注册中心实现。
 * 同时支持从文件系统与 classpath 扫描 SKILL.md。
 */
@Slf4j
@RequiredArgsConstructor
public class FileSkillRegistry implements SkillRegistry {

    private static final String SKILL_MD = "SKILL.md";
    private static final String CLASSPATH_SKILL_PATTERN = "classpath*:skills/*/SKILL.md";

    private final SkillProperties skillProperties;
    /** 技能列表缓存，减少重复 IO 扫描。 */
    private volatile List<SkillDescriptor> cachedSkills;

    @Override
    public List<SkillDescriptor> listAvailableSkills() {
        List<SkillDescriptor> snapshot = cachedSkills;
        if (snapshot != null) {
            return snapshot;
        }
        synchronized (this) {
            if (cachedSkills != null) {
                return cachedSkills;
            }
            // 以稳定 skill id 作为合并主键，避免同名技能互相覆盖。
            Map<Long, SkillDescriptor> merged = new LinkedHashMap<>();
            loadClasspathSkills().forEach(skill -> merged.put(skill.id(), skill));
            loadFilesystemSkills().forEach(skill -> merged.put(skill.id(), skill));
            cachedSkills = merged.values().stream()
                    .sorted(Comparator.comparing(SkillDescriptor::id, Comparator.nullsLast(Long::compareTo))
                            .thenComparing(SkillDescriptor::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
            return cachedSkills;
        }
    }

    @Override
    public Optional<String> readSkillContent(String skillName) {
        if (StrUtil.isBlank(skillName)) {
            return Optional.empty();
        }
        Optional<String> fileContent = readFilesystemSkillContent(skillName);
        if (fileContent.isPresent()) {
            return fileContent;
        }
        return readClasspathSkillContent(skillName);
    }

    @Override
    public Optional<SkillDescriptor> findSkillById(Long skillId) {
        if (skillId == null) {
            return Optional.empty();
        }
        return listAvailableSkills().stream()
                .filter(skill -> skill != null && skillId.equals(skill.id()))
                .findFirst();
    }

    @Override
    public Optional<String> readSkillContentById(Long skillId) {
        return findSkillById(skillId).flatMap(this::readContentByDescriptor);
    }

    @Override
    public String getRegistryType() {
        return "file";
    }

    @Override
    public String getSkillLoadInstructions() {
        StringBuilder builder = new StringBuilder("Prefer skill id with readSkillContentById; fallback to readSkillContent by name.");
        Path basePath = resolveBasePath();
        if (basePath != null) {
            builder.append(" File skills path: ").append(basePath).append('.');
        }
        builder.append(" Classpath skills path: ").append(CLASSPATH_SKILL_PATTERN).append('.');
        return builder.toString();
    }

    @Override
    public void reload() {
        // 主动清空缓存，下次读取重新扫描。
        cachedSkills = null;
    }

    private List<SkillDescriptor> loadFilesystemSkills() {
        Path basePath = resolveBasePath();
        if (basePath == null || !Files.exists(basePath) || !Files.isDirectory(basePath)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(basePath)) {
            List<Path> skillDirs = stream
                    .filter(Files::isDirectory)
                    .filter(dir -> Files.exists(dir.resolve(SKILL_MD)))
                    .toList();

            List<SkillDescriptor> result = new ArrayList<>(skillDirs.size());
            for (Path dir : skillDirs) {
                String name = dir.getFileName().toString();
                String description = readFrontmatterDescription(dir.resolve(SKILL_MD)).orElse("");
                String skillPath = dir.toString();
                result.add(new SkillDescriptor(stableSkillId("file", skillPath, name), name, description, "file", skillPath));
            }
            return result;
        } catch (IOException e) {
            log.error("Read file skills failed: {}", basePath, e);
            return List.of();
        }
    }

    private List<SkillDescriptor> loadClasspathSkills() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(CLASSPATH_SKILL_PATTERN);
            return Arrays.stream(resources)
                    .map(this::toClasspathSkillDescriptor)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException e) {
            log.warn("Read classpath skills failed: {}", CLASSPATH_SKILL_PATTERN, e);
            return List.of();
        }
    }

    private Optional<SkillDescriptor> toClasspathSkillDescriptor(Resource resource) {
        try {
            String path = resource.getURL().toString();
            String name = extractSkillName(path);
            String skillPath = extractSkillPath(path);
            return Optional.of(new SkillDescriptor(stableSkillId("classpath", skillPath, name), name, "", "classpath", skillPath));
        } catch (IOException e) {
            log.warn("Read classpath skill descriptor failed: {}", resource.getDescription(), e);
            return Optional.empty();
        }
    }

    private Optional<String> readFilesystemSkillContent(String skillName) {
        Path basePath = resolveBasePath();
        if (basePath == null) {
            return Optional.empty();
        }
        Path skillPath = basePath.resolve(skillName).normalize();
        if (!skillPath.startsWith(basePath)) {
            log.warn("Skill path traversal rejected: {}", skillPath);
            return Optional.empty();
        }
        Path skillFile = skillPath.resolve(SKILL_MD);
        if (!Files.exists(skillFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(skillFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Read file skill content failed: {}", skillFile, e);
            return Optional.empty();
        }
    }

    private Optional<String> readClasspathSkillContent(String skillName) {
        return listAvailableSkills().stream()
                .filter(skill -> "classpath".equals(skill.source()))
                .filter(skill -> skillName.equals(skill.name()))
                .findFirst()
                .flatMap(this::readContentByDescriptor);
    }

    private Path resolveBasePath() {
        if (StrUtil.isBlank(skillProperties.getFileBasePath())) {
            return null;
        }
        return Paths.get(skillProperties.getFileBasePath()).normalize();
    }

    private Optional<String> readFrontmatterDescription(Path skillFile) {
        try {
            List<String> lines = Files.readAllLines(skillFile, StandardCharsets.UTF_8);
            if (lines.isEmpty() || !"---".equals(lines.get(0).trim())) {
                return Optional.empty();
            }
            List<String> frontmatter = lines.stream().skip(1).takeWhile(line -> !"---".equals(line.trim())).toList();
            return frontmatter.stream()
                    .map(String::trim)
                    .filter(line -> line.startsWith("description:"))
                    .map(line -> line.substring("description:".length()).trim())
                    .map(raw -> raw.replaceAll("^\"|\"$", ""))
                    .filter(StrUtil::isNotBlank)
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String extractSkillName(String path) {
        if (StrUtil.isBlank(path)) {
            return "unknown";
        }
        String normalized = path.replace("\\", "/");
        int skillMarker = normalized.lastIndexOf("/skills/");
        int fileMarker = normalized.lastIndexOf("/SKILL.md");
        if (skillMarker < 0 || fileMarker <= skillMarker) {
            return "unknown";
        }
        String segment = normalized.substring(skillMarker + "/skills/".length(), fileMarker);
        if (StrUtil.isBlank(segment)) {
            return "unknown";
        }
        int idx = segment.lastIndexOf('/');
        return idx >= 0 ? segment.substring(idx + 1) : segment;
    }

    private String extractSkillPath(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        String normalized = path.replace("\\", "/");
        int skillMarker = normalized.lastIndexOf("/skills/");
        int fileMarker = normalized.lastIndexOf("/SKILL.md");
        if (skillMarker < 0 || fileMarker <= skillMarker) {
            return "";
        }
        return normalized.substring(skillMarker + 1, fileMarker);
    }

    private Optional<String> readContentByDescriptor(SkillDescriptor descriptor) {
        if (descriptor == null || StrUtil.isBlank(descriptor.source())) {
            return Optional.empty();
        }
        if ("file".equals(descriptor.source())) {
            if (StrUtil.isBlank(descriptor.skillPath())) {
                return Optional.empty();
            }
            Path skillFile = Paths.get(descriptor.skillPath()).resolve(SKILL_MD).normalize();
            try {
                if (!Files.exists(skillFile)) {
                    return Optional.empty();
                }
                return Optional.of(Files.readString(skillFile, StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("Read file skill content failed: {}", skillFile, e);
                return Optional.empty();
            }
        }
        if ("classpath".equals(descriptor.source())) {
            if (StrUtil.isBlank(descriptor.skillPath())) {
                return Optional.empty();
            }
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = "classpath*:" + descriptor.skillPath() + "/" + SKILL_MD;
            try {
                Resource[] resources = resolver.getResources(pattern);
                if (resources.length == 0) {
                    return Optional.empty();
                }
                return Optional.of(StreamUtils.copyToString(resources[0].getInputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.warn("Read classpath skill content failed: {}", pattern, e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Long stableSkillId(String source, String skillPath, String name) {
        // 基于来源+路径+名称生成稳定 id，保证 file/classpath 技能也可按 id 选择。
        String raw = StrUtil.blankToDefault(source, "unknown") + '|'
                + StrUtil.blankToDefault(skillPath, "") + '|'
                + StrUtil.blankToDefault(name, "");
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(raw.getBytes(StandardCharsets.UTF_8));
            long value = ByteBuffer.wrap(digest).getLong();
            if (value == Long.MIN_VALUE) {
                return 0L;
            }
            return Math.abs(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}
