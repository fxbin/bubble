package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Payer 支付者
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 17:17
 */
@Data
@Builder
@Accessors(chain = true)
public class Payer {

    /**
     * 用户在直连商户appid下的唯一标识。
     */
    private String openid;

}
