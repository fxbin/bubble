package cn.fxbin.bubble.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * I18nMessageProvider
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/3/30 18:17
 */
@Component
public class I18nMessageProvider {

    @Resource
    private MessageSource messageSource;


    /**
     * getMessage
     *
     * @since 2020/3/30 18:17
     * @param key 对应messages配置的key.
     * @return java.lang.String
     */
    public String getMessage(String key){
        return getMessage(key, null);
    }


    /**
     * getMessage
     *
     * @since 2020/3/30 18:18
     * @param key ：对应messages配置的key.
     * @param args : 数组参数.
     * @return java.lang.String
     */
    public String getMessage(String key, Object[] args){
        return getMessage(key, args, "");
    }


    /**
     * getMessage
     *
     * @since 2020/3/30 18:18
     * @param key ：对应messages配置的key.
     * @param args : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return java.lang.String
     */
    public String getMessage(String key, Object[] args, String defaultMessage){
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }


    /**
     * getMessage
     *
     * @since 2020/3/30 18:18
     * @param key 对应messages配置的key.
     * @param args 数组参数.
     * @param defaultMessage 没有设置key的时候的默认值.
     * @param locale java.util.Locale
     * @return java.lang.String
     */
    public String getMessage(String key, Object[]args, String defaultMessage, Locale locale){
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }


}
