package cn.fxbin.bubble.plugin.pay.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WeChatServer
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/30 16:21
 */
@Getter
@AllArgsConstructor
public enum WeChatServer {

    /**
     * 中国
     */
    CHINA("https://api.mch.weixin.qq.com"),

    /**
     * 中国国内(备用域名)
     */
    CHINA2("https://api2.mch.weixin.qq.com"),

    /**
     * 香港
     */
    HK("https://apihk.mch.weixin.qq.com"),

    /**
     * 美国
     */
    US("https://apius.mch.weixin.qq.com"),

    /**
     * 获取公钥
     */
    FRAUD("https://fraud.mch.weixin.qq.com"),

    /**
     * 活动
     */
    ACTION("https://action.weixin.qq.com");

    /**
     * 域名
     */
    private final String domain;

}
