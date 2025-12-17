package cn.fxbin.bubble.flow.core.provider;

import cn.fxbin.bubble.flow.core.annotation.VisualNode;
import cn.fxbin.bubble.flow.core.model.dto.NodeMetaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NodeMetadataProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/4/21 16:28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeMetadataProvider {


    private final ApplicationContext applicationContext;

    private List<NodeMetaDTO> cachedMetadata;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有节点元数据（带缓存机制）
     */
    public List<NodeMetaDTO> getAllNodeMetadata() {
        if (cachedMetadata == null) {
            cachedMetadata = scanNodeMetadata();
        }
        return cachedMetadata;
    }

    /**
     * 扫描所有带 @VisualNode 注解的组件
     */
    private List<NodeMetaDTO> scanNodeMetadata() {
        Map<String, Object> nodes = applicationContext.getBeansWithAnnotation(VisualNode.class);
        return nodes.values().stream()
                .map(bean -> {
                    Class<?> clazz = bean.getClass();
                    VisualNode meta = clazz.getAnnotation(VisualNode.class);
                    return new NodeMetaDTO(
                            meta.name(),
                            meta.pluginType(),
                            meta.desc(),
                null,
//                            this.generateInputSchema(meta.configClazz()),
                            this.generateOutputSchema(clazz)
                    );
                })
                .collect(Collectors.toList());
    }

//    /**
//     * 生成输入参数的 JSON Schema
//     */
//    private JsonNode generateInputSchema(Class<?> configClazz) {
//        if (configClazz == Void.class) {
//            return JsonNodeFactory.instance.objectNode();
//        }
//        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
//        return jsonSchemaGenerator.generateJsonSchema(configClazz);
//    }

    /**
     * 生成输出参数的 JSON Schema（通过解析节点类的输出方法）
     */
    private JsonNode generateOutputSchema(Class<?> nodeClass) {
        // 假设所有节点输出均通过 context.set(key, value) 
        // 这里可扩展为解析注解或固定结构
        return JsonNodeFactory.instance.objectNode()
                .put("type", "object")
                .set("properties", new ObjectMapper().createObjectNode()
                        .put("output", "object"));
    }
    
}
