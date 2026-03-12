package com.gitee.cli.command.auth;

import com.gitee.cli.AnsiColor;
import com.gitee.cli.ConfigManager;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;

/**
 * {@code gitee auth status} — 显示当前认证状态。
 */
@Command(name = "status", description = "View authentication status.")
public class AuthStatusCommand extends BaseCommand {

    @Override
    public void run() {
        // 检查环境变量
        var envToken = System.getenv("GITEE_TOKEN");
        if (envToken != null && !envToken.isBlank()) {
            System.out.println(AnsiColor.success("Authenticated via environment variable GITEE_TOKEN"));
            System.out.println(AnsiColor.dim("  Token: " + maskToken(envToken)));
            return;
        }

        // 检查配置文件
        var fileToken = ConfigManager.loadToken();
        if (fileToken != null && !fileToken.isBlank()) {
            System.out.println(AnsiColor.success("Authenticated via config file"));
            System.out.println(AnsiColor.dim("  Token:  " + maskToken(fileToken)));
            System.out.println(AnsiColor.dim("  Config: " + ConfigManager.getConfigFile()));
            return;
        }

        // 未认证
        System.out.println(AnsiColor.error("Not authenticated."));
        System.out.println(AnsiColor.yellow("  Run 'gitee auth login --token <your_token>' to authenticate."));
    }

    /**
     * 遮罩 token，只显示前4位和后4位。
     */
    private static String maskToken(String token) {
        if (token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
