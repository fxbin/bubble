package cn.fxbin.bubble.plugin.pay.model;

import cn.fxbin.bubble.plugin.pay.enums.PayType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * PayRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 17:10
 */
@Data
public class PayRequest {

    /**
     * 支付方式
     */
    private PayType payType;
    /**
     * 订单号.
     */
    private String orderId;

    /**
     * 订单金额.
     */
    private BigDecimal orderAmount;

    /**
     * 订单名字.
     */
    private String orderName;

    /**
     * 微信openid, 仅微信公众号/小程序支付时需要
     */
    private String openid;

    /**
     * 附加内容，发起支付时传入
     */
    private String attach;



}
