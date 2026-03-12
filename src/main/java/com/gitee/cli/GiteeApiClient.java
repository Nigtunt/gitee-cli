package com.gitee.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gitee REST API 客户端 — 基于 Java 11+ 原生 {@link HttpClient} 的单例封装。
 * <p>
 * 鉴权方式：通过 HTTP Header {@code Private-Token} + {@code OAUTH: enabled} 注入 Token。
 */
public final class GiteeApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static volatile GiteeApiClient instance;

    /** 全局 debug 开关，由 GiteeCliCommand --debug 设置 */
    private static volatile boolean debug;

    private final HttpClient httpClient;
    private final String token;
    private final String baseUrl;
    private final ObjectMapper mapper;

    private GiteeApiClient(String token, String baseUrl) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.mapper = OutputHelper.getMapper();
    }

    /**
     * 获取单例实例（token 和 baseUrl 自动从配置解析）。
     */
    public static GiteeApiClient getInstance() {
        if (instance == null) {
            synchronized (GiteeApiClient.class) {
                if (instance == null) {
                    instance = new GiteeApiClient(resolveToken(), ConfigManager.loadBaseUrl());
                }
            }
        }
        return instance;
    }

    /**
     * 使用指定 Token 创建临时实例（不影响单例）。
     * 用于 auth login 验证 token 有效性。
     */
    public static GiteeApiClient withToken(String token) {
        return new GiteeApiClient(token, ConfigManager.loadBaseUrl());
    }

    /**
     * 设置全局 debug 模式。
     */
    public static void setDebug(boolean enabled) {
        debug = enabled;
    }

    // ── 公共 API ─────────────────────────────────────────────

    /**
     * 发送 GET 请求，返回解析后的 {@link JsonNode}。
     *
     * @param path        API 路径，例如 {@code /repos/owner/repo/issues}
     * @param queryParams 额外的查询参数，可为 null
     */
    public JsonNode get(String path, Map<String, String> queryParams) {
        var url = buildUrl(path, queryParams);
        var request = authBuilder(url)
                .GET()
                .build();
        debugRequest(request, null);
        return execute(request);
    }

    /**
     * 发送 POST 请求（JSON body），返回解析后的 {@link JsonNode}。
     *
     * @param path API 路径
     * @param body 请求体对象，会被序列化为 JSON
     */
    public JsonNode post(String path, Object body) {
        try {
            var jsonBody = mapper.writeValueAsString(body);
            var url = buildUrl(path, null);
            var request = authBuilder(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            debugRequest(request, jsonBody);
            return execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    /**
     * 发送 PATCH 请求（JSON body），返回解析后的 {@link JsonNode}。
     *
     * @param path API 路径
     * @param body 请求体对象
     */
    public JsonNode patch(String path, Object body) {
        try {
            var jsonBody = mapper.writeValueAsString(body);
            var url = buildUrl(path, null);
            var request = authBuilder(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            debugRequest(request, jsonBody);
            return execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    /**
     * 发送 PUT 请求（JSON body），返回解析后的 {@link JsonNode}。
     *
     * @param path API 路径
     * @param body 请求体对象
     */
    public JsonNode put(String path, Object body) {
        try {
            var jsonBody = mapper.writeValueAsString(body);
            var url = buildUrl(path, null);
            var request = authBuilder(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            debugRequest(request, jsonBody);
            return execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    /**
     * 发送 GET 请求，返回原始字符串（用于 diff 等非 JSON 响应）。
     */
    public String getRaw(String path, Map<String, String> queryParams) {
        var url = buildUrl(path, queryParams);
        var request = authBuilder(url)
                .GET()
                .build();
        debugRequest(request, null);
        return executeRaw(request);
    }

    /**
     * 发送 DELETE 请求，通常不返回 body 或者返回空 JSON 即可。
     *
     * @param path API 路径
     */
    public JsonNode delete(String path) {
        var url = buildUrl(path, null);
        var request = authBuilder(url)
                .DELETE()
                .build();
        debugRequest(request, null);
        return execute(request);
    }

    // ── 内部方法 ──────────────────────────────────────────────

    private JsonNode execute(HttpRequest request) {
        var body = executeRaw(request);
        try {
            if (body == null || body.isBlank()) {
                return mapper.createObjectNode();
            }
            return mapper.readTree(body);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    private String executeRaw(HttpRequest request) {
        try {
            var response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            var status = response.statusCode();

            debugResponse(response);

            if (status < 200 || status >= 300) {
                throw new GiteeApiException(status, response.body());
            }
            return response.body();
        } catch (GiteeApiException e) {
            throw e;
        } catch (IOException e) {
            throw new GiteeApiException(0, "Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GiteeApiException(0, "Request interrupted: " + e.getMessage());
        }
    }

    // ── Debug 输出 ─────────────────────────────────────────────

    /**
     * 打印请求详情（debug 模式下）。
     */
    private void debugRequest(HttpRequest request, String body) {
        if (!debug)
            return;

        System.err.println("───────── DEBUG: Request ─────────");
        System.err.println(request.method() + " " + request.uri());
        System.err.println("Headers:");
        request.headers().map().forEach((name, values) -> {
            for (var value : values) {
                // 隐藏 Token 值，只显示前 4 位
                if ("Private-Token".equalsIgnoreCase(name) && value.length() > 4) {
                    System.err.println("  " + name + ": " + value.substring(0, 4) + "****");
                } else {
                    System.err.println("  " + name + ": " + value);
                }
            }
        });
        if (body != null && !body.isBlank()) {
            System.err.println("Body:");
            System.err.println("  " + body);
        }
        System.err.println("──────────────────────────────────");
    }

    /**
     * 打印响应详情（debug 模式下）。
     */
    private void debugResponse(HttpResponse<String> response) {
        if (!debug)
            return;

        var status = response.statusCode();
        var statusColor = (status >= 200 && status < 300) ? String.valueOf(status)
                : String.valueOf(status);

        System.err.println("───────── DEBUG: Response ────────");
        System.err.println("Status: " + statusColor);
        System.err.println("Headers:");
        response.headers().map().forEach((name, values) -> {
            for (var value : values) {
                System.err.println("  " + name + ": " + value);
            }
        });

        var body = response.body();
        if (body != null && !body.isBlank()) {
            System.err.println("Body:");
            // 截断过长的响应体
            if (body.length() > 2000) {
                System.err.println("  " + body.substring(0, 2000));
                System.err.println("  ... (" + body.length() + " chars total, truncated)");
            } else {
                System.err.println("  " + body);
            }
        }
        System.err.println("──────────────────────────────────");
    }

    /**
     * 创建已注入鉴权 Header 的 {@link HttpRequest.Builder}。
     * <ul>
     * <li>{@code OAUTH: enabled}</li>
     * <li>{@code Private-Token: <token>}</li>
     * </ul>
     */
    private HttpRequest.Builder authBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("OAUTH", "enabled")
                .header("Private-Token", token);
    }

    /**
     * 构建完整 URL（鉴权已移至 Header，此处仅处理 query params）。
     */
    private String buildUrl(String path, Map<String, String> queryParams) {
        var sb = new StringBuilder(baseUrl);
        if (!path.startsWith("/")) {
            sb.append('/');
        }
        sb.append(path);

        if (queryParams != null && !queryParams.isEmpty()) {
            var params = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                            + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            sb.append('?').append(params);
        }

        return sb.toString();
    }

    /**
     * Token 解析优先级：
     * <ol>
     * <li>环境变量 {@code GITEE_TOKEN}（CI/CD 场景）</li>
     * <li>配置文件 {@code ~/.gitee-cli/config.json}（日常使用）</li>
     * <li>都没有 → 报错提示 {@code gitee auth login}</li>
     * </ol>
     */
    private static String resolveToken() {
        // 1. 环境变量优先
        var token = System.getenv("GITEE_TOKEN");
        if (token != null && !token.isBlank()) {
            return token;
        }

        // 2. 配置文件
        token = ConfigManager.loadToken();
        if (token != null && !token.isBlank()) {
            return token;
        }

        // 3. 都没有 → 报错
        System.err.println("Error: Not authenticated.");
        System.err.println("Run 'gitee auth login --token <your_token>' to authenticate,");
        System.err.println("or set environment variable GITEE_TOKEN.");
        System.exit(ExitCode.CLIENT_ERROR);
        return null; // unreachable
    }
}
