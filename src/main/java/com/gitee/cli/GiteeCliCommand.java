package com.gitee.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.gitee.cli.command.auth.AuthCommand;
import com.gitee.cli.command.config.ConfigCommand;
import com.gitee.cli.command.issue.IssueCommand;
import com.gitee.cli.command.pr.PrCommand;
import com.gitee.cli.command.repo.RepoCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 主入口命令 — {@code gitee}.
 * <p>
 * 设计为 Machine-First CLI：绝对非交互，所有参数通过命令行或环境变量传入。
 */
@Command(name = "gitee", mixinStandardHelpOptions = true, version = "gitee-cli 0.1.0", description = "A machine-first CLI for Gitee, mirroring GitHub CLI (gh) commands.", subcommands = {
        AuthCommand.class,
        ConfigCommand.class,
        IssueCommand.class,
        PrCommand.class,
        RepoCommand.class
})
public class GiteeCliCommand implements Runnable {

    // ── 全局参数 ───────────────────────────────────────────────

    @Option(names = { "--repo",
            "-R" }, description = "Target repository in <OWNER>/<REPO> format.", scope = CommandLine.ScopeType.INHERIT)
    private String repo;

    @Option(names = "--json", description = "Force all output to be valid JSON.", scope = CommandLine.ScopeType.INHERIT)
    private boolean jsonOutput;

    @Option(names = "--debug", description = "Enable debug mode: print detailed request and response info.", scope = CommandLine.ScopeType.INHERIT)
    private boolean debug;

    // ── Getter ────────────────────────────────────────────────

    public String getRepo() {
        return repo;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public boolean isDebug() {
        return debug;
    }

    // ── 入口 ──────────────────────────────────────────────────

    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();

        // 1. 拦截底层输出流：如果是 Windows，且是人类在测试，且不是 JSON 模式，强行用 GBK 翻译输出！
        if (os.contains("windows") && System.console() != null) {
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true,
                        "GBK"));
            } catch (Exception ignored) {
            }
        }
        int exitCode = new CommandLine(new GiteeCliCommand())
                .setExecutionExceptionHandler(new GlobalExceptionHandler())
                .execute(args);

        // 严格返回状态码，这对大模型 Agent 判断命令是否成功极其重要
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // 没有子命令时，打印帮助信息
        new CommandLine(this).usage(System.out);
    }
}
