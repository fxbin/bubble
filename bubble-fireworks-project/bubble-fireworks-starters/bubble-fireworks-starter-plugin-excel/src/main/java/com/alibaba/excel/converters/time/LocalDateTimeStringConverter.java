package com.alibaba.excel.converters.time;

import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import cn.fxbin.bubble.fireworks.core.util.time.DateUtils;
import com.alibaba.excel.annotation.format.LocalDateTimeFormat;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDateTime;

/**
 * timeLocalDateTimeStringConverter
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/4/7 12:48
 */
@SuppressWarnings("rawtypes")
public class LocalDateTimeStringConverter implements Converter<LocalDateTime> {
    @Override
    public Class supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDateTime convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        LocalDateTimeFormat annotation = contentProperty.getField().getAnnotation(LocalDateTimeFormat.class);
        return DateUtils.parseLocalDateTime(DateUtils.formatDateText(cellData.getStringValue()), ObjectUtils.isEmpty(annotation) ? DateUtils.NORM_DATETIME_PATTERN : annotation.value());
    }

    @Override
    public CellData convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        LocalDateTimeFormat annotation = contentProperty.getField().getAnnotation(LocalDateTimeFormat.class);
        return new CellData(DateUtils.format(value, ObjectUtils.isEmpty(annotation) ? DateUtils.NORM_DATETIME_MS_PATTERN : annotation.value()));
    }
}
