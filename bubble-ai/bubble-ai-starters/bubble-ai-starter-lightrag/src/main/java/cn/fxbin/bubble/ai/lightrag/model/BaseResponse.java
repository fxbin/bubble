package cn.fxbin.bubble.ai.lightrag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 基础响应模型
 * 
 * <p>抽象基类，包含所有响应模型的公共字段，减少代码重复。
 * 所有具体的响应类都应该继承此基类。</p>
 *
 * <p>示例数据：
 * <pre>
 *     {
 *   "detail": "400: Unsupported file type. Supported types: ('.txt', '.md', '.pdf', '.docx', '.pptx', '.xlsx', '.rtf', '.odt', '.tex', '.epub', '.html', '.htm', '.csv', '.json', '.xml', '.yaml', '.yml', '.log', '.conf', '.ini', '.properties', '.sql', '.bat', '.sh', '.c', '.cpp', '.py', '.java', '.js', '.ts', '.swift', '.go', '.rb', '.php', '.css', '.scss', '.less')"
 * }
 * </pre>
 *
 *
 * @author fxbin
 * @version v1.0
 * @since 2025-08-22
 */
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseResponse implements Serializable {

    /**
     * 错误详情
     */
    private String detail;

}