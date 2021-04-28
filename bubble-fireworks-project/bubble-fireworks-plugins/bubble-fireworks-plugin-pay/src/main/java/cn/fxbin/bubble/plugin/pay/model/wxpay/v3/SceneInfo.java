package cn.fxbin.bubble.plugin.pay.model.wxpay.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * SceneInfo 场景信息
 *
 * <p>
 *     支付场景描述
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 17:56
 */
@Data
@Builder
@Accessors(chain = true)
public class SceneInfo {

    /**
     * 用户终端IP
     * 用户的客户端IP，支持IPv4和IPv6两种格式的IP地址。
     */
    @JsonProperty("payer_client_ip")
    private String payerClientIp;

    /**
     * 商户端设备号
     * 商户端设备号（门店号或收银设备ID）。
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 商户门店信息
     */
    @JsonProperty("store_info")
    private StoreInfo storeInfo;


    /**
     * 商户门店信息
     */
    @Data
    @Builder
    @Accessors(chain = true)
    public static class StoreInfo {

        /**
         * 门店编号
         */
        private String id;

        /**
         * 门店名称
         */
        private String name;

        /**
         * 地区编码
         */
        @JsonProperty("area_code")
        private String areaCode;

        /**
         * 详细地址
         */
        private String address;

    }

}
