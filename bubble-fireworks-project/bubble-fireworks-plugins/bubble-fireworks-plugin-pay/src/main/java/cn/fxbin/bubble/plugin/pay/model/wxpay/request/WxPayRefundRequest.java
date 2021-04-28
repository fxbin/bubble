package cn.fxbin.bubble.plugin.pay.model.wxpay.request;

import cn.fxbin.bubble.plugin.pay.model.wxpay.v3.RefundAmount;
import cn.fxbin.bubble.plugin.pay.model.wxpay.v3.RefundGoodsDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * WxPayRefundRequest
 *
 * <p>
 *     申请退款请求对象
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:34
 */
@Data
@Builder
@Accessors(chain = true)
public class WxPayRefundRequest {


    /**
     * 微信支付订单号
     * 与【商户订单号】二选一
     */
    @JsonProperty("transaction_id")
    private String transactionId;

    /**
     * 商户订单号
     * 与【微信支付订单号】二选一
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

    /**
     * 商户退款单号
     */
    @JsonProperty("out_refund_no")
    private String outRefundNo;

    /**
     *
     */
    private String reason;

    /**
     * 退款结果回调url
     */
    @JsonProperty("notify_url")
    private String notifyUrl;

    /**
     * 退款资金来源
     */
    @JsonProperty("funds_account")
    private String fundsAccount;

    /**
     * 订单金额信息
     */
    private RefundAmount amount;

    /**
     * 退款商品
     */
    @JsonProperty("goods_detail")
    private RefundGoodsDetail goodsDetail;

}
