package com.gitee.cli.command.pr;

import picocli.CommandLine.Command;

/**
 * {@code gitee pr} — Pull Request 子命令组。
 */
@Command(name = "pr", mixinStandardHelpOptions = true, description = "Manage pull requests.", subcommands = {
        PrListCommand.class,
        PrViewCommand.class,
        PrCreateCommand.class,
        PrEditCommand.class,
        PrCloseCommand.class,
        PrReopenCommand.class,
        PrMergeCommand.class,
        PrDiffCommand.class,
        PrReviewCommand.class,
        PrCommentCommand.class
})
public class PrCommand implements Runnable {

    @Override
    public void run() {
        new picocli.CommandLine(this).usage(System.out);
    }
}
