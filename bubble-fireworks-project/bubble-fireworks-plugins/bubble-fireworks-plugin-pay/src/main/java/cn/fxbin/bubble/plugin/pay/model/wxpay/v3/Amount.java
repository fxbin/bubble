package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Amount 订单金额
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 17:15
 */
@Data
@Builder
@Accessors(chain = true)
public class Amount {

    /**
     * 总金额
     */
    private int total;

    /**
     * 货币类型
     */
    private String currency;

}
