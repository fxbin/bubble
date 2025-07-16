# Bubble日志模块配置指南

## 概述

Bubble日志模块提供了强大而灵活的日志记录功能，支持Web层和Service层的自动日志记录。本文档详细介绍了所有可用的配置选项及其使用方法。

## 配置结构

```yaml
bubble:
  logging:
    enabled: true  # 全局日志开关
    web:           # Web层配置
      # Web层相关配置...
    service:       # Service层配置
      # Service层相关配置...
```

## Web层配置详解

### 基础配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.web.enabled` | boolean | true | 是否启用Web层日志记录 |
| `bubble.logging.web.ignore-urls` | List<String> | [] | 需要忽略的URL路径列表，支持Ant风格匹配 |

### 敏感信息过滤

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.web.sensitive-headers` | List<String> | ["authorization", "cookie", "x-auth-token", "x-api-key"] | 敏感请求头列表，这些头的值将被脱敏为[FILTERED] |

**示例配置：**
```yaml
bubble:
  logging:
    web:
      sensitive-headers:
        - "authorization"
        - "cookie"
        - "x-auth-token"
        - "x-api-key"
        - "authentication"
        - "proxy-authorization"
```

### 请求体和响应体记录

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.web.log-request-body` | boolean | true | 是否记录HTTP请求体内容 |
| `bubble.logging.web.log-response-body` | boolean | true | 是否记录HTTP响应体内容 |
| `bubble.logging.web.max-request-body-length` | int | 1000 | 请求体最大记录长度（字符数） |
| `bubble.logging.web.max-response-body-length` | int | 1000 | 响应体最大记录长度（字符数） |

**注意事项：**
- 请求体只会记录POST、PUT、PATCH等方法的内容
- 超过最大长度的内容会被截断并添加`...[TRUNCATED]`标识
- 生产环境建议关闭请求体和响应体记录以提升性能

## Service层配置详解

### 基础配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.service.enabled` | boolean | false | 是否启用Service层日志记录 |

### 参数和返回值记录

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.service.log-parameters` | boolean | true | 是否记录方法参数 |
| `bubble.logging.service.log-return-value` | boolean | true | 是否记录方法返回值 |
| `bubble.logging.service.max-parameter-length` | int | 500 | 方法参数最大记录长度（字符数） |
| `bubble.logging.service.max-return-value-length` | int | 500 | 方法返回值最大记录长度（字符数） |

### 慢方法监控

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `bubble.logging.service.slow-method-threshold` | long | 1000 | 慢方法执行时间阈值（毫秒） |

**慢方法特性：**
- 超过阈值的方法会被标记为`[SLOW]`
- 慢方法使用WARN级别记录日志
- 正常方法使用INFO级别记录日志
- 异常方法使用ERROR级别记录日志

## 环境配置建议

### 开发环境配置

```yaml
bubble:
  logging:
    enabled: true
    web:
      enabled: true
      log-request-body: true
      log-response-body: true
      max-request-body-length: 2000
      max-response-body-length: 2000
    service:
      enabled: true
      log-parameters: true
      log-return-value: true
      max-parameter-length: 1000
      max-return-value-length: 1000
      slow-method-threshold: 200  # 开发环境设置较低阈值便于性能调优
```

### 测试环境配置

```yaml
bubble:
  logging:
    enabled: true
    web:
      enabled: true
      log-request-body: true
      log-response-body: false  # 减少日志量
      max-request-body-length: 1000
    service:
      enabled: true
      log-parameters: true
      log-return-value: false  # 减少日志量
      slow-method-threshold: 500
```

### 生产环境配置

```yaml
bubble:
  logging:
    enabled: true
    web:
      enabled: true
      log-request-body: false  # 性能考虑
      log-response-body: false # 性能考虑
      sensitive-headers:
        - "authorization"
        - "cookie"
        - "x-auth-token"
        - "x-api-key"
        - "authentication"
        - "proxy-authorization"
        - "x-forwarded-for"
        - "x-real-ip"
    service:
      enabled: true
      log-parameters: false    # 减少日志量和敏感信息泄露
      log-return-value: false  # 减少日志量和敏感信息泄露
      slow-method-threshold: 500  # 更严格的性能监控
```

## 性能优化建议

### 1. 合理设置内容长度限制
- 根据实际需求设置`max-request-body-length`和`max-response-body-length`
- 避免设置过大的值，以免影响日志性能

### 2. 生产环境优化
- 关闭请求体和响应体记录：`log-request-body: false`、`log-response-body: false`
- 关闭Service层参数记录：`log-parameters: false`、`log-return-value: false`
- 设置合理的慢方法阈值进行性能监控

### 3. 敏感信息保护
- 配置完整的敏感头列表
- 避免在参数和返回值中记录敏感信息
- 定期审查日志内容确保安全性

## 日志格式说明

### Web层日志格式
```json
{
  "serviceName": "your-service",
  "traceId": "trace-id",
  "spanId": "span-id",
  "eventName": "[WEB] ControllerName.methodName",
  "requestBody": {
    "method": "POST",
    "uri": "/api/users",
    "url": "http://localhost:8080/api/users",
    "parameters": {},
    "requestBody": "request content...",
    "methodArgs": []
  },
  "responseBody": "response content...",
  "costTime": 150,
  "clientIp": "127.0.0.1",
  "userAgent": "Mozilla/5.0...",
  "requestHeaders": {
    "content-type": "application/json",
    "authorization": "[FILTERED]"
  },
  "responseHeaders": {
    "content-type": "application/json"
  },
  "httpStatus": 200
}
```

### Service层日志格式
```json
{
  "serviceName": "your-service",
  "traceId": "trace-id",
  "spanId": "span-id",
  "eventName": "[SLOW] [SPRING_SERVICE] UserService.createUser",
  "requestBody": "method parameters...",
  "responseBody": "method return value...",
  "costTime": 1500
}
```

## 故障排查

### 常见问题

1. **日志没有输出**
   - 检查`bubble.logging.enabled`是否为true
   - 检查对应层级的enabled配置
   - 确认日志级别配置正确

2. **请求体读取失败**
   - 确认请求方法为POST/PUT/PATCH
   - 检查是否有其他组件已经消费了InputStream

3. **性能问题**
   - 检查内容长度限制设置
   - 考虑关闭请求体/响应体记录
   - 调整慢方法阈值

### 调试配置

```yaml
logging:
  level:
    cn.fxbin.bubble.plugin.logging: DEBUG
```

## 更新日志

### v2.0.0
- ✅ 新增动态敏感头过滤配置
- ✅ 新增请求体/响应体记录控制
- ✅ 新增内容长度限制功能
- ✅ 新增Service层参数/返回值记录控制
- ✅ 新增慢方法检测和标识
- ✅ 优化日志级别策略
- ✅ 提供完整的配置示例和文档