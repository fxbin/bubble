package cn.fxbin.bubble.plugin.excel.listener;

import cn.fxbin.bubble.core.util.JsonUtils;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AbstractExcelListener
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 12:53
 */
@Slf4j
public abstract class AbstractExcelListener<M> extends AnalysisEventListener<M> {
    /**
     * 每隔3000条 执行一次（存储数据库|其它操作）, 然后清理list，方便内存回收
     */
    private static final int BATCH_COUNT = 3000;

    /**
     * 抛出异常之后是否继续读取， 默认为 true
     */
    private final boolean isContinueAfterThrowing = true;

    List<M> list = new ArrayList<>();

    /**
     * 存储数据库|其它操作，等等业务逻辑。。。
     */
    public abstract void doService();

    @Override
    public void invoke(M object, AnalysisContext context) {

        list.add(object);

        if (list.size() >= BATCH_COUNT) {
            doService();
            list = new ArrayList<>(BATCH_COUNT);
        }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        doService();
        list.clear();

        log.info("所有数据解析完成！");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {

        log.error("解析失败，是否继续解析下一行:[{}]， msg:[{}]", isContinueAfterThrowing, exception.getMessage());

        if(!isContinueAfterThrowing) {
            throw exception;
        }

        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            log.error("第 {} Sheet页，第{}行，第{}列解析异常",
                    excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex());
        }



    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        log.info("解析到一条头数据:{}", JsonUtils.toJson(headMap));
    }
}
