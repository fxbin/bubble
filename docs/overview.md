# 项目概述

Bubble 致力于为企业级项目提供统一的微服务开发基座与最佳实践。通过模块化架构与丰富的 Starter，开发者可以快速集成 Web、安全、数据访问、RPC、国际化、日志、调度、AI 等能力。

## 目标与定位

- 面向微服务与模块化单体的统一开发框架
- 统一依赖版本与构建流程，降低升级与兼容成本
- 提供生产就绪的默认配置与治理能力

## 关键能力

- Starter 模块化能力覆盖主流技术栈
- 统一的响应模型与异常治理（`Result`/`ServiceException`）
- 统一的配置命名空间（如 `bubble.logging.*`, `bubble.feign.*`, `bubble.satoken.*`）
- 自动化发布与文档生成方案（OpenAPI + Javadoc 聚合 + Pages）

## 典型使用场景

- 快速搭建中后台服务：认证、权限、缓存、数据库、消息、任务调度
- 面向 AI 的服务能力：Prompt/LLM 集成、LightRAG 检索增强

