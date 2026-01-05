# AI 能力

## 模块结构

- `bubble-ai` 聚合模块
- `bubble-ai-starters/bubble-ai-starter` 基础 AI 启动器
- `bubble-ai-starters/bubble-ai-starter-model` Spring AI 模型集成（核心模块）
- `bubble-ai-starters/bubble-ai-starter-lightrag` LightRAG 集成（Forest + WebFlux）

## 模型集成 (bubble-ai-starter-model)

本模块基于 Spring AI 构建，但移除了官方臃肿的 Starter 依赖，转而使用 Base Packages（如 `spring-ai-openai`, `spring-ai-anthropic`）进行精细化管理。

### 核心特性

1.  **轻量级依赖**：只引入必要的 Base Packages，避免传递依赖冲突。
2.  **统一模型工厂**：通过 `AiModelFactory` 统一管理不同平台（OpenAI, DeepSeek, Anthropic, Gemini, Zhipu, MiniMax, SiliconFlow, Ollama 等）的 `ChatModel` 实例。
3.  **Token 用量统计**：
    - 内置 `TokenCountingChatModel` 包装器。
    - 支持**虚拟线程（Virtual Threads）**异步记录 Token 用量，不阻塞主线程。
    - 提供流式（Streaming）调用的 Token 估算回退机制。
    - 智能采样策略：当流式响应超过缓冲区限制时，自动采样内容进行估算，避免内存溢出。
4.  **性能优化**：
    - 使用 `ConcurrentHashMap` 缓存 `ChatModel` 实例。
    - 针对高并发场景优化了 `Usage` 统计逻辑。
    - API Key 哈希缓存机制，减少重复计算开销。
5.  **安全性增强**：
    - 内置敏感信息脱敏工具 `SensitiveDataUtils`，自动对 API Key 和 URL 进行脱敏处理。
    - 日志输出自动脱敏，避免敏感信息泄露。
    - 完善的异常处理和错误日志记录。
6.  **代码质量**：
    - 统一的常量管理 `AiModelConstants`，消除魔法数字。
    - 完善的单元测试覆盖，确保功能稳定性。

### 依赖引入

```xml
<dependency>
  <groupId>cn.fxbin.bubble</groupId>
  <artifactId>bubble-ai-starter-model</artifactId>
</dependency>
```

### 配置属性详解

#### 基础配置 (bubble.ai)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `token-counting.enabled` | boolean | `true` | 是否启用 Token 用量统计 |
| `token-counting.stream-estimation-enabled` | boolean | `true` | 是否启用流式响应的 Token 估算功能 |
| `model-config.enabled` | boolean | `false` | 是否启用数据库配置支持 |
| `providers` | Map | 空 | 多模型配置提供商，Key 为自定义提供商 ID |

#### 提供商配置 (bubble.ai.providers.*)

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `platform` | AiPlatformEnum | 必填 | 平台类型 (openai, deepseek, anthropic, gemini, zhipu, minimax, siliconflow, ollama) |
| `api-key` | String | 必填 | API 密钥 |
| `base-url` | String | 平台默认 | 基础 URL |
| `model` | String | 平台默认 | 模型名称 |
| `description` | String | 空 | 模型描述 |
| `temperature` | Double | `0.7` | 温度参数 |
| `top-k` | Integer | 空 | Top K 参数 |

### 配置示例

在 `application.yml` 中配置模型参数：

