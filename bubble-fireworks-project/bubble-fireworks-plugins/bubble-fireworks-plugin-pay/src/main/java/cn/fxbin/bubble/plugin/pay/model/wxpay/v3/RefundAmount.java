package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RefundAmount
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:51
 */
@Data
@Builder
@Accessors(chain = true)
public class RefundAmount {

    /**
     * 总金额
     */
    private int total;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 退款金额
     */
    private int refund;

}
