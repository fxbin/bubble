# 故障排查

- 日志未输出：检查 `bubble.logging.enabled`
- 请求体读取失败：是否被其他组件消费；使用 `WebUtils.getRequestBody` 的缓存机制
- 版本冲突：以 BOM 为准统一约束

