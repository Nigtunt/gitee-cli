package com.gitee.cli.command.issue;

import picocli.CommandLine.Command;

/**
 * {@code gitee issue} — Issue 子命令组。
 */
@Command(name = "issue", mixinStandardHelpOptions = true, description = "Manage issues.", subcommands = {
        IssueListCommand.class,
        IssueViewCommand.class,
        IssueCreateCommand.class,
        IssueCommentCommand.class
})
public class IssueCommand implements Runnable {

    @Override
    public void run() {
        new picocli.CommandLine(this).usage(System.out);
    }
}
