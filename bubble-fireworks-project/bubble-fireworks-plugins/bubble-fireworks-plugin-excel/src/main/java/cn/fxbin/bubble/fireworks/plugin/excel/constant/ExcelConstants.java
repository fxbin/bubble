package cn.fxbin.bubble.fireworks.plugin.excel.constant;

/**
 * ExcelConstants
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 12:47
 */
public interface ExcelConstants {


    /**
     * 每个sheet存储的记录数 100W
     */
    Integer PER_SHEET_ROW_COUNT = 1000000;

    /**
     * 每次向EXCEL写入的记录数(查询每页数据大小) 20W
     */
    Integer PER_WRITE_ROW_COUNT = 200000;


}
