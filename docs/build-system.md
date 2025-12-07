# 构建系统与依赖管理

## 聚合与分发

- `bubble-build`：根聚合与统一插件管理（发布、签名、Javadoc、Jacoco、Versions、Git Commit 信息等）
- `bubble-dependencies`：BOM，统一管理所有三方与生态版本，作为唯一真源
- `bubble-parent`：父 POM，管理构建插件与 Native Profile

## 版本对齐（示例）

- Spring Boot：`3.5.7`
- Spring Cloud：`2025.0.0`
- Spring AI：`1.0.3`
- Spring Modulith：`1.4.3`
- Lombok：`1.18.42`
- Hutool：`5.8.41`

以上以 `bubble-dependencies/pom.xml` 中的属性为准。

## 发布流程与插件

- 发布签名：`maven-gpg-plugin`
- 版本打包：`maven-release-plugin` + `flatten-maven-plugin`
- 站点与文档：`maven-javadoc-plugin`（聚合/非聚合）、`jacoco-maven-plugin`（覆盖率）
- 原生镜像：`org.graalvm.buildtools:native-maven-plugin` 与 Spring AOT 支持（`bubble-parent/pom.xml:96`）

## 仓库与分发

- Snapshots：Sonatype OSSRH
- Releases：Sonatype Staging → Maven Central

