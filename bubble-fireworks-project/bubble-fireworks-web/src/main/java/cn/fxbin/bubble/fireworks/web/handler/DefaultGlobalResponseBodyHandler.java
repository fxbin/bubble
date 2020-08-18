package cn.fxbin.bubble.fireworks.web.handler;

import cn.fxbin.bubble.fireworks.core.model.Result;
import com.google.common.collect.Lists;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

/**
 * DefaultGlobalResponseBodyHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/28 10:22
 */
@SuppressWarnings("ALL")
@RestControllerAdvice
public class DefaultGlobalResponseBodyHandler implements ResponseBodyAdvice {

    private final static List<String> SWAGGER_RESOURCES = Lists.newArrayList(
            "/v2/api-docs", "/v2/api-docs-ext","/swagger-resources", "/swagger-resources/configuration/ui", "/swagger-resources/configuration/security");

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        String path = serverHttpRequest.getURI().getPath();

        if (SWAGGER_RESOURCES.stream().filter(swaggerResource -> path.contains(swaggerResource)).count() >= 1L){
            return body;
        }

        // 如果已经是 Result 类型，则直接返回
        if (body instanceof Result) {
            return body;
        }
        // 如果不是，则包装成 Result 类型
        return Result.success(body);
    }
}
