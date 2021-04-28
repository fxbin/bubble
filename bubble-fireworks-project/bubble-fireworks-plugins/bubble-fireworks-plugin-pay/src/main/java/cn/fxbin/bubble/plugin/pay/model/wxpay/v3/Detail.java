package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Detail 优惠功能
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 17:21
 */
@Data
@Builder
@Accessors(chain = true)
public class Detail {

    /**
     * 订单原价
     * 1、商户侧一张小票订单可能被分多次支付，订单原价用于记录整张小票的交易金额。
     * 2、当订单原价与支付金额不相等，则不享受优惠。
     * 3、该字段主要用于防止同一张小票分多次支付，以享受多次优惠的情况，正常支付订单不必上传此参数。
     */
    @JsonProperty("cost_price")
    private int costPrice;

    /**
     * 商品小票ID
     */
    @JsonProperty("invoice_id")
    private String invoiceId;

    /**
     * 单品列表信息
     * 条目个数限制：【1，6000】
     */
    @JsonProperty("goods_detail")
    private List<GoodsDetail> goodsDetail;

}
