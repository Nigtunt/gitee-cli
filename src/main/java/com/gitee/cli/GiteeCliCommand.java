package com.gitee.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

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
        // 1. 抢在 JAnsi 之前，进行最高级别的底层流拦截！
        String os = System.getProperty("os.name").toLowerCase();

        // 只有在 Windows 系统，且是真实人类在 CMD 里敲命令时 (System.console() != null)，才转成 GBK。
        // 如果是大模型通过 Python 脚本在后台静默调用 (返回 null)，则保持原生 UTF-8 不变！
        if (os.contains("windows") && System.console() != null) {
            try {
                // 使用 FileDescriptor.out 绕过 JDK 21 的默认包装，拿到最纯粹的系统输出管道，强制烙上 GBK 编码
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "GBK"));
            } catch (UnsupportedEncodingException e) {
                // 静默忽略，GBK 在 Windows 上一定存在
            }
        }

        // 2. 底层流的编码已经彻底干净了，现在轮到 JAnsi 闪亮登场 (把原本 static 里的代码移到这里)
        try {
            org.fusesource.jansi.AnsiConsole.systemInstall();
        } catch (Exception ignored) {
            // 在 GraalVM 原生镜像中，如果 jansi.dll 没打进去，这里会静默失败，不影响核心功能
        }
        int exitCode = new CommandLine(new GiteeCliCommand())
                .setExecutionExceptionHandler(new GlobalExceptionHandler())
                .execute(args);
        System.exit(exitCode);

        // 4. 优雅卸载 JAnsi
        try {
            org.fusesource.jansi.AnsiConsole.systemUninstall();
        } catch (Exception ignored) {
        }

        // 严格返回状态码，这对大模型 Agent 判断命令是否成功极其重要
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // 没有子命令时，打印帮助信息
        new CommandLine(this).usage(System.out);
    }
}
