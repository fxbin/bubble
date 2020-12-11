package cn.fxbin.bubble.fireworks.cloud.feign;

import cn.fxbin.bubble.fireworks.core.exception.ServiceException;
import cn.fxbin.bubble.fireworks.core.model.Result;
import cn.fxbin.bubble.fireworks.core.util.JsonUtils;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

import static feign.FeignException.errorStatus;

/**
 * CustomizeFeignException
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/12/11 17:42
 */
public class CustomizeFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = errorStatus(methodKey, response);
        if (JsonUtils.isJsonString(exception.contentUTF8())) {
            Result<?> result = JsonUtils.parse(exception.contentUTF8(), Result.class);
            return new ServiceException(result);
        }
        return exception;
    }
}
