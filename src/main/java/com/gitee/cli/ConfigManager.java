package com.gitee.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置管理器 — 负责 Token、Base URL 等配置的持久化读写。
 * <p>
 * 配置文件路径：{@code {user.home}/.gitee-cli/config.json}
 * <p>
 * 配置文件格式示例：
 * 
 * <pre>
 * {
 *   "token": "xxx",
 *   "base_url": "https://gitee.com/api/v5"
 * }
 * </pre>
 */
public final class ConfigManager {

    private static final String CONFIG_DIR_NAME = ".gitee-cli";
    private static final String CONFIG_FILE_NAME = "config.json";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_REMOTE_PATTERN = "remote_pattern";

    public static final String DEFAULT_BASE_URL = "https://gitee.com/api/v5";
    public static final String DEFAULT_REMOTE_PATTERN = "^(?:https?://[^/]+/*|ssh://git@[^/]+:\\d+/*|git@[^:]+:)(.*?)(\\.git)?$";

    private static final ObjectMapper MAPPER = OutputHelper.getMapper();

    private ConfigManager() {
    }

    /**
     * 获取配置目录路径：{@code {user.home}/.gitee-cli/}
     */
    public static Path getConfigDir() {
        return Path.of(System.getProperty("user.home"), CONFIG_DIR_NAME);
    }

    /**
     * 获取配置文件路径。
     */
    public static Path getConfigFile() {
        return getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    // ── Token ─────────────────────────────────────────────────

    public static void saveToken(String token) {
        saveField(KEY_TOKEN, token);
    }

    public static String loadToken() {
        return loadField(KEY_TOKEN);
    }

    public static boolean deleteToken() {
        return deleteField(KEY_TOKEN);
    }

    // ── Base URL ──────────────────────────────────────────────

    public static void saveBaseUrl(String baseUrl) {
        saveField(KEY_BASE_URL, baseUrl);
    }

    /**
     * 读取配置的 Base URL，未配置时返回默认值。
     */
    public static String loadBaseUrl() {
        var url = loadField(KEY_BASE_URL);
        return (url != null && !url.isBlank()) ? url : DEFAULT_BASE_URL;
    }

    // ── Remote Pattern ────────────────────────────────────────

    public static void saveRemotePattern(String pattern) {
        saveField(KEY_REMOTE_PATTERN, pattern);
    }

    /**
     * 读取配置的 Remote Pattern，未配置时返回默认的提取所有主流 git URL 的正则。
     */
    public static String loadRemotePattern() {
        var pattern = loadField(KEY_REMOTE_PATTERN);
        return (pattern != null && !pattern.isBlank()) ? pattern : DEFAULT_REMOTE_PATTERN;
    }

    // ── 通用读写方法 ──────────────────────────────────────────

    private static void saveField(String key, String value) {
        try {
            var configDir = getConfigDir();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // 读取已有配置（保留其他字段）
            var configFile = getConfigFile();
            ObjectNode node;
            if (Files.exists(configFile)) {
                var existing = MAPPER.readTree(configFile.toFile());
                node = (existing instanceof ObjectNode obj) ? obj : MAPPER.createObjectNode();
            } else {
                node = MAPPER.createObjectNode();
            }

            node.put(key, value);
            MAPPER.writeValue(configFile.toFile(), node);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + e.getMessage(), e);
        }
    }

    private static String loadField(String key) {
        var configFile = getConfigFile();
        if (!Files.exists(configFile)) {
            return null;
        }
        try {
            var node = MAPPER.readTree(configFile.toFile());
            var fieldNode = node.path(key);
            if (fieldNode.isMissingNode() || fieldNode.asText().isBlank()) {
                return null;
            }
            return fieldNode.asText();
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean deleteField(String key) {
        var configFile = getConfigFile();
        if (!Files.exists(configFile)) {
            return false;
        }
        try {
            var node = MAPPER.readTree(configFile.toFile());
            if (node instanceof ObjectNode objectNode && objectNode.has(key)) {
                objectNode.remove(key);
                MAPPER.writeValue(configFile.toFile(), objectNode);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to update config: " + e.getMessage(), e);
        }
    }
}
