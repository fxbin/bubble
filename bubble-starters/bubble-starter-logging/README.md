# Bubble日志模块 (bubble-starter-logging)

## 简介

Bubble日志模块是一个基于Spring Boot的自动配置日志记录组件，提供了Web层和Service层的自动日志记录功能。通过AOP技术实现无侵入式的日志记录，支持丰富的配置选项和敏感信息过滤。

## 核心特性

### 🌐 Web层日志记录
- ✅ HTTP请求/响应自动记录
- ✅ 请求头/响应头记录与敏感信息过滤
- ✅ 请求体/响应体内容记录（可配置开关）
- ✅ 内容长度限制和截断
- ✅ 客户端IP和User-Agent记录
- ✅ URL路径忽略配置
- ✅ 执行时间统计

### 🔧 Service层日志记录
- ✅ 业务方法执行日志记录
- ✅ 方法参数和返回值记录（可配置开关）
- ✅ 参数/返回值内容长度限制
- ✅ 慢方法检测和标识
- ✅ 异常信息记录
- ✅ 执行时间统计

### 🛡️ 安全特性
- ✅ 敏感请求头自动过滤
- ✅ 可配置的敏感头列表
- ✅ 内容截断防止日志过大
- ✅ 支持生产环境优化配置

### ⚡ 性能特性
- ✅ 慢方法监控和告警
- ✅ 可配置的日志记录开关
- ✅ 内容长度限制减少性能影响
- ✅ 支持按环境差异化配置

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-logging</artifactId>
    <version>${bubble.version}</version>
</dependency>
```

### 2. 基础配置

```yaml
bubble:
  logging:
    enabled: true  # 启用日志功能
    web:
      enabled: true  # 启用Web层日志
    service:
      enabled: true  # 启用Service层日志
```

### 3. 使用示例

#### Web层自动记录
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        // 方法执行会自动记录HTTP请求/响应日志
        return ResponseEntity.ok(userService.createUser(request));
    }
}
```

#### Service层自动记录
```java
@Service
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        // 方法执行会自动记录参数、返回值和执行时间
        // 如果执行时间超过阈值，会标记为慢方法
        return userRepository.save(new User(request));
    }
}
```

## 详细配置

### Web层配置

```yaml
bubble:
  logging:
    web:
      enabled: true                    # 是否启用Web层日志
      ignore-urls:                     # 忽略的URL路径
        - "/health"
        - "/actuator/**"
      sensitive-headers:               # 敏感请求头列表
        - "authorization"
        - "cookie"
        - "x-auth-token"
      log-request-body: true           # 是否记录请求体
      log-response-body: true          # 是否记录响应体
      max-request-body-length: 1000    # 请求体最大记录长度
      max-response-body-length: 1000   # 响应体最大记录长度
```

### Service层配置

```yaml
bubble:
  logging:
    service:
      enabled: true                    # 是否启用Service层日志
      log-parameters: true             # 是否记录方法参数
      log-return-value: true           # 是否记录返回值
      max-parameter-length: 500        # 参数最大记录长度
      max-return-value-length: 500     # 返回值最大记录长度
      slow-method-threshold: 1000      # 慢方法阈值(毫秒)
```

## 环境配置建议

### 开发环境
```yaml
bubble:
  logging:
    web:
      log-request-body: true
      log-response-body: true
      max-request-body-length: 2000
    service:
      log-parameters: true
      log-return-value: true
      slow-method-threshold: 200  # 更低的阈值便于性能调优
```

### 生产环境
```yaml
bubble:
  logging:
    web:
      log-request-body: false     # 关闭以提升性能
      log-response-body: false    # 关闭以提升性能
    service:
      log-parameters: false       # 关闭以减少敏感信息泄露
      log-return-value: false     # 关闭以减少敏感信息泄露
      slow-method-threshold: 500  # 更严格的性能监控
```

## 日志格式

### Web层日志示例
```json
{
  "serviceName": "user-service",
  "traceId": "1234567890abcdef",
  "eventName": "[WEB] UserController.createUser",
  "requestBody": {
    "method": "POST",
    "uri": "/api/users",
    "requestBody": "{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}",
    "parameters": {}
  },
  "responseBody": "{\"id\":1,\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}",
  "costTime": 150,
  "clientIp": "192.168.1.100",
  "requestHeaders": {
    "content-type": "application/json",
    "authorization": "[FILTERED]"
  },
  "httpStatus": 200
}
```

### Service层日志示例
```json
{
  "serviceName": "user-service",
  "traceId": "1234567890abcdef",
  "eventName": "[SLOW] [SPRING_SERVICE] UserService.createUser",
  "requestBody": "CreateUserRequest(name=张三, email=zhangsan@example.com)",
  "responseBody": "User(id=1, name=张三, email=zhangsan@example.com)",
  "costTime": 1500
}
```

## 慢方法监控

当方法执行时间超过配置的阈值时，会自动标记为慢方法：
- 事件名称添加`[SLOW]`前缀
- 使用WARN级别记录日志
- 便于监控系统告警和性能分析

## 敏感信息保护

模块提供多层次的敏感信息保护：
1. **请求头过滤**：配置的敏感头值会被替换为`[FILTERED]`
2. **内容截断**：超长内容自动截断防止敏感信息泄露
3. **可配置开关**：生产环境可关闭详细内容记录

## 性能优化

### 配置优化建议
1. 生产环境关闭请求体/响应体记录
2. 设置合理的内容长度限制
3. 根据业务需求调整慢方法阈值
4. 配置忽略URL减少不必要的日志

### 监控指标
- 慢方法执行次数和平均耗时
- 日志记录的性能影响
- 敏感信息过滤效果

## 文档链接

- [详细配置指南](./CONFIGURATION.md)
- [配置示例文件](./src/main/resources/application-logging-example.yml)

## 版本历史

### v2.0.0 (Latest)
- ✅ 新增动态敏感头过滤配置
- ✅ 新增请求体/响应体记录控制
- ✅ 新增内容长度限制功能
- ✅ 新增Service层参数/返回值记录控制
- ✅ 新增慢方法检测和标识
- ✅ 优化日志级别策略
- ✅ 提供完整的配置文档和示例

### v1.0.0
- ✅ 基础Web层和Service层日志记录
- ✅ 基本的敏感信息过滤
- ✅ 执行时间统计

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。在提交代码前，请确保：
1. 代码符合项目的编码规范
2. 添加必要的单元测试
3. 更新相关文档

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。