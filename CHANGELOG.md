# 变更记录
## 发行版本

### [2022.0.1] - 2023.06.25

### [2022.0.0] - 2022.04.28
🎇New Features
* 移除swagger模块，独立为 [swagger-spring-boot-starter](https://github.com/fxbin/swagger-spring-boot-starter)
* 增加项目启动信息
* redis 记录操作日志
* 项目结构调整

🔨Dependency Upgrades

* Upgrade to hutool 5.5.2
* Upgrade to redisson 3.17.0
* Upgrade to aliyun-log 0.6.69
* Upgrade to transmittable-thread-local 2.12.6
* Upgrade to easyexcel 3.0.5
* Upgrade to aliyun-sdk-oss 3.14.0
* Upgrade to springboot 2.6.6
* Upgrade to knife4j 3.0.3

### [1.0.1-RELEASE] - 2021.04.23
🛠 Bug Fix
* Swagger 在WebFlux 模式下Bean加载冲突问题修复

### [1.0.0-RELEASE] - 2021.04.23
🎇New Features
* 适配SpringCloud2020
* 适配SpringCloudAlibaba 2021.1
* 相关依赖版本升级

🛠 Bug Fix
* Redis 时间序列化问题修复

### [0.0.9-RELEASE] - 2020.12.13
🎇New Features

* expand ifpresent api for mybatis-plus
* expand jwt generator & jwt parser & set default secret
* unpack default feign exception

🔨Dependency Upgrades

* Upgrade to hutool 5.5.2
* Upgrade to protobuf-java 3.14.0
* Upgrade to redisson 3.14.0
* Upgrade to aliyun-log 0.6.61
* Upgrade to transmittable-thread-local 2.12.0
* Upgrade to easyexcel 2.2.7
* Upgrade to aliyun-sdk-oss 3.11.2
* Upgrade to springboot 2.3.7.RELEASE
* Upgrade to knife4j 2.0.8

🛠 Bug Fix

* filter swagger exclude path based on regular expression

### [0.0.8-RELEASE] - 2020.11.15
🎇New Features

* add xxl-job-executor module
* add token generator plugin module
* support hot refreshing of resources

🔨Dependency Upgrades

* Upgrade to guava 30.0-jre
* Upgrade to hutool 5.4.7
* Upgrade to protobuf-java 3.13.0
* Upgrade to aliyun-log 0.6.60
* Upgrade to mapstruct 1.4.1.Final
* Upgrade to knife4j 2.0.
* Upgrade to matis-plus 3.4.1
* Upgrade to SpringBootAdmin 2.3.1
* Upgrade to SpringBoot 2.3.6.RELEASE
* Upgrade to SpringCloud Hoxton SR9
* Upgrade to maven-enforcer-plugin 3.0.0-M3
* Upgrade to jacoco-maven-plugin 0.8.6
* Upgrade to versions-maven-plugin 2.8.1

🎨 Other Changes

* remove expired  swagger api
* improve sentinel customize exception
* `bubble-fireworks-core` add getRequestUrl method

### [0.0.7-RELEASE] - 2020.10.18
🎇New Features
* layering package jar config
* add spring-boot-admin-dependencies

🛠 Bug Fix
* swagger web resources auto configuration bug fixed

🎨 Other Changes
* redis try lock downgrade strategy log update
* remove default global response handler
* remove exclude dependency

### [0.0.6-RELEASE] - 2020.09.22
🎨 Other Changes
* set default response and ignore swagger resources
* set default error code of service exception
* add swagger test example module

🔨Dependency Upgrades
* Upgrade to mybatis-plus 3.4.0
* Upgrade to SpringBoot 2.3.4.RELEASE
* Upgrade to SpringCloud Hoxton SR8
* Upgrade to SpringCloud Alibaba 2.2.3.RELEASE

### [0.0.5-RELEASE] - 2020.08.12
🛠 bug fixed
* 紧急修复 spring-configuration-metadata.json 文件不能自动生成问题

### [0.0.4-RELEASE] - 2020.08.10
🎇New Features
* 新增动态线程池starter
* 新增redis lock starter

🎨 Other Changes
* compiler default import mapstruct and lombok
* set global configuration about web project
* clean unused code

🔨Dependency Upgrades
* Upgrade to Hutool 5.3.10
* Upgrade to SpringBoot 2.3.2.RELEASE

### [0.0.3-RELEASE] - 2020.06.29
* first release, base modules