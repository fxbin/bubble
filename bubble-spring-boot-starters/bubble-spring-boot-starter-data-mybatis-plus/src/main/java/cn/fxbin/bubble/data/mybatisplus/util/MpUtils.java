package cn.fxbin.bubble.data.mybatisplus.util;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * MpUtils
 *
 * <p>
 *     mybatis-plus 辅助工具类
 * </p>
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/20 00:14
 */
@Slf4j
@UtilityClass
public class MpUtils {

    private static final String MYSQL_ESCAPE_CHARACTER = "`";

    /**
     * 获取表信息，获取不到报错提示
     *
     * @param entityClass 实体类
     * @return 对应表信息
     */
    public <T> TableInfo getTableInfo(Class<T> entityClass) {
        return Optional.ofNullable(TableInfoHelper.getTableInfo(entityClass)).
                orElseThrow(() ->
                        ExceptionUtils.mpe("error: can not find TableInfo from Class: \"%s\".",
                                entityClass.getName()));
    }


}