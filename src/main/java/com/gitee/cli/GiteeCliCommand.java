package com.gitee.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

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
        // 1. 获取操作系统真正创建这个进程的绝对时间戳
        long osProcessStartTime = ProcessHandle.current().info().startInstant().get().toEpochMilli();
        // 2. 获取 main 方法开始执行的时间戳
        long mainStartTime = System.currentTimeMillis();

        // 如果启用了 debug，打印启动耗时
        if (Arrays.asList(args).contains("--debug")) {
            System.err.println("[System] OS进程分配 -> main()启动 耗时: " + (mainStartTime - osProcessStartTime) + " ms");
        }
        // 如果大模型带了 --json 参数，强行将底层输出流锁死为 UTF-8，防止解析崩溃
        if (Arrays.asList(args).contains("--json")) {
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
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
