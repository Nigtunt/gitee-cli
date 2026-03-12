package com.gitee.cli.command.auth;

import com.gitee.cli.AnsiColor;
import com.gitee.cli.ConfigManager;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;

/**
 * {@code gitee auth logout} — 删除已保存的 Token。
 */
@Command(name = "logout", description = "Remove saved authentication token.")
public class AuthLogoutCommand extends BaseCommand {

    @Override
    public void run() {
        var deleted = ConfigManager.deleteToken();
        if (deleted) {
            System.out.println(AnsiColor.success("Token removed from: " + ConfigManager.getConfigFile()));
            System.out.println(AnsiColor.dim("  You are now logged out."));
        } else {
            System.out.println(AnsiColor.yellow("No saved token found. Already logged out."));
        }
    }
}
