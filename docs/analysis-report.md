# 项目分析报告

## 项目架构与技术栈评估

- 架构形态：多模块 Maven 聚合工程，采用 Spring Modulith 进行模块化单体设计；核心模块包括 `bubble-core`、`bubble-starters/*`、`bubble-ai/*`
- 构建与版本：以 `bubble-dependencies` 作为 BOM 管理版本；父 POM 提供 Native/AOT、Spring Boot 打包、编译器与注解处理器统一配置
- 技术选型：
  - 框架：Spring Boot `3.5.7`、Spring Cloud `2025.0.0`、Spring Modulith `1.4.3`
  - 能力：Spring AI、Springdoc、MyBatis-Plus、Redis/Redisson、Elasticsearch、Dubbo、Sa-Token、XXL-Job、Hutool、Lombok、MapStruct、Forest、SOFA-Tracer
- 评估结论：版本栈前沿且稳定，BOM 管理良好；具备生产化所需核心能力，适合企业级微服务与模块化单体场景

## 核心功能模块分解

- `bubble-core`：统一响应（`Result`）、错误码与异常模型、Web 工具（请求体缓存/多代理 IP 缓存）、JSON/Jackson 配置、Yaml 工厂等
- `bubble-starters`：Web、数据访问（Redis/MyBatis-Plus/Elasticsearch）、RPC（OpenFeign/Dubbo）、认证授权（Sa-Token）、日志治理、分布式锁、邮件、Excel、国际化、任务调度、测试增强
- `bubble-ai`：AI 基础与 LightRAG 集成（Forest + WebFlux），提供资源配置与可选健康检查

## 当前开发进度与里程碑

- 文档化基础（M1）：已完成门户与核心章节，包含概述、架构、构建系统、技术栈、核心库、Starter 索引与子页
- 模块化详解（M2）：各 Starter 子页与 AI 能力文档已补充，引用到代码路径与示例
- API/自动化（M3）：提供 Springdoc UI 接入与 Javadoc 聚合方案，CI 发布流程文档已给出
- 性能与最佳实践（M4）：提供慢方法阈值与日志策略、原生镜像建议、排障与安全指南

## 关键依赖项与第三方服务

- 中间件：Redis（Redisson）、MySQL、Elasticsearch、XXL-Job 调度中心、Dubbo（含 Redis 注册扩展）
- 生态库：Springdoc、Sa-Token、MapStruct、Forest、SOFA-Tracer 等
- 管理策略：统一由 BOM 控制版本与兼容性；示例与文档均以 BOM 为权威

## 性能指标与优化空间

- 基线建议：
  - 日志模块：生产关闭请求/响应体记录，配置敏感头脱敏；慢方法阈值建议≥500ms
  - Web 工具：请求体缓存避免重复读取；IP 获取缓存，上限控制千级条目
  - 远程调用：OkHttp 连接池与超时设置（`bubble.feign.*`），合理重试策略
- 优化方向：
  - 原生镜像与 AOT：改善冷启动与内存占用；对计算密集型与高并发服务收益显著
  - 序列化：Jackson 统一配置与日期处理；MapStruct 降低反射成本
  - 资源治理：线程池与 TTL 异步上下文穿透（Sa-Token），避免上下文丢失

---

- 代码参考：
  - 启动事件监听输出：`bubble-starters/bubble-starter/src/main/java/cn/fxbin/bubble/lanuch/StartedEventListener.java:50`
  - 统一响应模型：`bubble-core/src/main/java/cn/fxbin/bubble/core/dataobject/Result.java:136`
  - 请求体安全读取：`bubble-core/src/main/java/cn/fxbin/bubble/core/util/WebUtils.java:212`
