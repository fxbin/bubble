## 项目现状评估
- 项目类型：多模块 Maven 聚合工程，根模块 `bubble-build` 管理插件，`bubble-dependencies` 作为 BOM 统一版本，`bubble-parent` 管理构建插件与 Native Profile
- 版本信息：`2.0.0.BUILD-SNAPSHOT`；核心版本以 BOM 为准：Spring Boot `3.5.7`，Spring Cloud `2025.0.0`，Spring AI `1.0.3`，Spring Modulith `1.4.3`
- 模块结构：
  - 核心库：`bubble-core`（`Result` 响应包装、异常模型、枚举、`WebUtils`、`JsonUtils`、`BeanUtils` 等工具）
  - Starter 集合：`bubble-starters`（web、data-redis、data-mybatis-plus、data-elasticsearch、dubbo、openfeign、satoken、logging、lock、mail、excel、i18n、xxl-job、test）
  - AI 集成：`bubble-ai`（`bubble-ai-starter`、`bubble-ai-starter-lightrag`，使用 Forest、WebFlux 并提供 `lightrag-forest.yaml` 配置）
  - 运行与构建：原生镜像支持（GraalVM）、Jacoco、Javadoc、Release/Flatten、Git Commit 信息生成
- 现有文档：根 `README.md`、`CHANGELOG.md`、`docs/规范定义.md`、`bubble-starter-logging/CONFIGURATION.md`、`bubble-starter-satoken/README.md`
- 性能与工程实践：`WebUtils` 引入请求体缓存与 IP 缓存；日志模块提供请求/响应体截断与慢方法阈值；测试框架和 Jacoco 已配置

## 目标与交付物
- 完整的项目分析报告（架构、技术栈、核心模块、进度里程碑、依赖与第三方、性能指标与优化建议）
- 结构化文档体系（Markdown），与 DeepWiki 结构对齐：架构、构建系统、核心框架、Starter 模块
- 文档模板与编写规范（统一章节结构、示例、代码与配置块、Mermaid 图表规范）
- 自动化文档生成方案（OpenAPI 导出与 UI、Javadoc 聚合、CI 发布到 GitHub Pages）

## 文档目录与文件规划（将创建于 `docs/`）
- `index.md`：门户页（项目概述、特性列表、目录导航、版本徽章）
- `overview.md`：项目概述（定位、能力边界、典型使用场景、架构总览图）
- `architecture.md`：架构与设计（模块化单体、微服务治理、模块关系、启动流程与事件、依赖关系图）
- `build-system.md`：构建系统与依赖管理（聚合与分发、BOM 管理、父 POM 插件、Release/Nexus 流程、Native Profile）
- `technology-stack.md`：技术栈（以 BOM 为权威；各组件用途、替代方案与选型理由）
- `core-framework.md`：核心库详解（`bubble-core` 常用类与模式：`Result`、`ErrorCode`、`ServiceException`、`WebUtils` 等）
- `starter-modules.md`：Starter 索引（每个 Starter 链接到子页）
  - `starters/web.md`、`starters/data-redis.md`、`starters/data-mybatis-plus.md`、`starters/data-elasticsearch.md`、`starters/dubbo.md`、`starters/openfeign.md`、`starters/satoken.md`、`starters/logging.md`、`starters/lock.md`、`starters/mail.md`、`starters/excel.md`、`starters/i18n.md`、`starters/xxl-job.md`、`starters/test.md`
- `ai.md`：AI 能力（Spring AI 集成、LightRAG Starter 使用、Forest 调用、配置与健康检查）
- `installation.md`：安装与引入（JDK/Maven 环境、BOM 引入示例、必要/可选中间件）
- `quick-start.md`：快速开始（依赖引入、最小化应用示例、常用配置）
- `usage.md`：使用说明（常见场景食谱：认证、远程调用、缓存、任务调度、国际化、日志）
- `configuration.md`：统一配置参考（命名空间、默认值、生产建议）
- `api.md`：API 文档入口（Springdoc UI 与 JSON 导出说明、离线生成方案）
- `best-practices.md`：最佳实践（模块划分、事务与限流、幂等、异常与响应规范、日志与安全）
- `security.md`：安全指南（Sa-Token、JWT 模式、权限模型、敏感信息治理）
- `performance.md`：性能指标与优化（基线指标、压测建议、GC/Native、慢方法监控、网络与序列化）
- `troubleshooting.md`：故障排查（常见问题、诊断工具、日志级别、健康检查）
- `writing-standard.md`：文档编写规范（结构、术语、图表、代码样例规范）
- `templates/`：模板集合（分析报告模板、模块文档模板、API 与配置示例模板）
- 变更记录沿用根 `CHANGELOG.md`，`docs/changelog.md` 仅作为跳转页（如需要）

## 章节内容要点（示例）
- 安装与引入：
  - BOM 引入与示例依赖：`bubble-starter-web`、`bubble-starter-data-redis`、`bubble-starter-data-mybatis-plus`
  - 环境要求：JDK 17+、Maven 3.6+，可选 Redis/MySQL/Elasticsearch
