package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * SettleInfo 结算信息
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:05
 */
@Data
@Builder
@Accessors(chain = true)
public class SettleInfo {

    /**
     * 是否指定分账
     */
    @JsonProperty("profit_sharing")
    private boolean profitSharing;

}