```yaml
bubble:
  ai:
    # Token 用量统计配置
    token-counting:
      enabled: true
      stream-estimation-enabled: true
    
    # 数据库模型配置（可选）
    model-config:
      enabled: false
    
    # 模型配置列表
    providers:
      openai-gpt4o:
        platform: openai
        model: gpt-5-mini
        api-key: ${OPENAI_API_KEY}
        base-url: https://api.openai.com
        temperature: 0.7
        top-k: 40
        description: "OpenAI GPT-5 Mini 模型"
      
      deepseek-chat:
        platform: deepseek
        model: deepseek-chat
        api-key: ${DEEPSEEK_API_KEY}
        base-url: https://api.deepseek.com
        description: "DeepSeek 对话模型"
      
      claude-sonnet:
        platform: anthropic
        model: claude-sonnet-4-5
        api-key: ${ANTHROPIC_API_KEY}
        base-url: https://api.anthropic.com
        description: "Claude 4.5 Sonnet"
      
      gemini-pro:
        platform: gemini
        model: gemini-3-flash-preview
        api-key: ${GEMINI_API_KEY}
        description: "Google Gemini Pro"
      
      zhipu-glm4:
        platform: zhipu
        model: glm-4。7
        api-key: ${ZHIPU_API_KEY}
        base-url: https://open.bigmodel.cn/api/paas/v4
        description: "智谱 GLM-4.7"
      
      minimax-chat:
        platform: minimax
        model: MiniMax-M2.1
        api-key: ${MINIMAX_API_KEY}
        base-url: https://api.minimax.chat
        description: "MiniMax 对话模型"
      
      siliconflow-deepseek:
        platform: siliconflow
        model: deepseek-ai/DeepSeek-V3.2
        api-key: ${SILICONFLOW_API_KEY}
        base-url: https://api.siliconflow.cn
        description: "SiliconFlow DeepSeek V3"
      
      ollama-local:
        platform: ollama
        model: llama3.2
        base-url: http://localhost:11434
        description: "本地 Ollama Llama 3.2"
```

### 支持的 AI 平台

| 平台 | Platform 枚举值 | 默认模型 | 默认基础URL | 说明 |
|------|----------------|----------|-------------|------|
| OpenAI | `OPENAI` | `gpt-4o` | `https://api.openai.com` | OpenAI 官方 API |
| DeepSeek | `DEEPSEEK` | `deepseek-chat` | `https://api.deepseek.com` | DeepSeek AI 模型 |
| Anthropic | `ANTHROPIC` | `claude-3-5-sonnet-20241022` | `https://api.anthropic.com` | Claude 系列模型 |
| Gemini | `GEMINI` | `gemini-1.5-pro` | Google AI Studio | Google Gemini 模型 |
| Zhipu | `ZHIPU` | `glm-4` | `https://open.bigmodel.cn/api/paas/v4` | 智谱 AI GLM 模型 |
| MiniMax | `MINIMAX` | `abab6.5s-chat` | `https://api.minimax.chat` | MiniMax 模型 |
| SiliconFlow | `SILICONFLOW` | `deepseek-ai/DeepSeek-V3` | `https://api.siliconflow.cn` | SiliconFlow 平台 |
| Ollama | `OLLAMA` | `llama3.2` | `http://localhost:11434` | 本地 Ollama 模型 |

### API 使用

#### 获取模型实例

```java
@Autowired
private AiModelFactory aiModelFactory;

public void example() {
    // 通过配置 ID 获取（推荐）
    ChatModel model = aiModelFactory.getChatModel("openai-gpt4o");
    
    // 通过平台和模型名称获取
    ChatModel model = aiModelFactory.getOrCreateChatModel(
        AiPlatformEnum.OPENAI, 
        "sk-xxx", 
        "https://api.openai.com", 
        "gpt-4o", 
        0.7, 
        40
    );
    
    // 获取默认配置的模型
    ChatModel model = aiModelFactory.getDefaultChatModel(AiPlatformEnum.OPENAI);
}
```

#### Token 用量统计

```java
@Autowired
private TokenUsageRecorder tokenUsageRecorder;

// Token 用量会自动记录，无需手动调用
// 可通过实现 TokenUsageRecorder 接口自定义记录逻辑

// 自定义记录器示例
@Component
public class MyTokenUsageRecorder implements TokenUsageRecorder {
    @Override
    public void record(TokenUsageContext context) {
        // 记录到数据库、监控系统等
        log.info("Token usage - Platform: {}, Model: {}, Usage: {}", 
                 context.platform(), context.model(), context.usage());
    }
}
```

