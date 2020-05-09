package cn.fxbin.bubble.fireworks.autoconfigure.plugin.swagger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * BubbleFireworksSwaggerProperties
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/31 17:35
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = SwaggerProperties.BUBBLE_FIREWORKS_SWAGGER_PREFIX)
public class SwaggerProperties {

    /**
     * swagger prefix
     */
    public static final String BUBBLE_FIREWORKS_SWAGGER_PREFIX = "bubble.fireworks.swagger";

    /**
     * 是否开启 swagger，默认：false
     */
    private boolean enabled = false;

    /**
     * host
     */
    private String host = "";

    /**
     * 标题 默认：XXX服务
     */
    private String title;

    /**
     * 详情描述 默认：XXX服务
     */
    private String description;

    /**
     * 版本， 默认v1.0
     */
    private String version = "v1.0";

    /**
     * 服务条款
     */
    private String termsOfServiceUrl = "";

    /**
     * 许可证
     */
    private String license = "BubbleFireworks";

    /**
     * 许可证url
     */
    private String licenseUrl = "https://github.com/fxbin/bubble-fireworks";

    /**
     * swagger会解析的包路径
     */
    private String basePackage = "";

    /**
     * swagger会解析的url规则
     */
    private List<String> basePath = new ArrayList<String>();

    /**
     * 基于base path需要排除的url规则
     */
    private List<String> excludeBasePath = new ArrayList<String>();

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    /**
     * 文档接口访问时认证信息
     */
    private Authorization authorization = new Authorization();


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {

        /**
         * 联系人
         */
        private String name = "大痴小乙";

        /**
         * 联系人url
         */
        private String url = "https://fxbin.blog.csdn.net/";

        /**
         * 联系人email
         */
        private String email = "fxbin123@gmail.com";

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Authorization {

        /**
         * 开启Authorization，默认：false
         */
        private Boolean enabled = false;

        /**
         * 鉴权策略ID，对应 SecurityReferences ID，默认：Authorization
         */
        private String name = "Authorization";

        /**
         * 鉴权传递的Header参数，默认：TOKEN
         */
        private String keyName = "TOKEN";

        /**
         * 需要开启鉴权URL的正则，默认：^.*$
         */
        private String authRegex = "^.*$";
    }

}
