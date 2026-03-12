package com.gitee.cli.command.config;

import com.gitee.cli.AnsiColor;
import com.gitee.cli.ConfigManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * {@code gitee config set <key> <value>} — 设置配置项。
 * <p>
 * 支持的 key：
 * <ul>
 * <li>{@code base_url} — API 基础地址（默认 https://gitee.com/api/v5）</li>
 * <li>{@code token} — 认证 Token（建议使用 {@code gitee auth login} 代替）</li>
 * <li>{@code remote_pattern} — Git 远程仓库提取正则</li>
 * </ul>
 */
@Command(name = "set", mixinStandardHelpOptions = true, description = "Set a configuration value. Keys: base_url, token, remote_pattern.")
public class ConfigSetCommand implements Runnable {

    @Parameters(index = "0", description = "Configuration key (e.g. base_url, token, remote_pattern).")
    private String key;

    @Parameters(index = "1", description = "Configuration value.")
    private String value;

    @Override
    public void run() {
        switch (key) {
            case "base_url" -> {
                ConfigManager.saveBaseUrl(value);
                System.out.println(AnsiColor.success("base_url = " + value));
            }
            case "token" -> {
                ConfigManager.saveToken(value);
                System.out.println(AnsiColor.success("token saved (use 'gitee auth login' for validation)"));
            }
            case "remote_pattern" -> {
                ConfigManager.saveRemotePattern(value);
                System.out.println(AnsiColor.success("remote_pattern = " + value));
            }
            default -> {
                System.err.println(AnsiColor.error("Unknown config key: " + key));
                System.err.println(AnsiColor.dim("  Available keys: base_url, token, remote_pattern"));
            }
        }
    }
}
