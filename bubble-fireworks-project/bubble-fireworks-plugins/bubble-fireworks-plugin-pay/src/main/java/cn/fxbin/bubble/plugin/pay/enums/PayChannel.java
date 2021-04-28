package cn.fxbin.bubble.plugin.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PayChannel 支付渠道
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 15:57
 */
@Getter
@AllArgsConstructor
public enum PayChannel {

    /**
     * 微信支付
     */
    WX("wx", "微信"),

    /**
     * 支付宝支付
     */
    ALIPAY("alipay", "支付宝"),
    ;


    private final String code;

    private final String name;

}