- 核心库示例：
  - `Result` 的成功/失败用法、`throwOnFail` 使用范式
  - `WebUtils` 请求体安全读取与缓存、IP 获取与缓存策略、User-Agent/Headers 提取
- Starter 重点：
  - OpenFeign：`bubble.feign.*` 属性（超时、日志级别、透传头）；OkHttp 连接池说明
  - Logging：请求/响应体截断、敏感头过滤、慢方法阈值与环境建议（开发/测试/生产）
  - Sa-Token：原生与 Bubble 扩展配置、JWT 模式（simple/mixin/stateless）、TTL 异步上下文穿透、异常处理与常见注解
  - I18n：`CookieLocaleResolver`、`LocaleChangeInterceptor`、`spring.messages.basename` 管理
  - 数据访问：MyBatis-Plus BOM、动态数据源、Redis/Redisson 版本对齐
  - 任务调度：XXL-Job 集成要点与治理建议
- AI 能力：Spring AI 集成与 LightRAG Starter 使用、Forest DSL 与 WebFlux、健康检查与性能建议

## 图表与代码示例
- 使用 Mermaid 绘制：
  - 模块结构图（聚合→BOM→Parent→Core/Starters/AI）
  - Web 请求日志拦截与敏感头过滤流程
  - JWT 鉴权与 TTL 异步上下文传播序列图
- 代码块：
  - POM 依赖与配置样例
  - 常用配置 YAML（logging、feign、satoken、i18n）
  - 最小化 Controller + Starter 组合示例

## API 文档生成与聚合方案
- 运行时 UI：引入 `springdoc-openapi-starter-webmvc-ui`，暴露 `v3/api-docs` 与 `/swagger-ui.html`（或 `/swagger-ui/index.html`）
- 离线导出：在示例应用中通过 CI 任务导出 `openapi.json` 并产出 `api.md` 链接；可选集成 Redoc CLI 生成静态页面
- Javadoc 聚合：使用根 POM 的 `maven-javadoc-plugin` 聚合生成 JavaDoc，挂载到 `docs/site/javadoc` 并链接主页

## 自动化发布与 CI
- 复用现有 Maven 工作流，新增 Docs 发布工作流：
  - 步骤：构建示例应用 → 导出 OpenAPI → 运行 `mvn -q -DskipTests javadoc:aggregate` → 将 `docs/` 与产物发布到 GitHub Pages（`fxbin.github.io/bubble`）
  - 版本对齐：以 BOM 为准，徽章与文档版本自动更新（从 `pom.xml` 解析）

## 开发进度与里程碑
- M1（文档化基础）：搭建 `docs/` 框架、完成概述/安装/快速开始/技术栈/构建系统
- M2（模块化详解）：完成全部 Starter 子页与 `bubble-core` 深入文档
- M3（API/自动化）：接入 Springdoc 导出与 Javadoc 聚合，完成 CI 发布
- M4（性能与最佳实践）：压测基线与优化建议、故障排查与安全指南完善

## 关键依赖与第三方服务
- 中间件：Redis/Redisson、MySQL、Elasticsearch、XXL-Job、Dubbo（含 Redis 注册扩展）
- 生态：Spring Boot/Cloud/Modulith、Springdoc、Sa-Token、MapStruct、Forest、SOFA-Tracer 等
- 说明：依赖版本与兼容性均以 `bubble-dependencies` 为唯一真源，文档展示与示例统一引用 BOM 变量

## 性能指标与优化建议
- 运行基线：
  - 日志模块慢方法阈值（分环境建议）、请求/响应体长度截断策略
  - `WebUtils` 请求体缓存/避免重复读取；IP 获取缓存（上限控制）
- 优化空间：
  - 原生镜像（GraalVM）与 AOT 配置；日志敏感信息治理；OkHttp 连接池与 Feign 重试策略；序列化与内存占用控制

## 文档模板与编写规范
- 模板：模块页、配置参考页、API 页、分析报告页；统一包含：目的、安装、配置、示例、注意事项、最佳实践
- 规范：
  - 标题层级与目录导航一致；术语与命名统一（以 BOM/模块实际命名为准）
  - 示例代码与配置可复制粘贴即用；图表使用 Mermaid；链接相对路径
  - 示例严格遵循项目约定：日志用 `@Slf4j`、尽量使用 Lombok，公共方法示例给出注释与简单测试片段

## 验证与交付方式
- 文档语法校验（Markdown Lint）与链接有效性检查
- 示例工程编译校验（最小化应用 + 常用 Starter）
- CI 发布演练（Pages 站点预览）

## 对齐参考标准
- 结构与目录参考 DeepWiki：Architecture、Build System、Core Framework、Starter Modules
- 内容与版本信息以仓库 BOM/Parent 为准，确保现状一致性

——
准备好后将按上述结构在 `docs/` 目录生成所有文档文件与模板，并补充 CI 工作流以自动化发布。请确认以上计划。