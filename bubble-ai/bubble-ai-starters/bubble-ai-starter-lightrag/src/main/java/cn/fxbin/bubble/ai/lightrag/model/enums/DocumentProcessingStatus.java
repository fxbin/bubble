package cn.fxbin.bubble.ai.lightrag.model.enums;

import cn.fxbin.bubble.core.enumeration.IEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文档处理状态枚举
 * 
 * <p>表示 LightRAG 中 DocStatus 定义的文档处理生命周期状态。
 * 这些状态跟踪文档在处理管道中的进度。</p>
 * 
 * <h3>处理生命周期：</h3>
 * <ol>
 *   <li><strong>PENDING：</strong> 文档排队等待处理</li>
 *   <li><strong>PROCESSING：</strong> 文档正在处理中</li>
 *   <li><strong>PROCESSED：</strong> 文档处理成功完成</li>
 *   <li><strong>FAILED：</strong> 文档处理遇到错误</li>
 * </ol>
 * 
 * <h3>使用场景：</h3>
 * <p>用于以下接口：</p>
 * <ul>
 *   <li>/documents - 获取所有文档及其处理状态</li>
 *   <li>/documents/paginated - 获取分页文档及状态</li>
 *   <li>/documents/track_status/{track_id} - 跟踪特定文档处理</li>
 * </ul>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-28
 */
@Getter
@RequiredArgsConstructor
public enum DocumentProcessingStatus implements IEnum {

    /**
     * 文档等待处理
     */
    PENDING(1, "PENDING", "文档等待处理"),

    /**
     * 文档正在处理中
     */
    PROCESSING(2, "PROCESSING", "文档正在处理中"),

    /**
     * 文档处理成功完成
     */
    PROCESSED(3, "PROCESSED", "文档处理成功完成"),

    /**
     * 文档处理失败
     */
    FAILED(4, "FAILED", "文档处理失败");

    private final int value;
    private final String statusValue;
    private final String description;

    @Override
    public int value() {
        return value;
    }

    /**
     * 获取 API 响应的状态字符串值
     * 
     * @return 状态字符串值
     */
    public String getStatusValue() {
        return statusValue;
    }

    /**
     * 根据状态字符串值获取状态枚举
     * 
     * @param statusValue 状态字符串值
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static DocumentProcessingStatus fromStatusValue(String statusValue) {
        if (statusValue == null) {
            return null;
        }
        for (DocumentProcessingStatus status : values()) {
            if (status.statusValue.equals(statusValue)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查此状态是否表示完成（成功或失败）
     * 
     * @return 如果处理完成则返回 true
     */
    public boolean isComplete() {
        return this == PROCESSED || this == FAILED;
    }

    /**
     * 检查此状态是否表示成功完成
     * 
     * @return 如果处理成功完成则返回 true
     */
    public boolean isSuccessful() {
        return this == PROCESSED;
    }

    /**
     * 检查此状态是否表示正在处理
     * 
     * @return 如果正在处理则返回 true
     */
    public boolean isInProgress() {
        return this == PROCESSING;
    }

    /**
     * 检查此状态是否表示等待状态
     * 
     * @return 如果正在等待处理则返回 true
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 检查此状态是否表示失败
     * 
     * @return 如果处理失败则返回 true
     */
    public boolean isFailed() {
        return this == FAILED;
    }

}