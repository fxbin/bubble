package cn.fxbin.bubble.plugin.satoken.context;

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.context.model.SaTokenContextModelBox;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.SaTokenContextException;
import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * SaTokenContextForTtlStaff
 * Sa-Token 上下文处理器 [TransmittableThreadLocal 版本] ---- 对象存储器
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/6/6 11:07
 */
public class SaTokenContextForTtlStaff {

    /**
     * 当前线程的 [ Box 存储器 ]
     */
    public static TransmittableThreadLocal<SaTokenContextModelBox> modelBoxTtl = new TransmittableThreadLocal<>();

    /**
     * 初始化当前线程的 [ Box 存储器 ]
     * @param request {@link SaRequest}
     * @param response {@link SaResponse}
     * @param storage {@link SaStorage}
     */
    public static void setModelBox(SaRequest request, SaResponse response, SaStorage storage) {
        SaTokenContextModelBox box = new SaTokenContextModelBox(request, response, storage);
        modelBoxTtl.set(box);
    }

    /**
     * 清除当前线程的 [ Box 存储器 ]
     */
    public static void clearModelBox() {
        modelBoxTtl.remove();
    }

    /**
     * 获取当前线程的 [ Box 存储器 ]
     * @return /
     */
    public static SaTokenContextModelBox getModelBoxOrNull() {
        return modelBoxTtl.get();
    }

    /**
     * 获取当前线程的 [ Box 存储器 ], 如果为空则抛出异常
     * @return /
     */
    public static SaTokenContextModelBox getModelBox() {
        SaTokenContextModelBox box = modelBoxTtl.get();
        if(box ==  null) {
            throw new SaTokenContextException("SaTokenContext 上下文尚未初始化").setCode(SaErrorCode.CODE_10002);
        }
        return box;
    }

    /**
     * 在当前线程的 SaRequest 包装对象
     *
     * @return /
     */
    public static SaRequest getRequest() {
        return getModelBox().getRequest();
    }

    /**
     * 在当前线程的 SaResponse 包装对象
     *
     * @return /
     */
    public static SaResponse getResponse() {
        return getModelBox().getResponse();
    }

    /**
     * 在当前线程的 SaStorage 存储器包装对象
     *
     * @return /
     */
    public static SaStorage getStorage() {
        return getModelBox().getStorage();
    }


}

