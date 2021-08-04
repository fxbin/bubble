package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SwaggerProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/3 11:19
 */
@Slf4j
@Primary
@Component
@ConditionalOnClass({HandlerFunction.class})
public class SwaggerProvider implements SwaggerResourcesProvider {

    private static final String API_URI = "/v2/api-docs";


    @Resource
    private RouteDefinitionRepository routeDefinitionRepository;

    @Resource
    private DiscoveryClient discoveryClient;

    @Override
    public List<SwaggerResource> get() {

        List<RouteDefinition> routeList = new ArrayList<>();

        routeDefinitionRepository.getRouteDefinitions()
                .sort(Comparator.comparingInt(RouteDefinition::getOrder))
                .subscribe(routeList::add);

        return routeList.stream().flatMap(routeDefinition -> routeDefinition.getPredicates().stream()
                .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                .map(predicateDefinition ->
                        swaggerResource(routeDefinition.getId(), predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI))
                ))
                // 过滤注册中心没有的服务
                .filter(swaggerResource -> discoveryClient.getServices().stream().anyMatch(serviceId -> serviceId.equalsIgnoreCase(swaggerResource.getName())))
                .sorted(Comparator.comparing(SwaggerResource::getName))
                .distinct()
                .collect(Collectors.toList());
    }


    private SwaggerResource swaggerResource(String name, String location) {
        log.info("name:{}, location:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}
