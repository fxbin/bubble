package cn.fxbin.bubble.plugin.pay.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static cn.fxbin.bubble.plugin.pay.enumeration.PayChannel.WX;

/**
 * PayType 支付类型
 *
 * <p>
 *     微信支付产品类型 <a href="#">https://pay.weixin.qq.com/wiki/doc/apiv3/terms_definition/chapter1_1_0.shtml</a>
 *      JSAPI：公众号支付
 *      NATIVE：扫码支付
 *      APP：APP支付
 *      MICROPAY：付款码支付
 *      MWEB：H5支付
 *      FACEPAY：刷脸支付
 *
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 16:30
 */
@Getter
@AllArgsConstructor
public enum PayType {


    /**
     *  JSAPI网页支付（公众号支付）
     */
    WXPAY_JSAPI("JSAPI", WX, "JSAPI支付"),

    /**
     * H5支付
     */
    WXPAY_MWEB("MWEB", WX, "微信H5支付"),

    /**
     * Native原生支付
     */
    WXPAY_NATIVE("NATIVE", WX, "微信Native支付"),

    /**
     * 小程序支付
     */
    WXPAY_MINI("JSAPI", WX, "微信小程序支付"),

    /**
     *  APP支付
     */
    WXPAY_APP("APP", WX, "微信APP支付"),

    /**
     * 付款码支付
     */
    WXPAY_MICROPAY("MICROPAY", WX, "微信付款码支付"),

    /**
     * 刷脸支付
     */
    WXPAY_FACEPAY("FACEPAY", WX, ""),
    ;


    private final String code;

    private final PayChannel channel;

    private final String desc;

}
