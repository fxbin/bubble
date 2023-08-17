package cn.fxbin.bubble.plugin.excel.support;

import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * ExcelParameter
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 14:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExcelModel<T> implements Serializable {
    private static final long serialVersionUID = -412069164101623189L;

    /**
     * Excel Name|File Name
     */
    private String name;

    /**
     * 文件密码
     */
    private String password;

    /**
     * 文件类型 (xlsx, xls)
     */
    private ExcelTypeEnum suffix;

    /**
     * Excel Sheet Name,支持多个
     */
    private String[] sheet;

    /**
     * 内存操作，默认false
     */
    private boolean inMemory = false;

    /**
     * Excel Data List
     */
    private List<T> data;

    /**
     * Excel Data Model Class
     */
    private Class<T> dataModelClass;

    /**
     * 包含字段
     */
    private Set<String> includeColumnFiledNames;

    /**
     * 排除字段
     */
    private Set<String> excludeColumnFiledNames;

}
