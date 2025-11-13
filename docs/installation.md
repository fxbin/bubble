# 安装指南

## 环境要求

- JDK 17+
- Maven 3.6+
- 可选中间件：Redis 6.0+、MySQL 8.0+、Elasticsearch 7.13+

## 引入 BOM

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

