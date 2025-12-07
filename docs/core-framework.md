# 核心库详解（bubble-core）

## 统一响应模型：Result

- 成功返回：`Result.success(data)`；失败返回：`Result.failure(errmsg)` 或基于 `ErrorCode`
- 判定与抛错：`Result.isSuccess(result)`、`Result.throwOnFail(result)`

示例：

```java
// 成功返回
return Result.success(userInfo);

// 失败返回（自定义文案）
return Result.failure("操作失败，请稍后重试");

// 校验后抛错
Result.throwOnFail(serviceCall());
```

参考：`bubble-core/src/main/java/cn/fxbin/bubble/core/dataobject/Result.java:136`

## Web 工具：WebUtils（性能优化版）

- 请求体安全读取与缓存：`getRequestBody(HttpServletRequest)`，避免重复读取导致异常与性能问题
- IP 获取与缓存：`getIpAddr(request)`，支持多代理头与本机降级
- 统一 Headers、User-Agent 获取、URL/Method 工具

参考：`bubble-core/src/main/java/cn/fxbin/bubble/core/util/WebUtils.java:212`

## 其他常用组件

- `JsonUtils`、`BeanUtils`、`CollectionUtils`、`StringUtils` 等工具
- `ServiceException`、`ErrorCode`、`GlobalErrorCode` 错误体系

