package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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
@Configuration(
        proxyBeanMethods = false
)
@Import({SwaggerResourceHandler.class, SwaggerSecurityHandler.class, SwaggerUiHandler.class})
@ConditionalOnClass({GatewayAutoConfiguration.class, SwaggerResourcesProvider.class})
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SwaggerResourceConfiguration implements SwaggerResourcesProvider {

    private static final String API_URI = "/v2/api-docs";

    @Resource
    private RouteLocator routeLocator;

    @Resource
    private GatewayProperties gatewayProperties;

    @Resource
    private RouteDefinitionRepository routeDefinitionRepository;

    @Override
    public List<SwaggerResource> get() {

        List<SwaggerResource> resourceList = new ArrayList<>();
        List<RouteDefinition> routeDefinitionList = new ArrayList<>();
        List<Route> routeList = new ArrayList<>();

        routeDefinitionRepository.getRouteDefinitions().subscribe(routeDefinitionList::add);
        routeLocator.getRoutes().subscribe(routeList::add);

        // 动态路由信息
        List<SwaggerResource> dynamicConfigList = routeDefinitionList.stream().flatMap(routeDefinition -> routeDefinition.getPredicates().stream()
                .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                .map(predicateDefinition ->
                        swaggerResource(routeDefinition.getId(), predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI))
                )).sorted(Comparator.comparing(SwaggerResource::getName))
                .collect(Collectors.toList());


        // 配置文件信息
        List<SwaggerResource> fileConfigList = gatewayProperties.getRoutes().stream()
                .filter(routeDefinition -> routeList.stream().map(Route::getId).collect(Collectors.toList()).contains(routeDefinition.getId()))
                .flatMap(routeDefinition -> routeDefinition.getPredicates().stream()
                        .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                        .map(predicateDefinition ->
                                swaggerResource(routeDefinition.getId(), predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI))
                        )).sorted(Comparator.comparing(SwaggerResource::getName))
                .collect(Collectors.toList());

        resourceList.addAll(dynamicConfigList);
        resourceList.addAll(fileConfigList);

        // 去重
        return resourceList.parallelStream().distinct().collect(Collectors.toList());
    }


    private SwaggerResource swaggerResource(String name, String location) {
        log.info("name:{},location:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}
