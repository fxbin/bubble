package cn.fxbin.bubble.flow.core.scanner;

import cn.hutool.extra.pinyin.PinyinUtil;
import cn.fxbin.bubble.flow.core.annotation.VisualNode;
import cn.fxbin.bubble.flow.core.mapper.FlowPluginMapper;
import cn.fxbin.bubble.flow.core.model.entity.FlowPluginDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NodeMetadataScanner
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/17 11:06
 */
@Component
@RequiredArgsConstructor
public class NodeMetadataScanner {

    private final ApplicationContext applicationContext;
    private final FlowPluginMapper flowPluginMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 扫描所有带 @VisualNode 的节点并注册元数据
     */
    @PostConstruct
    public void scanAndRegisterNodes() {
        Map<String, Object> nodes = applicationContext.getBeansWithAnnotation(VisualNode.class);
        nodes.forEach((beanName, bean) -> {
            VisualNode vn = bean.getClass().getAnnotation(VisualNode.class);
            // 使用 name + code 生成唯一ID
            // 将中文名称转换为拼音，并移除声调，用下划线连接
            String pinyin = PinyinUtil.getPinyin(vn.name(), "_");
            // 生成唯一标识：拼音 + 下划线 + 分类
            String id = pinyin.toLowerCase() + "_" + vn.pluginType();

            FlowPluginDefinition flowPluginDefinition = new FlowPluginDefinition();
            flowPluginDefinition.setId(id);
            flowPluginDefinition.setName(vn.name());
            flowPluginDefinition.setVersion(1);
            flowPluginDefinition.setType(vn.pluginType());
            flowPluginDefinition.setDescription(vn.desc());
//            flowPluginDefinition.setMetadata(convertJsonNodeToString(parseJsonSchema(vn.configClazz())));

            flowPluginMapper.saveOrUpdate(flowPluginDefinition);
        });
    }

//    /**
//     * 解析配置类的字段生成 JSON Schema
//     * @param configClazz 配置类
//     * @return JsonNode
//     */
//    private JsonNode parseJsonSchema(Class<?> configClazz) {
//        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
//        // 使用 Jackson 生成 Schema
//        return jsonSchemaGenerator.generateJsonSchema(configClazz);
//    }

    /**
     * 将 JsonNode 转换为 JSON 字符串
     * @param jsonNode JsonNode 对象
     * @return JSON 字符串
     */
    private String convertJsonNodeToString(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("转换 JsonNode 到字符串失败", e);
        }
    }


}
