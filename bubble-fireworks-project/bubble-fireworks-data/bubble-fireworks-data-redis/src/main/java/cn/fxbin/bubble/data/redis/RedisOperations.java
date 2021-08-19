package cn.fxbin.bubble.data.redis;

import cn.fxbin.bubble.fireworks.core.logging.LoggerMessageFormat;
import cn.fxbin.bubble.fireworks.core.util.CollectionUtils;
import cn.fxbin.bubble.fireworks.core.util.ObjectUtils;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.sync.RedisHashCommands;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RedisOperatorTools
 *
 * @author fxbin
 * @version v1.0
 * @since 2019/11/18 0:31
 */
@Slf4j
@Component
public class RedisOperations {

    @Getter
    @Resource
    private RedisTemplate redisTemplate;


    /**
     * delete 删除键
     *
     * @author fxbin
     * @since 2019/11/21 16:49
     * @param key 键
     * @return boolean 删除成功/失败
     */
    public boolean delete(@NonNull String key) {
        return redisTemplate.delete(key);
    }


    /**
     * delete 删除多个key
     *
     * @author fxbin
     * @since 2019/11/21 16:50
     * @param keys 键 一个或多个
     */
    public void delete(String... keys) {
        if (ObjectUtils.isNotEmpty(keys)) {
            if (keys.length == 1) {
                redisTemplate.delete(keys[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(keys));
            }
        }

    }


    /**
     * expire 指定缓存失效时间
     *
     * @author fxbin
     * @since 2019/11/21 17:21
     * @param key 键
     * @param time 时间(秒)
     * @return boolean 成功/失败
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * expireAt 指定缓存失效时间
     *
     * @author fxbin
     * @since 2019/11/21 17:22
     * @param key 键
     * @param date 时间
     * @return boolean
     */
    public boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }


    /**
     * getExpire 根据key值获取过期时间
     *
     * @author fxbin
     * @since 2019/11/21 17:24
     * @param key 键
     * @return long 时间(秒) 返回 0 代表永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    /**
     * hasKey 判断key是否存在
     *
     * @author fxbin
     * @since 2019/11/21 17:24
     * @param key 键
     * @return boolean 存在/不存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * get 获取value值
     *
     * @author fxbin
     * @since 2019/11/21 17:25
     * @param key 键
     * @return java.lang.Object 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }


    /**
     * set 设置值
     *
     * @author fxbin
     * @since 2019/11/21 17:26
     * @param key 键
     * @param value 值
     * @return boolean 成功/失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * set 设置值 并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/21 17:26
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time &gt; 0, 则设置time, 否则无限期
     * @return 成功/失败
     */
    public boolean set(String key, Object value, long time) {
        return set(key, value, time, TimeUnit.SECONDS);
    }


    /**
     * set 设置值 并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/21 17:29
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time &gt; 0, 则设置time, 否则无限期
     * @param timeUnit timeUnit
     * @return boolean 成功/失败
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if(time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
                return true;
            } else {
                return set(key, value);
            }
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * incr 递增(1)
     *
     * @author fxbin
     * @since 2019/11/21 17:32
     * @param key 键
     * @return long 递增之后的返回值 long
     */
    public long incr(String key) {
        return incr(key, 1);
    }


    /**
     * incr
     *
     * @author fxbin
     * @since 2019/11/21 17:33
     * @param key the key
     * @param delta the increment type: long
     * @return Long integer-reply the value of {@code key} after the increment
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RedisException("递增因子 [{}] 必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }


    /**
     * decr
     *
     * @author fxbin
     * @since 2019/11/21 17:35
     * @param key 键
     * @return long 递减之后的返回值 long
     */
    public long decr(String key) {
        return decr(key, 1);
    }


    /**
     * decr 递减
     *
     * @author fxbin
     * @since 2019/11/21 17:35
     * @param key the key
     * @param delta the decrement type: long
     * @return Long integer-reply the value of {@code key} after the decrement
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RedisException("递减因子 [{}] 必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, -delta);
    }


    /**
     * HashGet, 获取一组Map的键值对
     *
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return 值
     * @see RedisHashCommands
     */
    public Object hget(@NonNull String key, @NonNull String item) {
        return redisTemplate.opsForHash().get(key, item);
    }


    /**
     * hset
     *
     * @author fxbin
     * @since 2019/11/21 17:44
     * @param key 键
     * @param item 项
     * @param value 值
     * @return boolean 成功/失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * hset 向一张hash表中放入数据，如果不存在将创建并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/21 17:44
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 过期时间
     * @return boolean 成功/失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            boolean flag = hset(key, item, value);
            if(time > 0 && flag) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * hdel
     *
     * @author fxbin
     * @since 2019/11/22 11:37
     * @param key 键
     * @param items 项, 可以是多个, 不为null
     */
    public void hdel(String key, @NonNull Object... items) {
        redisTemplate.opsForHash().delete(key, items);
    }


