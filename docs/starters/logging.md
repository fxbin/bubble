# bubble-starter-logging

## 作用

- Web/Service 层日志增强：敏感头过滤、请求/响应体截断、慢方法监控。

## 配置示例（生产建议）

```yaml
bubble:
  logging:
    enabled: true
    web:
      enabled: true
      log-request-body: false
      log-response-body: false
      sensitive-headers:
        - authorization
        - cookie
        - x-auth-token
        - x-api-key
        - authentication
        - proxy-authorization
        - x-forwarded-for
        - x-real-ip
    service:
      enabled: true
      log-parameters: false
      log-return-value: false
      slow-method-threshold: 500
```

参考：`bubble-starters/bubble-starter-logging/CONFIGURATION.md`

