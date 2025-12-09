# bubble-starter-dubbo

## 简介

`bubble-starter-dubbo` 是基于 Apache Dubbo 的微服务集成模块。它在官方 Dubbo Starter 的基础上，提供了更符合 Bubble 框架规范的默认配置与扩展能力。

## 功能特性

- **Dubbo RPC 集成**：开箱即用的 Dubbo 服务调用与暴露能力。
- **Redis 注册中心支持**：通过 `bubble-starter-dubbo-registry-redis` 模块，支持使用 Redis 作为轻量级注册中心，替代 Zookeeper/Nacos (适用于中小规模集群)。
- **全局异常处理**：集成了 `DubboProviderExceptionFilter` 和 `DubboConsumerExceptionFilter`，确保 RPC 调用异常能被正确序列化和传播，与 `bubble-core` 的 `Result` 模型无缝对接。
- **链路追踪**：默认集成 SOFATracer (需引入相关依赖) 支持 RPC 链路追踪。

## 快速开始

### 1. 引入依赖

基础 Dubbo 支持：

```xml
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-dubbo</artifactId>
</dependency>
```

如果使用 Redis 作为注册中心，需额外引入：

```xml
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-dubbo-registry-redis</artifactId>
</dependency>
```

### 2. 配置

在 `application.yml` 中配置 Dubbo：

```yaml
dubbo:
  application:
    name: ${spring.application.name}
  protocol:
    name: dubbo
    port: -1
  registry:
    # 使用 Redis 注册中心
    address: redis://${spring.data.redis.host}:${spring.data.redis.port}
  consumer:
    check: false
    timeout: 3000
    retries: -1
```

### 3. 定义服务接口

```java
public interface DemoService {
    String sayHello(String name);
}
```

### 4. 服务提供者

```java
@DubboService
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

### 5. 服务消费者

```java
@RestController
public class DemoController {
    
    @DubboReference
    private DemoService demoService;
    
    @GetMapping("/hello")
    public Result<String> hello(String name) {
        return Result.success(demoService.sayHello(name));
    }
}
```

## 异常处理机制

Bubble Dubbo 模块重写了异常过滤器：

1. **Provider 端**：捕获所有异常，如果是 `ServiceException`，则保留错误码；否则封装为 `INTERNAL_SERVER_ERROR`。确保返回给 Consumer 的始终是格式化的异常信息。
2. **Consumer 端**：接收 Provider 抛出的异常，重新抛出为本地的 `ServiceException`，使得上层 Controller 可以像处理本地异常一样处理 RPC 异常。