#### 敏感信息脱敏

```java
import cn.fxbin.bubble.ai.util.SensitiveDataUtils;

public void example() {
    // API Key 脱敏
    String apiKey = "sk-proj-abc123xyz789";
    String maskedKey = SensitiveDataUtils.maskApiKey(apiKey);
    // 输出: sk-proj****xyz789
    
    // URL 脱敏
    String url = "https://api.openai.com/v1/chat/completions";
    String maskedUrl = SensitiveDataUtils.maskUrl(url);
    // 输出: https://****.openai.com/v1/chat/completions
}
```

#### 模型管理

```java
@Autowired
private AiModelManager aiModelManager;

public void example() {
    // 获取所有可用模型
    List<AiModelInfo> models = aiModelManager.listAvailableModels();
    
    // 根据模型 ID 获取 ChatModel
    ChatModel model = aiModelManager.getChatModel("openai-gpt4o");
}
```

### 高级特性

#### 1. 流式响应内存优化

`TokenCountingChatModel` 提供了智能的流式响应处理机制：

- 当流式响应超过 `MAX_STREAM_BUFFER_SIZE`（默认 100,000 字符）时，自动采样内容进行 Token 估算
- 优先使用提供商返回的 Usage 信息，只有在缺失时才进行估算
- 避免无界内存增长，防止内存溢出

```java
// 自定义缓冲区大小
TokenCountingChatModel chatModel = new TokenCountingChatModel(
    delegate, 
    recorder, 
    estimator, 
    "openai", 
    "gpt-4o", 
    true,  // 启用流式估算
    50000  // 自定义缓冲区大小
);
```

#### 2. 缓存机制

`AiModelFactoryImpl` 实现了多级缓存机制：

- **模型实例缓存**：使用 `ConcurrentHashMap` 缓存创建的 `ChatModel` 实例
- **API Key 哈希缓存**：缓存 API Key 的 SHA-256 哈希值，避免重复计算
- **缓存清理策略**：当缓存大小超过限制时自动清理，防止内存泄漏

#### 3. 数据库支持（可选）

启用数据库支持后，可以从数据库动态加载模型配置：

```yaml
bubble:
  ai:
    model-config:
      enabled: true  # 启用数据库配置
```

需要配置 MyBatis-Plus 和相应的数据库连接。

### 最佳实践

#### 1. 配置管理

- 使用环境变量存储敏感的 API Key
- 为不同的使用场景创建不同的提供商配置
- 合理设置 temperature 和 top-k 参数

#### 2. 性能优化

- 启用 Token 用量统计但注意流式估算的内存使用
- 合理设置缓存大小，避免过大的缓存占用内存
- 使用虚拟线程处理 Token 用量记录，避免阻塞主线程

#### 3. 安全建议

- 不要在日志中输出完整的 API Key
- 使用 `SensitiveDataUtils` 对敏感信息进行脱敏
- 定期轮换 API Key

#### 4. 错误处理

```java
try {
    ChatModel model = aiModelFactory.getChatModel("non-existent-model");
} catch (IllegalArgumentException e) {
    // 处理模型不存在的情况
    log.error("Model not found: {}", e.getMessage());
}
```

### 版本兼容性

- **Spring Boot**: 3.x
- **Spring AI**: 1.0.0-M6 及以上
- **JDK**: 17 及以上（支持虚拟线程）

### 更新日志

#### v1.0.0
- 初始版本发布
- 支持 OpenAI、DeepSeek、Anthropic、Gemini、Zhipu、MiniMax、SiliconFlow、Ollama
- 实现 Token 用量统计和缓存机制
- 添加敏感信息脱敏功能
- 支持流式响应内存优化