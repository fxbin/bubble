package cn.fxbin.bubble.plugin.dynamic.threadpool.handler;

/**
 * ConfigListener
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/7/8 14:22
 */
@FunctionalInterface
public interface ConfigListenerHandler {

   /**
    * refreshConfig 刷新配置
    *
    * @since 2020/7/8 14:33
    */
    void refreshConfig();



}
