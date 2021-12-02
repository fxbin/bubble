package cn.fxbin.bubble.fireworks.web.support;

import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * DefaultErrorController
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/8/16 13:06
 */
@RestController
public class DefaultErrorController extends BasicErrorController {

    public static ErrorProperties initProperties(){
        ErrorProperties properties = new ErrorProperties();
        properties.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        return properties;
    }

    public DefaultErrorController() {
        super(new DefaultErrorAttributes(), initProperties());
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }

        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        String path = (String) body.get("path");
        String error = (String) body.get("error");
        String errmsg = String.format("path %s %s", path, error);

        Map<String, Object> bodyResult = Maps.newHashMap();
        bodyResult.put("errcode", -1);
        bodyResult.put("errmsg", errmsg);
        return new ResponseEntity<>(bodyResult, status);
    }

}
