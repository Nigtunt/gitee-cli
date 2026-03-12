package com.gitee.cli.command.config;

import com.gitee.cli.ConfigManager;
import picocli.CommandLine.Command;

import java.nio.file.Files;

/**
 * {@code gitee config list} — 列出所有配置项。
 */
@Command(name = "list", mixinStandardHelpOptions = true, description = "List all configuration values.")
public class ConfigListCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("测试中文乱码Config file: " + ConfigManager.getConfigFile());
        System.out.println("Exists:      " + Files.exists(ConfigManager.getConfigFile()));
        System.out.println();

        // base_url
        System.out.println("base_url" + "       = " + ConfigManager.loadBaseUrl());

        // remote_pattern
        System.out.println("remote_pattern" + " = " + ConfigManager.loadRemotePattern());

        // token
        var token = ConfigManager.loadToken();
        if (token != null) {
            System.out.println("token" + "          = " + maskToken(token));
        } else {
            System.out.println("token" + "          = " + "(not set)");
        }
    }

    private static String maskToken(String token) {
        if (token.length() <= 8)
            return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
