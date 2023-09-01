package cn.fxbin.bubble.core.dataobject;

/**
 * BizErrorCode
 *
 * <p>
 *     业务状态码定义，业务方可做相应实现
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/29 00:15
 */
public non-sealed interface BizErrorCode extends ErrorCode {

    /**
     * 错误码解析
     *
     * @param errorCode 错误码
     * @return {@link BizErrorCode}
     */
    BizErrorCode resolve(int errorCode);

}
