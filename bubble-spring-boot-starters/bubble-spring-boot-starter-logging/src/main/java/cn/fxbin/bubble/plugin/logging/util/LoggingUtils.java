package cn.fxbin.bubble.plugin.logging.util;

import cn.fxbin.bubble.core.util.BeanUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.logging.model.BubbleFireworksLogging;
import com.aliyun.openservices.log.common.LogItem;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * LoggingUtils
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/19 15:30
 */
@UtilityClass
public class LoggingUtils {


    /**
     * generateLogItem
     *
     * @since 2020/5/19 15:31
     * @param fireworksLogging cn.fxbin.bubble.fireworks.logging.model.BubbleFireworksLogging
     * @return com.aliyun.openservices.log.common.LogItem
     */
    public LogItem generateLogItem(BubbleFireworksLogging fireworksLogging) {
        LogItem logItem = new LogItem();

        Map<String, Object> logMap = BeanUtils.object2Map(fireworksLogging);
        for (Map.Entry<String, Object> entry : logMap.entrySet()) {
            logItem.PushBack(entry.getKey(), StringUtils.utf8Str(entry.getValue()));
        }
        return logItem;
    }


}
