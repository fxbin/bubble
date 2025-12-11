package cn.fxbin.bubble.core.util;

import cn.fxbin.bubble.core.exception.UtilException;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * DiffUtils
 *
 * <p>
 *     文本差异对比工具类，基于 java-diff-utils 封装。
 *     提供文本比对、生成 Unified Diff 格式、应用补丁等功能。
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/12/11 10:03
 */
@Slf4j
@UtilityClass
public class DiffUtils {


    /**
     * 计算两个文本列表的差异
     *
     * @param original 原文本列表
     * @param revised  修改后的文本列表
     * @param <T>      文本类型
     * @return {@link Patch} 差异补丁对象
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised) {
        Assert.notNull(original, "original list must not be null");
        Assert.notNull(revised, "revised list must not be null");
        return com.github.difflib.DiffUtils.diff(original, revised);
    }

    /**
     * 生成 Unified Diff 格式的差异数据
     *
     * @param originalFileName 原始文件名
     * @param revisedFileName  修改后的文件名
     * @param originalLines    原始文本行列表
     * @param patch            差异补丁
     * @param contextSize      上下文行数（即差异行上下保留的行数）
     * @return Unified Diff 格式的字符串列表
     */
    public static List<String> generateUnifiedDiff(String originalFileName, String revisedFileName,
                                                   List<String> originalLines, Patch<String> patch, int contextSize) {
        Assert.notNull(originalLines, "originalLines must not be null");
        Assert.notNull(patch, "patch must not be null");

        return UnifiedDiffUtils.generateUnifiedDiff(
                Optional.ofNullable(originalFileName).orElse("Original"),
                Optional.ofNullable(revisedFileName).orElse("Revised"),
                originalLines,
                patch,
                Math.max(0, contextSize)
        );
    }

    /**
     * 对原始文本应用补丁，生成新的文本
     *
     * @param originalLines 原始文本行列表
     * @param patch         差异补丁
     * @return 应用补丁后的文本列表
     * @throws UtilException 如果应用补丁失败
     */
    public static List<String> patch(List<String> originalLines, Patch<String> patch) {
        Assert.notNull(originalLines, "originalLines must not be null");
        Assert.notNull(patch, "patch must not be null");

        try {
            return com.github.difflib.DiffUtils.patch(originalLines, patch);
        } catch (PatchFailedException e) {
            log.error("Failed to patch content", e);
            throw new UtilException("Failed to patch content", e);
        }
    }

    /**
     * 快捷方法：计算差异并生成 Unified Diff 格式
     *
     * @param originalLines 原始文本行列表
     * @param revisedLines  修改后的文本行列表
     * @param contextSize   上下文行数
     * @return Unified Diff 格式的字符串列表
     */
    public static List<String> unifiedDiff(List<String> originalLines, List<String> revisedLines, int contextSize) {
        Patch<String> patch = diff(originalLines, revisedLines);
        return generateUnifiedDiff(null, null, originalLines, patch, contextSize);
    }

    /**
     * 生成行级差异详情（用于展示）
     *
     * @param originalLines 原始文本
     * @param revisedLines  修改后文本
     * @param inlineDiff    是否显示行内差异
     * @return 差异行列表
     */
    public static List<DiffRow> diffRows(List<String> originalLines, List<String> revisedLines, boolean inlineDiff) {
        Assert.notNull(originalLines, "originalLines must not be null");
        Assert.notNull(revisedLines, "revisedLines must not be null");

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(inlineDiff)
                .inlineDiffByWord(inlineDiff)
                .mergeOriginalRevised(inlineDiff)
                .build();

        return generator.generateDiffRows(originalLines, revisedLines);
    }


}
