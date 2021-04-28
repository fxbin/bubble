package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RefundGoodsDetail
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:43
 */
@Data
@Builder
@Accessors(chain = true)
public class RefundGoodsDetail {

    /**
     * 商户侧商品编码
     */
    @JsonProperty("merchant_goods_id")
    private String merchantGoodsId;

    /**
     * 微信侧商品编码
     */
    @JsonProperty("wechatpay_goods_id")
    private String wechatpayGoodsId;

    /**
     * 商品名称
     */
    @JsonProperty("goods_name")
    private String goodsName;

    /**
     * 商品单价
     */
    @JsonProperty("unit_price")
    private int unitPrice;

    /**
     * 商品退款金额
     */
    @JsonProperty("refund_amount")
    private int refundAmount;

    /**
     * 商品退货数量
     */
    @JsonProperty("refund_quantity")
    private int refundQuantity;

}
