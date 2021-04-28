package cn.fxbin.bubble.plugin.pay;

/**
 * AbstractPay
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/28 16:14
 */
public abstract class AbstractPay {

    /**
     * 支付完成后的异步通知地址
     */
    private String notifyUrl;

    /**
     * 默认非沙箱测试
     */
    private boolean sandbox = false;

}
