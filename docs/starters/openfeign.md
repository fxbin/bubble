# bubble-starter-openfeign

## 作用

- OpenFeign 客户端增强，支持 OkHttp3 连接管理、全局超时与日志级别，透传请求头。

## 关键配置

```yaml
bubble:
  feign:
    allowHeaders: ["X-Real-IP", "x-forwarded-for", "Authorization"]
    connectTimeout: 2000
    readTimeout: 2000
    loggingLevel: FULL
```

参考：`bubble-starters/bubble-starter-openfeign/src/main/java/cn/fxbin/bubble/openfeign/FeignProperties.java:33`

