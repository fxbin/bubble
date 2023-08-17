package cn.fxbin.bubble.data.mybatis.handler;

import cn.fxbin.bubble.core.util.time.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * CustomizeMetaObjectHandler
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/11/23 10:53
 */
@Slf4j
@Component
public class CustomizeMetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", DateUtils::localDateTime, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", DateUtils::localDateTime, LocalDateTime.class);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", DateUtils::localDateTime, LocalDateTime.class);
    }
}
