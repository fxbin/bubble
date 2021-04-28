package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * GoodsDetail
 *
 * <p>
 *     商品
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:46
 */
@Data
@Builder
@Accessors(chain = true)
public class GoodsDetail {

    /**
     * 商户侧商品编码
     * 由半角的大小写字母、数字、中划线、下划线中的一种或几种组成。
     */
    @JsonProperty("merchant_goods_id")
    private String merchantGoodsId;

    /**
     * 微信侧商品编码
     * 微信支付定义的统一商品编号（没有可不传）
     */
    @JsonProperty("wechatpay_goods_id")
    private String wechatpayGoodsId;

    /**
     * 商品名称
     */
    @JsonProperty("goods_name")
    private String goodsName;

    /**
     * 商品数量
     */
    private int quantity;

    /**
     * 商品单价
     */
    @JsonProperty("unit_price")
    private int unitPrice;

}
