package cn.fxbin.bubble.flow.core.model;

import cn.fxbin.bubble.flow.core.enums.PluginType;
import lombok.Data;

import java.io.Serializable;

/**
 * 输入参数定义
 * 示例数据格式：
 * [
 *   {
 *     "name": "apiUrl",          // 参数名称，如: apiUrl
 *     "type": "string",          // 参数类型，支持: string/number/boolean/object等
 *     "required": true,          // 是否必填: true-必填, false-选填
 *     "source": {                // 参数值来源配置
 *       "type": "ref",           // 来源类型: ref-引用其他节点输出, literal-字面量
 *       "nodeId": "configNode",  // 引用的节点ID, 当type=ref时必填
 *       "param": "apiEndpoint"   // 引用的参数名, 当type=ref时必填
 *     },
 *     "defaultValue": "https://api.example.com"  // 默认值，当source未配置时使用
 *   }
 * ]
 * @author fxbin
 * @version v1.0
 * @since 2025/4/18 16:01
 */
@Data
public class InputParamDefinition implements Serializable {

    /**
     * 输入参数ID
     */
    private String id;


    /**
     * 参数名称
     * 示例: apiUrl
     */
    private String name;

    /**
     * 参数类型
     * 支持类型: string/number/boolean/object等
     */
    private String type;

    /**
     * 是否必填
     * true: 必填
     * false: 选填
     */
    private Boolean required;

    /**
     * 参数值来源配置
     * 用于指定参数值如何获取
     */
    private ParamSource source;

    /**
     * 默认值
     * 当source未配置时使用此值
     */
    private String defaultValue;


    @Data
    public static class ParamSource implements Serializable {

        /**
         * 来源类型
         * ref: 引用其他节点输出
         * literal: 字面量
         */
        private String type;

        /**
         * 引用的节点类型
         */
        private PluginType nodeType;

        /**
         * 引用的节点ID
         * 当type=ref时必填
         */
        private String nodeId;

        /**
         * 引用的参数名
         * 当type=ref时必填
         */
        private String param;

        /**
         * 额外参数，回显使用
         */
        private Object value;

    }

}
