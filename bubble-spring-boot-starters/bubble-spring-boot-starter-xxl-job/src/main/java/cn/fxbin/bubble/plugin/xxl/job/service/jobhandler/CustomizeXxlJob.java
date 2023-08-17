package cn.fxbin.bubble.plugin.xxl.job.service.jobhandler;

import cn.fxbin.bubble.core.util.JsonUtils;
import cn.fxbin.bubble.core.util.ObjectUtils;
import cn.fxbin.bubble.core.util.RunTimeUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CustomizeXxlJob
 *
 * 开发步骤：
 * 1、在Spring Bean实例中，开发Job方法，方式格式要求为 "public ReturnT<String> execute(String param)"
 * 2、为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/10/28 11:43
 */
@Slf4j
@Component
public class CustomizeXxlJob {

    private static final String INDEX_OF = "?";

    @XxlJob("curlJobHandler")
    public ReturnT<String> curlJobHandler(String param) {
        log.info("Curl Job Start,param is:{}", param);
        XxlJobHelper.log("Curl Job Start,param {}", param);
        if (ObjectUtils.isEmpty(param)) {
            XxlJobHelper.log("参数不能为空，请设置");
            return new ReturnT<>(ReturnT.FAIL_CODE, "参数不能为空，请设置");
        }

        // 处理换行符
        param = param.replace("\n","");

        long timestamp = System.currentTimeMillis();
        if(!param.contains(INDEX_OF)){
            param += "?timestamp="+timestamp;
        }else {
            param += "&timestamp="+timestamp;
        }

        String command = "curl ";
        String m = RunTimeUtils.getOptionValue("-m", param);
        if (ObjectUtils.isEmpty(m)) {
            command += "-m 1200 ";
        }

        String connectTimeout = RunTimeUtils.getOptionValue("--connect-timeout", param);
        if (ObjectUtils.isEmpty(connectTimeout)) {
            command += "--connect-timeout 1200 ";
        }

        command += param;

        String result = RunTimeUtils.exec(86400, command);
        XxlJobHelper.log("Curl Job End,param {},command {} result {}",param, command, JsonUtils.toJson(result));
        log.info("Curl Job End, Param {}, Command {} Result {}", param, command, JsonUtils.toJson(result));

        return new ReturnT<>(result);
    }

}
