# 快速开始

## 依赖引入（示例）

```xml
<!-- Web -->
<dependency>
  <groupId>cn.fxbin.bubble</groupId>
  <artifactId>bubble-starter-web</artifactId>
</dependency>
<!-- Redis -->
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

## 最小化 Controller 示例

```java
// 示例控制器：返回统一 Result
//@Slf4j
//@RestController
//@RequestMapping("/hello")
//public class HelloController {
//    /**
//     * 简单示例接口
//     * @return 统一成功响应
//     */
//    @GetMapping
//    public Result<String> hello() {
//        return Result.success("Bubble Ready");
//    }
//}
```

