package com.gitee.cli.command.repo;

import picocli.CommandLine.Command;

/**
 * {@code gitee repo} — 仓库操作子命令组。
 */
@Command(name = "repo", mixinStandardHelpOptions = true, description = "Manage repositories.", subcommands = {
        RepoViewCommand.class,
        RepoCloneCommand.class
})
public class RepoCommand implements Runnable {

    @Override
    public void run() {
        new picocli.CommandLine(this).usage(System.out);
    }
}
