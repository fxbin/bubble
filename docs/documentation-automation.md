# 文档自动化与发布方案

## OpenAPI 文档

- 运行时：集成 `springdoc-openapi-starter-webmvc-ui`，提供交互式 UI 与 `v3/api-docs` JSON
- 离线：在示例应用 CI 中导出 `openapi.json` 并随 Pages 发布

## JavaDoc 聚合

- 使用根 POM 的 `maven-javadoc-plugin`（已配置聚合与非聚合 reportSets）
- 执行：`mvn -q -DskipTests javadoc:aggregate`
- 发布到 `docs/site/javadoc/`，由 Pages 提供静态访问

## CI 工作流建议

- 触发：push tag 或 `main` 分支
- 步骤：构建 → 生成 OpenAPI → 生成 Javadoc → 将 `docs/` 与产物发布到 `gh-pages`
- 注意：版本信息从 BOM 解析更新徽章与页面

## Pages 站点结构

- 门户：`docs/index.md`
- 导航：保持与目录一致，提供到 API/Javadoc 的外链

