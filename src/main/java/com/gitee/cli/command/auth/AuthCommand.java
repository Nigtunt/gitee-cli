package com.gitee.cli.command.auth;

import picocli.CommandLine.Command;

/**
 * {@code gitee auth} — 认证相关子命令组。
 */
@Command(name = "auth", mixinStandardHelpOptions = true, description = "Manage authentication.", subcommands = {
        AuthLoginCommand.class,
        AuthLogoutCommand.class,
        AuthStatusCommand.class
})
public class AuthCommand implements Runnable {

    @Override
    public void run() {
        new picocli.CommandLine(this).usage(System.out);
    }
}
