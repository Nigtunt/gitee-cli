package com.gitee.cli.command.config;

import picocli.CommandLine.Command;

/**
 * {@code gitee config} — 配置管理子命令组。
 */
@Command(name = "config", mixinStandardHelpOptions = true, description = "Manage gitee-cli configuration.", subcommands = {
        ConfigSetCommand.class,
        ConfigGetCommand.class,
        ConfigListCommand.class
})
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        new picocli.CommandLine(this).usage(System.out);
    }
}
