# bubble 🎉🎉🎉

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/63f51f8ee55f42bd8284c1c04e2b6f7d)](https://app.codacy.com/manual/fxbin/bubble?utm_source=github.com&utm_medium=referral&utm_content=fxbin/bubble&utm_campaign=Badge_Grade_Settings)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.0-brightgreen.svg)](https://github.com/spring-projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/SpringCloud-2025.0.0-brightgreen.svg)](https://github.com/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/SpringCloudAlibaba-2023.0.3.3-brightgreen.svg)](https://github.com/alibaba/spring-cloud-alibaba)
[![Spring AI](https://img.shields.io/badge/SpringAI-1.0.0-blue.svg)](https://spring.io/projects/spring-ai)
[![Version](https://img.shields.io/badge/Version-2.0.0.BUILD--SNAPSHOT-red.svg)](https://github.com/fxbin/bubble)

[![Star](https://img.shields.io/github/stars/fxbin/bubble.svg?label=Stars&style=social)](https://github.com/fxbin/bubble/stargazers)
[![Members](https://img.shields.io/github/forks/fxbin/bubble.svg?label=Fork&style=social)](https://github.com/fxbin/bubble/network/members)
[![Watchers](https://img.shields.io/github/watchers/fxbin/bubble.svg?label=Watch&style=social)](https://github.com/fxbin/bubble/watchers)

## 项目简介

`bubble` 是一个基于 Spring Boot 3.x 和 Spring Cloud 2025.x 的现代化微服务开发框架，旨在为企业级项目快速开发提供一系列的基础能力和最佳实践。项目采用模块化设计，支持 Java 17+ 和云原生架构，集成了 Spring AI、Spring Modulith 等前沿技术，方便使用者根据项目需求快速进行功能拓展。

### 🚀 核心特性

- **现代化技术栈**: 基于 Spring Boot 3.5.0、Spring Cloud 2025.0.0、Java 17
- **AI 集成**: 内置 Spring AI 1.0.0 支持，轻松构建智能化应用
- **模块化架构**: 采用 Spring Modulith 实现模块化单体架构
- **云原生支持**: 完整的微服务治理能力，支持容器化部署
- **开箱即用**: 提供丰富的 Starter 模块，快速集成常用功能
- **生产就绪**: 内置监控、日志、安全等生产级特性
- **性能优化**: 针对高并发场景进行深度优化

### 📦 模块结构

```
bubble/
├── bubble-core/                    # 核心工具库
├── bubble-dependencies/             # 依赖管理 BOM
├── bubble-parent/                   # 父级 POM
└── bubble-starters/                 # Starter 模块集合
    ├── bubble-starter/              # 基础 Starter
    ├── bubble-starter-web/          # Web 开发 Starter
    ├── bubble-starter-data-redis/   # Redis 集成 Starter
    ├── bubble-starter-data-mybatis-plus/ # MyBatis Plus Starter
    ├── bubble-starter-data-elasticsearch/ # Elasticsearch Starter
    ├── bubble-starter-dubbo/        # Dubbo 微服务 Starter
    ├── bubble-starter-openfeign/    # OpenFeign 客户端 Starter
    ├── bubble-starter-satoken/      # Sa-Token 权限认证 Starter
    ├── bubble-starter-logging/      # 日志增强 Starter
    ├── bubble-starter-lock/         # 分布式锁 Starter
    ├── bubble-starter-mail/         # 邮件发送 Starter
    ├── bubble-starter-excel/        # Excel 处理 Starter
    ├── bubble-starter-i18n/         # 国际化 Starter
    ├── bubble-starter-xxl-job/      # XXL-Job 任务调度 Starter
    └── bubble-starter-test/         # 测试增强 Starter
```

### 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 基础运行环境 |
| Spring Boot | 3.5.0 | 应用框架 |
| Spring Cloud | 2025.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.3.3 | 阿里云微服务套件 |
| Spring AI | 1.0.0 | AI 集成框架 |
| Spring Modulith | 1.0.0 | 模块化架构 |
| MyBatis Plus | 3.5.3.2 | ORM 框架 |
| Redis | - | 缓存中间件 |
| Elasticsearch | 7.13.4 | 搜索引擎 |
| Dubbo | 3.2.7 | RPC 框架 |
| Sa-Token | 1.43.0 | 权限认证框架 |
| XXL-Job | 2.4.2 | 分布式任务调度 |
| Hutool | 5.8.38 | Java 工具库 |
| Lombok | 1.18.38 | 代码简化工具 |

### 🎯 快速开始

#### 1. 环境要求

- JDK 17+
- Maven 3.6+
- Redis 6.0+（可选）
- MySQL 8.0+（可选）

#### 2. 依赖引入

在项目的 `pom.xml` 中添加依赖管理：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.fxbin.bubble</groupId>
            <artifactId>bubble-dependencies</artifactId>
            <version>2.0.0.BUILD-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 3. 使用 Starter

```xml
<!-- Web 开发 -->
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-web</artifactId>
</dependency>

<!-- Redis 缓存 -->
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-data-redis</artifactId>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>cn.fxbin.bubble</groupId>
    <artifactId>bubble-starter-data-mybatis-plus</artifactId>
</dependency>
```

### 📚 文档

- [快速开始指南](docs/quick-start.md)
- [模块使用说明](docs/modules.md)
- [最佳实践](docs/best-practices.md)
- [API 文档](docs/api.md)

### 🔄 版本说明

当前版本：`2.0.0.BUILD-SNAPSHOT`

- 全面升级至 Spring Boot 3.x 和 Spring Cloud 2025.x
- 支持 Java 17+ 和 GraalVM 原生镜像
- 集成 Spring AI 和 Spring Modulith
- 性能优化和安全增强
- 完善的云原生支持

所有 JAR 包都已推送至 Maven 中央仓库，每个版本的详细更新日志请查看 [CHANGELOG.md](CHANGELOG.md)

### 🤝 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 🐛 Bug 报告和修复
- ✨ 新功能建议和实现
- 📝 文档改进
- 🎨 代码优化
- 🧪 测试用例补充

#### 贡献流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

#### 开发规范

- 遵循 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- 使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范提交信息
- 确保代码覆盖率不低于 80%
- 所有 Public API 必须有完整的 Javadoc

### 📋 更新记录

详细的版本更新记录请查看：[CHANGELOG.md](CHANGELOG.md)

### 🔗 相关链接

- **官方文档**: [https://fxbin.github.io/bubble](https://fxbin.github.io/bubble)
- **示例项目**: [bubble-examples](https://github.com/fxbin/bubble-examples)
- **问题反馈**: [GitHub Issues](https://github.com/fxbin/bubble/issues)
- **讨论交流**: [GitHub Discussions](https://github.com/fxbin/bubble/discussions)

### 📚 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [Spring AI 官方文档](https://spring.io/projects/spring-ai)
- [lombok.config 配置系统](https://www.freesion.com/article/8894123984/)
- [JUnit5 使用指南](https://www.morcat.cn/archives/junit5)

### 📖 开发规范

- [项目规范定义](docs/规范定义.md)
- [Git Commit Emoji 使用指南](docs/git%20commit%20emoji%20使用指南.md)
- [代码风格指南](docs/code-style.md)
- [API 设计规范](docs/api-design.md)

### 🏆 致谢

感谢所有为 bubble 项目做出贡献的开发者们！

[![Contributors](https://contrib.rocks/image?repo=fxbin/bubble)](https://github.com/fxbin/bubble/graphs/contributors)

### 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源协议，详情请参阅 [LICENSE](LICENSE) 文件。

### 👨‍💻 作者信息

- **作者**: fanxubin
- **邮箱**: fxbin123@gmail.com
- **GitHub**: [@fxbin](https://github.com/fxbin)

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！**

[⬆ 回到顶部](#bubble)

</div>