# bubble-starter-satoken 使用文档

## 1. 概述

`bubble-starter-satoken` 是 Bubble 微服务框架中用于快速集成 [Sa-Token](https://sa-token.cc/) 权限认证框架的启动器。它简化了 Sa-Token 在 Spring Boot 项目中的配置和使用，并提供了一些增强功能，如基于 `TransmittableThreadLocal` 的上下文传递，确保在异步任务中 Sa-Token 上下文的正确性。

## 2. 主要功能

- **自动配置 Sa-Token**: 自动装配 Sa-Token 的核心组件和标准配置。
- **Bubble增强配置**: 通过 `application.yml` 或 `application.properties` 文件，使用 `bubble.satoken` 前缀对 Sa-Token 进行特定于 Bubble 的增强配置，例如JWT模式、拦截与放行规则等。
- **标准Sa-Token配置**: 完全兼容 Sa-Token 原生的 `sa-token.*` 配置项。
- **JWT 支持**: 内置 JWT (JSON Web Token) 支持，可通过 `bubble.satoken.jwt.enabled=true` 启用，并可配置不同的JWT工作模式。
- **上下文传递**: 实现了 `TaskDecorator` (`SaTokenTaskDecorator`)，确保 Sa-Token 上下文（包括 Request, Response, Storage, Token）在异步线程间的正确传递，依赖 `TransmittableThreadLocal`。
- **全局异常处理**: 提供了 `SaTokenExceptionHandler` 来统一处理 Sa-Token 相关的认证和授权异常。
- **工具类**: 提供了 `TokenUtils` 工具类，方便进行登录、登出、获取用户信息、生成 Token 等操作。

## 3. 引入依赖

在您的 Maven 项目的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>cn.fxbin</groupId>
    <artifactId>bubble-starter-satoken</artifactId>
    <version>YOUR_BUBBLE_VERSION</version> <!-- 请替换为您的 Bubble 版本 -->
</dependency>
```

该启动器会自动引入 Sa-Token 相关的依赖，例如：
- `sa-token-spring-boot3-starter`
- `sa-token-jwt`
- `sa-token-temp-jwt` (用于临时 Token，例如刷新 Token)
- `sa-token-redis-jackson` (如果选择 Redis 作为 Sa-Token 的 DAO)
- `com.alibaba:transmittable-thread-local` (用于异步上下文传递)

## 4. 配置说明

您可以在 `application.yml` (或 `.properties`) 文件中配置 Sa-Token 的行为。配置分为两部分：Sa-Token原生配置 (`sa-token.*`) 和 Bubble 增强配置 (`bubble.satoken.*`)。

### 4.1. Sa-Token 原生配置示例 (`sa-token.*`)

以下是一个 Sa-Token 原生配置的示例，更多配置请参考 [Sa-Token官方文档](https://sa-token.cc/doc.html#/config/all-config)。

```yaml
# application.yml
sa-token:
  # Token 名称 (同时也是 Cookie 名称)
  token-name: "satoken"
  # Token 有效期，单位s (默认30天)
  timeout: 2592000
  # Token 最低活跃频率，单位s (如果设置为-1，则永不刷新；如果设置为0，则每次请求都刷新；如果大于0，则具上次活跃指定时间后本次请求刷新)
  active-timeout: 1800
  # 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个 Token (为true时所有登录共用一个 Token, 为false时每次登录新建一个 Token)
  is-share: true
  # Token 风格 (默认可取值：uuid, simple-uuid, random-32, random-64, random-128, tik)
  token-style: "uuid"
  # 是否输出操作日志
  is-log: true
  # JWT 秘钥 (如果使用sa-token自带的JWT生成和解析功能，需要配置此项)
  jwt-secret-key: "YOUR_SA_TOKEN_JWT_SECRET_KEY_PLEASE_CHANGE_IT"
```

### 4.2. Bubble Sa-Token 扩展配置 (`bubble.satoken.*`)

以下是 `bubble-starter-satoken` 提供的扩展配置项：

```yaml
# application.yml
bubble:
  satoken:
    # 是否启用 bubble-starter-satoken 的自动配置 (默认为 true)
    enabled: true
    # JWT 相关配置 (当 bubble.satoken.jwt.enabled = true 时生效)
    jwt:
      # 是否启用 Bubble 对 Sa-Token JWT 的增强管理模式 (默认为 false)
      # 如果为 true, Sa-Token 的 Token 生成和校验会倾向于使用JWT模式。
      # 注意：这不等同于 sa-token.jwt-secret-key 的配置，那个是Sa-Token自身JWT功能。
      # Bubble的JWT增强模式主要体现在 SaTokenAutoConfiguration 中对 StpLogic 的选择和配置。
      enabled: false
      # JWT 工作模式 (默认为 simple)
      # - simple: JWT 不会存储到 Redis (或其他 SaTokenDao 实现) 中，只是单纯的 JWT 校验模式。
      # - mixin: JWT 会存储到 Redis 中，主要用于 Token 有效性校验（如续期、踢人等），但请求校验时仍依赖JWT本身。
      # - stateless: JWT 不会存储到 Redis 中，纯粹的 JWT 校验模式，会校验 JWT 本身。
      jwt-mode: simple 
    # 认证与授权拦截规则配置
    auth:
      # 需要进行登录拦截的路径，默认为 '/**' (所有路径)
      include-urls:
        - "/**"
      # 不需要进行登录拦截的路径 (白名单)
      exclude-urls:
        - "/auth/login"
        - "/doc.html/**" # Swagger/Knife4j UI
        - "/webjars/**"
        - "/v3/api-docs/**"
        - "/favicon.ico"
```

**Bubble 扩展配置项说明:**

- **`bubble.satoken.enabled`**: (布尔值, 默认 `true`) 是否启用 `bubble-starter-satoken` 的自动配置功能。如果设置为 `false`，此启动器的大部分自动配置将不会生效。
- **`bubble.satoken.jwt.enabled`**: (布尔值, 默认 `false`) 是否启用 Bubble 对 Sa-Token JWT 的增强管理模式。当设置为 `true` 时，`SaTokenAutoConfiguration` 会尝试配置 Sa-Token 使用 JWT 相关的 `StpLogic` (例如 `StpLogicJwtForSimple`, `StpLogicJwtForMixin`, `StpLogicJwtForStateless`)。
- **`bubble.satoken.jwt.jwt-mode`**: (枚举值, 默认 `simple`, 可选 `simple`, `mixin`, `stateless`) 定义了当 `bubble.satoken.jwt.enabled=true` 时，Sa-Token 使用 JWT 的具体工作模式。这些模式对应 Sa-Token 提供的不同 `StpLogic` 实现。
- **`bubble.satoken.auth.include-urls`**: (字符串列表, 默认 `["/**"]`) 定义了哪些 URL 路径需要进行 Sa-Token 的登录拦截和权限校验。使用 Ant 风格路径匹配。
- **`bubble.satoken.auth.exclude-urls`**: (字符串列表, 默认空) 定义了哪些 URL 路径不需要进行 Sa-Token 的登录拦截和权限校验（即白名单）。

**重要提示**: `bubble.satoken.jwt.secret-key` **不是** Bubble 提供的配置。JWT 签名秘钥应通过 Sa-Token 原生的 `sa-token.jwt-secret-key` 进行配置。Bubble 的 JWT 配置 (`bubble.satoken.jwt.*`) 主要是为了方便切换和管理 Sa-Token 内置的不同 JWT `StpLogic` 实现。

### 5. 核心组件解析

#### 5.1. 自动配置 (`SaTokenAutoConfiguration`)

<mcsymbol name="SaTokenAutoConfiguration" filename="SaTokenAutoConfiguration.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\autoconfigure\SaTokenAutoConfiguration.java" startline="34" type="class"></mcsymbol> 负责 Sa-Token 相关 Bean 的自动装配，包括：

- **`SaTokenProperties`**: 加载 `bubble.satoken` 前缀的配置。
- **`SaTokenExceptionHandler`**: 全局 Sa-Token 异常处理器。
- **`SaTokenContextFilter`**: 用于在请求处理开始时设置 Sa-Token 上下文，特别是针对 `TransmittableThreadLocal` 的支持。
- **`SaTokenTaskDecorator`**: Spring Task 装饰器，用于在异步任务中传播 Sa-Token 上下文。
- **JWT StpLogic 配置**: 如果 `bubble.satoken.jwt.enabled` 为 `true`，则会根据 `bubble.satoken.jwt.jwt-mode` 的值，尝试配置 Sa-Token 使用对应的 JWT `StpLogic` (例如 `StpLogicJwtForSimple`, `StpLogicJwtForMixin`, `StpLogicJwtForStateless`)。
- **路由拦截配置**: 根据 `bubble.satoken.auth.include-urls` 和 `bubble.satoken.auth.exclude-urls` 配置 Sa-Token 的路由拦截器。

#### 5.2. 配置属性 (`SaTokenProperties`)

<mcsymbol name="SaTokenProperties" filename="SaTokenProperties.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\autoconfigure\SaTokenProperties.java" startline="26" type="class"></mcsymbol> 类用于映射 `application.yml` 中 `bubble.satoken` 前缀的配置项。

#### 5.3. 上下文传递

为了解决异步场景下（例如 `@Async` 方法、线程池任务）`ThreadLocal` 无法正确传递 Sa-Token 上下文的问题，本启动器引入了 `TransmittableThreadLocal` (TTL) 的支持。

- **`SaTokenContextForTtl`**: <mcsymbol name="SaTokenContextForTtl" filename="SaTokenContextForTtl.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\context\SaTokenContextForTtl.java" startline="26" type="class"></mcsymbol> 实现了 `SaTokenContext` 接口，使用 `TransmittableThreadLocal` 来存储 `SaRequest`、`SaResponse` 和 `SaStorage`。
- **`SaTokenContextForTtlStaff`**: <mcsymbol name="SaTokenContextForTtlStaff" filename="SaTokenContextForTtlStaff.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\context\SaTokenContextForTtlStaff.java" startline="23" type="class"></mcsymbol> 是一个辅助类，用于管理 `SaTokenContextForTtl` 中的模型对象 (`SaTokenContextModelBox`)。
- **`SaRequestForTtl`**, **`SaResponseForTtl`**, **`SaStorageForTtl`**: <mcsymbol name="SaRequestForTtl" filename="SaRequestForTtl.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\model\SaRequestForTtl.java" startline="21" type="class"></mcsymbol>, <mcsymbol name="SaResponseForTtl" filename="SaResponseForTtl.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\model\SaResponseForTtl.java" startline="20" type="class"></mcsymbol>, <mcsymbol name="SaStorageForTtl" filename="SaStorageForTtl.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\model\SaStorageForTtl.java" startline="20" type="class"></mcsymbol> 分别是 `SaRequest`, `SaResponse`, `SaStorage` 针对 TTL 的简单实现，主要用于非 Web 环境下的上下文模拟和异步任务中的上下文恢复。
- **`SaTokenTaskDecorator`**: <mcsymbol name="SaTokenTaskDecorator" filename="SaTokenTaskDecorator.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\task\SaTokenTaskDecorator.java" startline="17" type="class"></mcsymbol> 在异步任务执行前捕获当前线程的 Sa-Token 上下文（包括 Token 值），并在异步任务开始时恢复这些上下文，任务结束后进行清理。这确保了 `StpUtil.getLoginId()` 等方法在异步任务中也能正确获取到登录信息。

#### 5.4. 全局异常处理 (`SaTokenExceptionHandler`)

<mcsymbol name="SaTokenExceptionHandler" filename="SaTokenExceptionHandler.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\exception\SaTokenExceptionHandler.java" startline="28" type="class"></mcsymbol> 使用 `@RestControllerAdvice` 注解，统一捕获并处理 Sa-Token 抛出的各种认证和授权异常，例如 `NotLoginException`, `NotRoleException`, `NotPermissionException` 等，并返回统一格式的 JSON 响应。

#### 5.5. 上下文过滤器 (`SaTokenContextFilter`)

<mcsymbol name="SaTokenContextFilter" filename="SaTokenContextFilter.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\filter\SaTokenContextFilter.java" startline="27" type="class"></mcsymbol> 是一个 Servlet Filter，它的主要作用是在每个 HTTP 请求处理之前，将 Sa-Token 的上下文设置为 `SaTokenContextForTtl` 的实例 (通过 `SaTokenContextForTtlStaff.setModelBox` 将 `SaRequestForServlet`, `SaResponseForServlet`, `SaStorageForServlet` 包装后存入 `TransmittableThreadLocal`)。这使得整个请求处理链（包括后续的 Controller 和 Service）都能使用基于 `TransmittableThreadLocal` 的上下文。

#### 5.6. Token 工具类 (`TokenUtils`)

<mcsymbol name="TokenUtils" filename="TokenUtils.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\util\TokenUtils.java" startline="21" type="class"></mcsymbol> 提供了一系列静态方法，简化 Token 相关操作：

- **`logout(String userId)`**: 用户登出。
- **`kickout(String userId)`**: 将用户踢下线。
- **`forceLogout(String userId)`**: 强制用户注销 (效果同 `logout`)。
- **`getCurrentUserId()`**: 获取当前登录用户的 ID。
- **`getCurrentUsername()`**: 获取当前登录用户的用户名 (需要登录时通过 `StpUtil.login` 的 `extraData` 参数存入，键为 "username")。
- **`getCurrentRoles()`**: 获取当前登录用户的角色列表 (需要登录时通过 `StpUtil.login` 的 `extraData` 参数存入，键为 "roles")。
- **`getCurrentPermissions()`**: 获取当前登录用户的权限列表 (需要登录时通过 `StpUtil.login` 的 `extraData` 参数存入，键为 "permissions")。
- **`getTokens(String userId, String username, List<String> roles, List<String> permissions)`**: 用户登录并生成 Access Token 和 Refresh Token (Refresh Token 通过 `SaTempUtil.createToken` 生成)。会将 `userId`, `username`, `roles`, `permissions` 作为额外数据 (`extraData`) 存入 Access Token。
- **`createJwtToken(String userId, String username, List<String> roles, List<String> permissions)`**: (主要供内部使用或特定场景) 直接创建一个 JWT Token，不执行 Sa-Token 的登录逻辑。依赖 `sa-token.jwt-secret-key` 配置。
- **`isValidToken(String token)`**: 检查给定的 Token 是否仍然有效 (通过 `SaManager.getSaTokenDao().get(tokenKey)` 检查)。

#### 5.7. Token 模型 (`Tokens`)

<mcsymbol name="Tokens" filename="Tokens.java" path="d:\Project\Github\bubble\bubble-starters\bubble-starter-satoken\src\main\java\cn\fxbin\bubble\plugin\satoken\model\Tokens.java" startline="19" type="class"></mcsymbol> 是一个简单的数据模型类，用于封装生成的 Access Token 和 Refresh Token。

```java
package cn.fxbin.bubble.plugin.satoken.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Tokens
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 16:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tokens implements Serializable {

    private String accessToken;

    private String refreshToken;

}
```

### 6. 使用示例

#### 6.1. 用户登录

```java
import cn.fxbin.bubble.core.model.Result;
import cn.fxbin.bubble.plugin.satoken.model.Tokens;
import cn.fxbin.bubble.plugin.satoken.util.TokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

// 假设的 LoginRequest 类
class LoginRequest {
    private String username;
    private String password;
    // getters and setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}

// 假设的 UserService
class UserService {
    public String verifyAndGetUserId(String username, String password) {
        // 实际的验证逻辑，例如查询数据库
        if ("admin".equals(username) && "password".equals(password)) {
            return "1";
        }
        return null;
    }
    public List<String> getUserRoles(String userId) { return Arrays.asList("admin", "user"); }
    public List<String> getUserPermissions(String userId) { return Arrays.asList("user:view", "user:edit"); }
    public UserInfo getUserInfo(String userId, String username) { return new UserInfo(userId, username); }
    public List<User> listAll() { return Arrays.asList(new User("1", "admin")); }
    public void addUser(User user) { System.out.println("User added: " + user.getUsername()); }
}

@RestController
@RequestMapping("/auth")
public class AuthController {

    // 模拟注入，实际项目中应使用 @Autowired
    private final UserService userService = new UserService();

    @PostMapping("/login")
    public Result<Tokens> login(@RequestBody LoginRequest loginRequest) {
        // 1. 验证用户名密码
        String userId = userService.verifyAndGetUserId(loginRequest.getUsername(), loginRequest.getPassword());
        if (userId == null) {
            return Result.failed("用户名或密码错误");
        }

        // 2. 获取用户的角色和权限
        List<String> roles = userService.getUserRoles(userId);
        List<String> permissions = userService.getUserPermissions(userId);

        // 3. 调用 TokenUtils.getTokens 进行登录并获取 Token
        // 将 username, roles, permissions 作为 extraData 存入
        Tokens tokens = TokenUtils.getTokens(userId, loginRequest.getUsername(), roles, permissions);
        return Result.success(tokens);
    }

}
```

#### 6.2. 接口权限校验

Sa-Token 提供了多种方式进行权限校验，可以直接在 Controller 方法上使用注解：

```java
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.fxbin.bubble.core.model.Result;
import cn.fxbin.bubble.plugin.satoken.util.TokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 假设的 UserInfo 和 User 类
class UserInfo { 
    public String userId; public String username; 
    public UserInfo(String u, String n){userId=u;username=n;} 
}
class User { 
    public String id; public String username; 
    public User(String i, String n){id=i;username=n;} 
    public String getUsername(){return username;}
}

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService = new UserService();

    // 需要登录才能访问
    @GetMapping("/info")
    @SaCheckLogin
    public Result<UserInfo> getUserInfo() {
        String userId = TokenUtils.getCurrentUserId();
        String username = TokenUtils.getCurrentUsername(); // 从 extraData 中获取
        return Result.success(userService.getUserInfo(userId, username));
    }

    // 需要拥有 'admin' 角色才能访问
    @GetMapping("/list")
    @SaCheckRole("admin") // 检查 extraData 中的 roles 是否包含 "admin"
    public Result<List<User>> listUsers() {
        return Result.success(userService.listAll());
    }

    // 需要拥有 'user:add' 权限才能访问
    @PostMapping("/add")
    @SaCheckPermission("user:add") // 检查 extraData 中的 permissions 是否包含 "user:add"
    public Result<Void> addUser(@RequestBody User newUser) {
        userService.addUser(newUser);
        return Result.success();
    }

}
```

#### 6.3. 异步任务中使用 Sa-Token

如果您配置了 Spring 的异步支持 (例如通过 `@EnableAsync` 和 `@Async` 注解)，`SaTokenTaskDecorator` 会自动确保 Sa-Token 上下文的传递。

```java
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendWelcomeEmailAsync(String userId) {
        // 在异步方法中，仍然可以正确获取到登录信息
        String currentLoginId = StpUtil.getLoginIdAsString(); // 正确获取到主线程的登录ID
        log.info("异步任务：尝试为用户 {} 发送欢迎邮件，当前 Sa-Token 上下文登录ID: {}", userId, currentLoginId);
        // ... 发送邮件逻辑
        // 可以在这里尝试获取 StpUtil.getExtra("username") 等信息
        String username = (String) StpUtil.getExtra("username");
        log.info("异步任务中获取到的用户名: {}", username);
    }

}
```
要使 `SaTokenTaskDecorator` 生效，您需要确保 Spring Boot 的 Task Execution 配置会使用到这个 Decorator。通常，如果项目中存在 `TaskDecorator` 类型的 Bean，Spring Boot 会自动应用它。`SaTokenAutoConfiguration` 中已经自动注册了 `SaTokenTaskDecorator` Bean。

### 7. 注意事项

- **JWT 秘钥安全**: 如果使用 Sa-Token 的 JWT 功能 (无论是通过 `bubble.satoken.jwt.enabled=true` 还是直接使用 Sa-Token 的 JWT API)，请务必将 `sa-token.jwt-secret-key` 设置为一个强大且唯一的秘钥，并妥善保管，不要硬编码在不安全的地方或提交到版本控制中。
- **`TransmittableThreadLocal` 依赖**: `SaTokenTaskDecorator` 和 `SaTokenContextForTtl` 的功能依赖于 `com.alibaba:transmittable-thread-local` 库。此启动器已将其作为传递依赖引入。
- **配置优先级**: `bubble.satoken.*` 下的配置项主要用于增强和简化 Sa-Token 的配置。Sa-Token 自身丰富的配置项 (`sa-token.*`) 仍然完全有效，请参考其官方文档。
- **DAO 实现**: Sa-Token 需要一个 `SaTokenDao` 的实现来持久化 Token 数据。默认情况下，它使用内存存储。对于生产环境，强烈建议配置 Redis (`sa-token-redis-jackson`) 或其他持久化存储。
- **`extraData` 的使用**: `TokenUtils.getTokens` 方法会将 `username`, `roles`, `permissions` 存入 Token 的 `extraData`。在需要获取这些信息时，可以使用 `StpUtil.getExtra("key_name")`。

### 8. 总结

`bubble-starter-satoken` 为 Bubble 项目集成 Sa-Token 提供了便捷的途径，通过自动配置和增强功能，简化了开发者的工作，特别是在处理异步任务时的上下文传递问题。合理配置并使用该启动器，可以快速构建安全可靠的认证授权体系。