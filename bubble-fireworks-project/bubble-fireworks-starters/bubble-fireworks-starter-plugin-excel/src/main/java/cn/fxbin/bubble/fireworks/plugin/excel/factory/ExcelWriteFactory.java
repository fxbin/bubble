package cn.fxbin.bubble.fireworks.plugin.excel.factory;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteWorkbook;

/**
 * ExcelWriteFactory
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 13:13
 */
public class ExcelWriteFactory extends ExcelWriter {

    public ExcelWriteFactory(WriteWorkbook writeWorkbook) {
        super(writeWorkbook);
    }

}
