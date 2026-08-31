# 03 - ChatModel with Manual Configuration & ChatResponse

This demo shows two things:

1. **Manual `ChatModel` configuration** — instead of relying on Spring AI's
   auto-configuration, we build the `ChatModel` bean ourselves so we control
   every part of the wiring (transport, credentials, and default options).
2. **`ChatResponse` usage** — how to get not just the generated text but also
   the run metadata (token usage) back from a call.

The LLM provider here is **GitHub Models** via Spring AI's OpenAI-compatible
client.

## 🎯 Auto-configuration vs Manual configuration

| | Auto-configuration | Manual configuration (this demo) |
|---|---|---|
| Who builds the `ChatModel` | Spring AI starter, from properties | You, in [`ApplicationConfig`](src/main/java/com/infy/config/ApplicationConfig.java) |
| Control | Convention-driven | Full, explicit control in code |
| Good for | Quick start | Custom transport, multiple beans, interceptors, swapping providers |

`ApplicationConfig` builds the bean in three explicit steps:

```java
// 1) transport / client
OpenAiApi openAiApi = OpenAiApi.builder()
        .baseUrl(baseUrl)
        .apiKey(apiKey)
        .completionsPath(completionsPath)
        .build();

// 2) model defaults
OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
        .model(model)
        .temperature(0.7)
        .maxTokens(1000)
        .build();

// 3) final model assembly
return OpenAiChatModel.builder()
        .openAiApi(openAiApi)
        .defaultOptions(defaultOptions)
        .build();
```

It returns the framework-neutral `ChatModel`, so controller code stays
provider-agnostic.

## 📡 API Endpoints

Endpoints are named after the **action** each one performs:

### 1. `GET /api/chat/ask`
Simplest usage — raw text in, plain text out. Uses `chatModel.call(String)`.

```
http://localhost:8080/api/chat/ask?userPrompt=What is REST API?
```

### 2. `GET /api/chat/ask-with-metadata`
Demonstrates **`ChatResponse`**. Builds a structured `Prompt` (system + user
message), then reads both the generated content and the token-usage metadata
off the `ChatResponse`.

```
http://localhost:8080/api/chat/ask-with-metadata?userPrompt=Explain REST API
```

Returns the answer followed by prompt / completion / total token counts.

## 🔧 Configuration

[`application.properties`](src/main/resources/application.properties) points the
OpenAI-compatible client at GitHub Models:

```properties
spring.ai.openai.api-key=${GITHUB_TOKEN}
spring.ai.openai.base-url=https://models.github.ai
spring.ai.openai.chat.completions-path=/inference/chat/completions
spring.ai.openai.chat.options.model=openai/gpt-4o-mini
```

Set your token before running:

**PowerShell**
```powershell
$env:GITHUB_TOKEN="your-github-models-token"
```

## 🚀 Running

```cmd
mvnw spring-boot:run
```

App starts on `http://localhost:8080`. Swagger UI is available at
`http://localhost:8080/swagger-ui.html` (springdoc).

## 🎓 Key Takeaways

1. You can build a `ChatModel` by hand for full control over transport and defaults.
2. `ChatModel.call(String)` is the quick path; `ChatModel.call(Prompt)` returns a
   rich `ChatResponse`.
3. `ChatResponse` carries both the output text and run metadata (token usage).
4. Naming endpoints after their action (`/ask`, `/ask-with-metadata`) keeps the
   API self-documenting.