    /**
     * hmget 获取hashKey 对应的所有键值
     *
     * @author fxbin
     * @since 2019/11/26 13:35
     * @param key 键
     * @return java.util.Map
     */
    public Map<?, ?> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * hmset HashSet 添加一个Map类型值
     *
     * @author fxbin
     * @since 2019/11/26 13:36
     * @param key 键
     * @param map map 值
     * @return boolean
     */
    public boolean hmset(String key, Map<?, ?> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * hmset HashSet 并设置时间
     *
     * @author fxbin
     * @since 2019/11/26 14:14
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return boolean
     */
    public boolean hmset(String key, Map<?, ?> map, long time) {

        try{
            boolean flag = hmset(key, map);
            if(time > 0 && flag) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * hHasKey 判断 hash 表中是否有该项的值
     *
     * @author fxbin
     * @since 2019/11/26 14:13
     * @param key 键
     * @param item 项
     * @return boolean
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }


    /**
     * hincr 递增, 如果不存在, 就会创建一个 并把新增后的值返回
     *
     * @author fxbin
     * @since 2019/11/26 14:13
     * @param key 键
     * @param item 项
     * @param by 要增加几( 大于0 )
     * @return double 新增后的返回值
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }


    /**
     * hdecr hash 递减
     *
     * @author fxbin
     * @since 2019/11/26 14:12
     * @param key 键
     * @param item 项
     * @param by 要减少的数 (大于0)
     * @return double 递减之后的返回值
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }


    /**
     * sGet 根据 key 获取Set中的值
     *
     * @author fxbin
     * @since 2019/11/26 14:11
     * @param key 键
     * @return java.util.Set
     */
    public Set<Object> sGet(String key) {
        try {
            
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return null;
        }
    }


    /**
     * sHasKey 根据value从一个set中查询，是否存在
     *
     * @author fxbin
     * @since 2019/11/26 14:11
     * @param key 键
     * @param value 值
     * @return boolean
     */
    public boolean sHasKey(String key, Object value) {
        try {
            
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * sSet 设置一个set
     *
     * @author fxbin
     * @since 2019/11/26 14:11
     * @param key 键
     * @param values 值(可以多个)
     * @return long
     */
    public long sSet(String key, Object... values) {
        try {
            
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }


    /**
     * sSetAndTime 设置set, 并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/26 14:10
     * @param key 键
     * @param time 时间(秒)
     * @param values 值(可以多个)
     * @return long
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try{
            long nums = sSet(key, values);
            if (time > 0 && nums > 0) {
                expire(key, time);
            }
            return nums;
        }catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }


    /**
     * sGetSetSize 获取 Set 长度
     *
     * @author fxbin
     * @since 2019/11/26 14:10
     * @param key 键
     * @return long
     */
    public long sGetSetSize(String key) {
        try {
            
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }


    /**
     * setRemove 移除指定key 的缓存
     *
     * @author fxbin
     * @since 2019/11/26 14:10
     * @param key 键
     * @param values 值(可多个)
     * @return long
     */
    public long setRemove(String key, Object... values) {
        try {
            
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }


    /**
     * lGet 获取 list
     *
     * @author fxbin
     * @since 2019/11/26 14:09
     * @param key key
     * @param start 开始
     * @param end 结束, 0 到 -1 表示所有值
     * @return java.util.List
     */
    public <T> List<T> lGet(String key, long start, long end) {
        try {
            
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return null;
        }
    }


    /**
     * lGetListSize 获取list长度
     *
     * @author fxbin
     * @since 2019/11/26 14:09
     * @param key 键
     * @return long
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }


    /**
     * lGetIndex 通过索引，获取list值
     *
     * @author fxbin
     * @since 2019/11/26 14:09
     * @param key 键
     * @param index 索引值, index &gt;%3D 0, 0 表头, 1 第二个元素, ...; index &lt;%3D 0, -1 表尾, -2 倒数第二个元素, ...
     * @return java.lang.Object
     */
    public Object lGetIndex(String key, long index) {
        try {
            
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return null;
        }
    }


    /**
     * lPush 将list存入
     *
     * @author fxbin
     * @since 2019/11/26 14:08
     * @param key 键
     * @param value 值
     * @return boolean
     */
    public <T> boolean lPush(String key, T value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * lPush 将list存入，并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/26 14:08
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return boolean
     */
    public <T> boolean lPush(String key, T value, long time) {
        try {
            boolean flag = lPush(key, value);
            if(time > 0 && flag) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * lPushAll 将list 存入
     *
     * @author fxbin
     * @since 2019/11/26 14:07
     * @param key 键
     * @param value 值
     * @return boolean
     */
    public boolean lPushAll(String key, List<?> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * lPushAll 将list存入, 并设置过期时间
     *
     * @author fxbin
     * @since 2019/11/26 14:07
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return boolean
     */
    public boolean lPushAll(String key, List<?> value, long time) {
        try {
            boolean flag = lPushAll(key, value);
            if(time > 0 && flag) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * lUpdateIndex 根据索引修改list中的某条数据
     *
     * @author fxbin
     * @since 2019/11/26 13:38
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return boolean
     */
    public <T> boolean lUpdateIndex(String key, long index, T value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return false;
        }
    }


    /**
     * lRemove 移除N个值为value
     *
     * @author fxbin
     * @since 2019/11/26 13:38
     * @param key 键
     * @param count 移除数量
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error(LoggerMessageFormat.format("{} error",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            return 0;
        }
    }
}
