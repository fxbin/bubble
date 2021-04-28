package cn.fxbin.bubble.plugin.pay.model.wxpay.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * WxOrderQueryRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:21
 */
@Data
@Builder
@Accessors(chain = true)
public class WxOrderQueryRequest {

    /**
     * 直连商户号
     */
    @JsonProperty("mchid")
    private String mchId;

    /**
     * 商户订单号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

}
