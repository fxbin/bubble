# bubble-starter-satoken

## 作用

- 快速集成 Sa-Token，支持 JWT 模式（simple/mixin/stateless）、TTL 异步上下文穿透、统一异常处理与路由拦截。

## 原生与扩展配置

```yaml
sa-token:
  token-name: satoken
  timeout: 2592000
  jwt-secret-key: "请自行配置强秘钥"

bubble:
  satoken:
    enabled: true
    jwt:
      enabled: false
      jwt-mode: simple
    auth:
      include-urls: ["/**"]
      exclude-urls: ["/auth/login", "/doc.html/**", "/v3/api-docs/**"]
```

更多内容参见模块自带文档：`bubble-starters/bubble-starter-satoken/README.md`

