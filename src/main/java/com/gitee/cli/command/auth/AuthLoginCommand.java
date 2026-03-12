package com.gitee.cli.command.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitee.cli.AnsiColor;
import com.gitee.cli.ConfigManager;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * {@code gitee auth login --token <token>} — 登录并持久化 Token。
 * <p>
 * 会先通过 GET /user 验证 Token 有效性，验证通过后保存到配置文件。
 */
@Command(name = "login", description = "Authenticate with a Gitee personal access token.")
public class AuthLoginCommand extends BaseCommand {

    @Option(names = { "--token", "-t" }, required = true, description = "Gitee personal access token.")
    private String token;

    @Override
    public void run() {
        // 1. 先验证 token 是否有效：调用 GET /user
        System.out.println(AnsiColor.info("Verifying token..."));

        var testClient = GiteeApiClient.withToken(token);
        JsonNode user;
        try {
            user = testClient.get("/user", null);
        } catch (Exception e) {
            System.err.println(AnsiColor.error("Token verification failed — " + e.getMessage()));
            System.err.println(AnsiColor.dim("  Please check your token and try again."));
            return;
        }

        var login = user.path("id").asText("");
        if (login.isEmpty()) {
            System.err.println(AnsiColor.error("Token appears invalid — could not retrieve user info."));
            return;
        }

        // 2. 验证通过，保存 token
        ConfigManager.saveToken(token);

        // 3. 打印用户信息
        var username = user.path("username").asText("");
        var name = user.path("name").asText("");
        var email = user.path("email").asText("");
        var gitEmail = user.path("git_email").asText("");

        System.out.println(AnsiColor.success("Login successful!"));
        System.out.println(AnsiColor.info("  Username : " + AnsiColor.bold(username)));
        System.out.println(AnsiColor.info("  Name     : " + AnsiColor.bold(name)));
        System.out.println(AnsiColor.info("  Email    : " + AnsiColor.bold(email)));
        System.out.println(AnsiColor.info("  Git Email: " + AnsiColor.bold(gitEmail)));
        System.out.println(AnsiColor.dim("  Token saved to: " + ConfigManager.getConfigFile()));
    }
}
