package cn.fxbin.bubble.plugin.pay.model.wxpay.request;

import cn.fxbin.bubble.plugin.pay.model.wxpay.v3.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WxPayUnifiedOrderRequest
 *
 * <p>
 *     统一下单请求对象
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 18:09
 */
public class WxPayUnifiedOrderRequest {

    /**
     * 应用ID
     * 由微信生成的应用ID，全局唯一。请求统一下单接口时请注意APPID的应用属性，例如公众号场景下，需使用应用属性为公众号的APPID
     */
    @JsonProperty("appid")
    private String appId;

    /**
     * 直连商户号
     * 直连商户的商户号，由微信支付生成并下发。
     */
    @JsonProperty("mchid")
    private String mchId;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商户订单号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

    /**
     * 交易结束时间
     * 订单失效时间，遵循rfc3339标准格式，
     * 格式为YYYY-MM-DDTHH:mm:ss+TIMEZONE，YYYY-MM-DD表示年月日，T出现在字符串中，表示time元素的开头，HH:mm:ss表示时分秒，TIMEZONE表示时区（+08:00表示东八区时间，领先UTC 8小时，即北京时间）。
     * 例如：2015-05-20T13:29:35+08:00表示，北京时间2015年5月20日 13点29分35秒。
     */
    @JsonProperty("time_expire")
    private String timeExpire;

    /**
     * 附加数据
     * 附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用
     */
    private String attach;

    /**
     * 通知地址
     * 通知URL必须为直接可访问的URL，不允许携带查询串，要求必须为https地址。
     */
    @JsonProperty("notify_url")
    private String notifyUrl;

    /**
     * 订单优惠标记
     */
    @JsonProperty("goods_tag")
    private String goodsTag;

    /**
     * 订单金额信息
     */
    private Amount amount;

    /**
     * 支付者信息
     */
    private Payer payer;

    /**
     * 优惠功能
     */
    private Detail detail;

    /**
     * 支付场景描述
     */
    @JsonProperty("scene_info")
    private SceneInfo sceneInfo;

    /**
     * 结算信息
     */
    @JsonProperty("settle_info")
    private SettleInfo settleInfo;

}
