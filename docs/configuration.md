# 配置参考

## 命名空间约定

- Feign：`bubble.feign.*`
- Logging：`bubble.logging.*`
- Sa-Token：`bubble.satoken.*`（扩展），`sa-token.*`（原生）
- I18n：`spring.messages.*` 与 MVC 相关配置

## 示例汇总

```yaml
bubble:
  feign:
    allowHeaders: ["X-Real-IP", "x-forwarded-for", "Authorization"]
    connectTimeout: 2000
    readTimeout: 2000
    loggingLevel: FULL

  logging:
    enabled: true
    web:
      enabled: true
      log-request-body: false
      log-response-body: false
    service:
      enabled: true
      slow-method-threshold: 500

  satoken:
    enabled: true
    jwt:
      enabled: false
      jwt-mode: simple
    auth:
      include-urls: ["/**"]
      exclude-urls: ["/auth/login"]

sa-token:
  token-name: satoken
  timeout: 2592000
  jwt-secret-key: "请配置强秘钥"

spring:
  messages:
    basename: messages
```

