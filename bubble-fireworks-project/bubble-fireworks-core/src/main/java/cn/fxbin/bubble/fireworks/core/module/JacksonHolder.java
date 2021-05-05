package cn.fxbin.bubble.fireworks.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JacksonHolder
 *
 * @author fxbin
 * @version v1.0
 * @since 2021/4/20 15:18
 */
public interface JacksonHolder {

    ObjectMapper INSTANCE = new ObjectMapper().registerModule(new JavaTimeModule());

}
