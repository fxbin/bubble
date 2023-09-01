package cn.fxbin.bubble.openfeign.codec;

import cn.fxbin.bubble.core.exception.ServiceException;
import cn.fxbin.bubble.core.dataobject.Result;
import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.StringUtils;
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
        String content = exception.contentUTF8();
        if (StringUtils.isNotBlank(content) && JsonUtils.isJsonString(content)) {
            Result<?> result = JsonUtils.parse(content, Result.class);
            return new ServiceException(result);
        }
        return exception;
    }
}
