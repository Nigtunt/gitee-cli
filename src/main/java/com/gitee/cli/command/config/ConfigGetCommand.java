package com.gitee.cli.command.config;

import com.gitee.cli.ConfigManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * {@code gitee config get <key>} — 查看配置项的值。
 */
@Command(name = "get", mixinStandardHelpOptions = true, description = "Get a configuration value. Keys: base_url, token, remote_pattern.")
public class ConfigGetCommand implements Runnable {

    @Parameters(index = "0", description = "Configuration key (e.g. base_url, token, remote_pattern).")
    private String key;

    @Override
    public void run() {
        switch (key) {
            case "base_url" -> System.out.println(ConfigManager.loadBaseUrl());
            case "token" -> {
                var token = ConfigManager.loadToken();
                if (token != null) {
                    // 遮罩输出
                    System.out.println(maskToken(token));
                } else {
                    System.out.println("(not set)");
                }
            }
            case "remote_pattern" -> System.out.println(ConfigManager.loadRemotePattern());
            default -> {
                System.err.println("Unknown config key: " + key);
                System.err.println("  Available keys: base_url, token, remote_pattern");
            }
        }
    }

    private static String maskToken(String token) {
        if (token.length() <= 8)
            return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
