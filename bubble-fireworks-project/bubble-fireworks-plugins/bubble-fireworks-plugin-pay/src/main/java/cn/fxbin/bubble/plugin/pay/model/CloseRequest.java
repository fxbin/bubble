package cn.fxbin.bubble.plugin.pay.model;

import cn.fxbin.bubble.plugin.pay.enums.PayType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * WxOrderCloseRequest
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:24
 */
@Data
@Builder
@Accessors(chain = true)
public class CloseRequest {

    /**
     * 支付方式
     */
    private PayType payType;

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
