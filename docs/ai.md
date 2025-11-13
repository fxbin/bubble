# AI 能力

## 模块结构

- `bubble-ai` 聚合模块
- `bubble-ai-starters/bubble-ai-starter` 基础 AI 启动器
- `bubble-ai-starters/bubble-ai-starter-lightrag` LightRAG 集成（Forest + WebFlux）

## 依赖引入（LightRAG）

```xml
<dependency>
  <groupId>cn.fxbin.bubble</groupId>
  <artifactId>bubble-ai-starter-lightrag</artifactId>
</dependency>
```

## 配置与资源

- `lightrag-forest.yaml`：Forest 客户端配置
- `lightrag-openapi.json`：LightRAG OpenAPI 说明

## 健康检查（可选）

- 依赖 `spring-boot-starter-actuator` 暴露健康端点

