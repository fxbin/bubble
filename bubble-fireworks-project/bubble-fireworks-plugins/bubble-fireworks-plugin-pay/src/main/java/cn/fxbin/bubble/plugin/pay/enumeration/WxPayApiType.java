package cn.fxbin.bubble.plugin.pay.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

/**
 * WxPayApiType
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/30 16:24
 */
@Getter
@AllArgsConstructor
public enum WxPayApiType {

    /**
     * APP 下单 API
     */
    PAY_APP(HttpMethod.POST, "/v3/pay/transactions/app"),
    /**
     * JS API 下单 API
     */
    PAY_JS_API(HttpMethod.POST, "/v3/pay/transactions/jsapi"),
    /**
     * Native 下单 API
     */
    PAY_NATIVE(HttpMethod.POST, "/v3/pay/transactions/native"),
    /**
     * H5 下单 API
     */
    PAY_H5(HttpMethod.POST, "/v3/pay/transactions/h5"),

    /**
     * 关闭订单.
     */
    CLOSE(HttpMethod.POST, "/v3/pay/transactions/out-trade-no/{out_trade_no}/close"),

    /**
     * 微信支付订单号查询API.
     */
    TRANSACTION_TRANSACTION_ID(HttpMethod.GET, "/v3/pay/transactions/id/{transaction_id}"),

    /**
     * 商户订单号查询API.
     */
    TRANSACTION_OUT_TRADE_NO(HttpMethod.GET, "/v3/pay/transactions/out-trade-no/{out_trade_no}"),

    /**
     * 申请退款API.
     */
    REFUND(HttpMethod.POST, "/v3/refund/domestic/refunds"),

    /**
     * 查询退款API.
     */
    QUERY_REFUND(HttpMethod.GET, "/v3/refund/domestic/refunds/{out_refund_no}"),

    ;

    private final HttpMethod method;

    private final String uri;

}
