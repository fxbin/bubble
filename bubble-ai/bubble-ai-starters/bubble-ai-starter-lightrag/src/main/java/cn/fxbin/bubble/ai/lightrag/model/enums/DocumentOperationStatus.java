package cn.fxbin.bubble.ai.lightrag.model.enums;

import cn.fxbin.bubble.core.enumeration.IEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文档操作状态枚举
 * 
 * <p>定义 LightRAG 文档 API 各端点返回的所有可能状态值。
 * 每个状态代表文档处理生命周期中的特定状态。</p>
 * 
 * <h3>状态分类：</h3>
 * <ul>
 *   <li><strong>扫描操作：</strong> 文档扫描操作的状态值</li>
 *   <li><strong>插入操作：</strong> 文档插入操作的状态值</li>
 *   <li><strong>删除操作：</strong> 文档删除操作的状态值</li>
 *   <li><strong>缓存操作：</strong> 缓存管理操作的状态值</li>
 *   <li><strong>处理状态：</strong> 文档处理生命周期的状态值</li>
 * </ul>
 * 
 * @author fxbin
 * @version v1.0
 * @since 2025-08-28
 */
@Getter
@RequiredArgsConstructor
public enum DocumentOperationStatus implements IEnum {

    // ========== 扫描操作状态 ==========
    /**
     * 后台扫描进程已启动
     * 使用接口：/documents/scan
     */
    SCANNING_STARTED(1, "scanning_started", "扫描进程已启动"),

    // ========== 插入操作状态 ==========
    /**
     * 操作成功完成
     * 使用接口：/documents/upload, /documents/text, /documents/texts
     */
    SUCCESS(2, "success", "操作成功完成"),

    /**
     * 文件已存在（仅上传操作）
     * 使用接口：/documents/upload
     */
    DUPLICATED(3, "duplicated", "文件已存在"),

    /**
     * 批量操作部分成功
     * 使用接口：/documents/texts
     */
    PARTIAL_SUCCESS(4, "partial_success", "操作部分成功"),

    /**
     * 操作失败
     * 使用接口：/documents/upload, /documents/text, /documents/texts
     */
    FAILURE(5, "failure", "操作失败"),

    // ========== 删除操作状态 ==========
    /**
     * 文档删除已在后台启动
     * 使用接口：/documents/delete_document
     */
    DELETION_STARTED(6, "deletion_started", "文档删除已在后台启动"),

    /**
     * 管道忙碌，无法执行操作
     * 使用接口：DELETE /documents, /documents/delete_document
     */
    BUSY(7, "busy", "管道忙碌，无法执行操作"),

    /**
     * 操作不被允许（例如：实体提取 LLM 缓存被禁用时）
     * 使用接口：/documents/delete_document
     */
    NOT_ALLOWED(8, "not_allowed", "操作不被允许"),

    /**
     * 所有文档成功清除
     * 使用接口：DELETE /documents
     */
    ALL_CLEARED(9, "success", "所有文档成功清除"),

    /**
     * 文档清除完成但有错误
     * 使用接口：DELETE /documents
     */
    PARTIAL_CLEARED(10, "partial_success", "文档清除完成但有错误"),

    /**
     * 所有存储删除操作失败
     * 使用接口：DELETE /documents
     */
    CLEAR_FAILED(11, "fail", "所有存储删除操作失败"),

    // ========== 缓存操作状态 ==========
    /**
     * 缓存清除成功
     * 使用接口：/documents/clear_cache
     */
    CACHE_SUCCESS(12, "success", "缓存清除成功"),

    /**
     * 缓存清除失败
     * 使用接口：/documents/clear_cache
     */
    CACHE_FAILED(13, "fail", "缓存清除失败"),

    // ========== 实体/关系操作状态 ==========
    /**
     * 实体或关系未找到
     * 使用接口：/documents/delete_entity, /documents/delete_relation
     */
    NOT_FOUND(14, "not_found", "实体或关系未找到"),

    /**
     * 删除失败
     * 使用接口：/documents/delete_entity, /documents/delete_relation
     */
    DELETE_FAILED(15, "fail", "删除失败");

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
    public static DocumentOperationStatus fromStatusValue(String statusValue) {
        if (statusValue == null) {
            return null;
        }
        for (DocumentOperationStatus status : values()) {
            if (status.statusValue.equals(statusValue)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查此状态是否表示成功的操作
     * 
     * @return 如果操作成功则返回 true
     */
    public boolean isSuccess() {
        return this == SUCCESS || this == ALL_CLEARED || this == CACHE_SUCCESS || 
               this == SCANNING_STARTED || this == DELETION_STARTED;
    }

    /**
     * 检查此状态是否表示部分成功
     * 
     * @return 如果操作部分成功则返回 true
     */
    public boolean isPartialSuccess() {
        return this == PARTIAL_SUCCESS || this == PARTIAL_CLEARED;
    }

    /**
     * 检查此状态是否表示失败
     * 
     * @return 如果操作失败则返回 true
     */
    public boolean isFailure() {
        return this == FAILURE || this == CLEAR_FAILED || this == CACHE_FAILED || 
               this == DELETE_FAILED || this == NOT_FOUND || this == NOT_ALLOWED;
    }

    /**
     * 检查此状态是否表示正在进行的过程
     * 
     * @return 如果操作正在进行则返回 true
     */
    public boolean isInProgress() {
        return this == SCANNING_STARTED || this == DELETION_STARTED || this == BUSY;
    }

}