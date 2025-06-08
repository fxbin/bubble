# Bubble Starter Data Redis

这是一个基于Spring Boot的Redis集成模块，提供了Redis缓存、Redis Stream消息流等功能的自动配置。

## 功能特性

### 1. Redis缓存自动配置
- 自动配置Redis缓存管理器
- 支持自定义序列化方式
- 支持缓存前缀、TTL等配置
- 自动创建CacheManagerCustomizers Bean

### 2. Redis Template自动配置
- 提供`bfRedisTemplate`和`stringRedisTemplate`
- 自动配置序列化器
- 提供`RedisOperations`工具类，封装常用Redis操作
- 支持操作日志记录

### 3. Redis Stream支持
- 自动配置Stream消息监听容器
- 支持`@RStreamListener`注解进行消息监听
- 提供`RStreamOperations`工具类
- 支持消费者组和消费者名称自动配置

### 4. Session支持
- 集成Spring Session Data Redis
- 自动配置Session序列化器
- 支持云Redis服务

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-data-redis</artifactId>
    <version>2.0.0.BUILD-SNAPSHOT</version>
</dependency>
```

### 2. 配置Redis连接

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your-password
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 3. 配置缓存

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10分钟
      key-prefix: "app:"
      cache-null-values: false
    cache-names:
      - userCache
      - productCache
```

### 4. 配置Redis Stream（可选）

```yaml
bubble:
  data:
    redis:
      stream:
        enabled: true
        consumer-group: my-app-group
        consumer-name: consumer-1
        poll-batch-size: 10
        poll-timeout: 1000ms
```

## 使用示例

### 1. 使用缓存注解

```java
@Service
public class UserService {
    
    @Cacheable(value = "userCache", key = "#id")
    public User getUserById(Long id) {
        // 查询用户逻辑
        return userRepository.findById(id);
    }
    
    @CacheEvict(value = "userCache", key = "#user.id")
    public void updateUser(User user) {
        // 更新用户逻辑
        userRepository.save(user);
    }
}
```

### 2. 使用RedisOperations

```java
@Service
public class CacheService {
    
    @Autowired
    private RedisOperations redisOperations;
    
    public void setCache(String key, Object value, long timeout) {
        redisOperations.set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    public Object getCache(String key) {
        return redisOperations.get(key);
    }
}
```

### 3. 使用Redis Stream

```java
@Component
public class MessageListener {
    
    @RStreamListener(name = "order-stream")
    public void handleOrderMessage(Map<String, Object> message) {
        // 处理订单消息
        System.out.println("Received order message: " + message);
    }
}

@Service
public class MessageProducer {
    
    @Autowired
    private RStreamOperations streamOperations;
    
    public void sendOrderMessage(Order order) {
        streamOperations.add("order-stream", order);
    }
}
```

## 配置说明

### Redis Stream配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `bubble.data.redis.stream.enabled` | 是否启用Stream功能 | false |
| `bubble.data.redis.stream.consumer-group` | 消费者组名称 | 应用名+环境 |
| `bubble.data.redis.stream.consumer-name` | 消费者名称 | IP+端口 |
| `bubble.data.redis.stream.poll-batch-size` | 批量拉取大小 | - |
| `bubble.data.redis.stream.poll-timeout` | 拉取超时时间 | - |

## 注意事项

1. **依赖顺序**: 本模块会在Spring Boot的Redis自动配置之后执行
2. **Bean覆盖**: 如果你需要自定义某些Bean，可以通过`@Primary`注解或者条件注解来覆盖默认配置
3. **序列化**: 默认使用Jackson进行JSON序列化，如需自定义可以提供自己的`RedisSerializer` Bean
4. **Session**: Session功能是可选的，只有在classpath中存在相关依赖时才会启用
5. **Stream**: Stream功能需要显式启用，通过配置`bubble.data.redis.stream.enabled=true`

## 故障排除

### 1. CacheManagerCustomizers Bean找不到

如果遇到`CacheManagerCustomizers`相关的错误，请确保：
- 使用了正确的Spring Boot版本
- 本模块已经自动创建了该Bean，如果仍有问题，请检查自动配置是否正确加载

### 2. Redis连接问题

请检查：
- Redis服务是否正常运行
- 网络连接是否正常
- 认证信息是否正确
- 防火墙设置

### 3. Stream消息丢失

请检查：
- 消费者组是否正确配置
- 消费者是否正常启动
- Redis Stream是否存在
- 网络连接是否稳定