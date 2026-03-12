package com.gitee.cli.command;

import com.gitee.cli.GiteeCliCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.gitee.cli.ConfigManager;

/**
 * 命令基类 — 提供对全局参数的便捷访问。
 * <p>
 * 子类通过 {@link CommandSpec} 向上查找根命令 {@link GiteeCliCommand}。
 */
@Command(mixinStandardHelpOptions = true)
public abstract class BaseCommand implements Runnable {

    @Spec
    protected CommandSpec spec;

    /**
     * 向上递归找到根命令 {@link GiteeCliCommand}，获取全局参数。
     */
    protected GiteeCliCommand root() {
        var current = spec;
        while (current != null) {
            if (current.userObject() instanceof GiteeCliCommand root) {
                // 将全局 debug 标记同步给 API 客户端（因为客户端是单例/静态方法，这里是最早能拿到解析后参数的地方）
                com.gitee.cli.GiteeApiClient.setDebug(root.isDebug());
                return root;
            }
            current = current.parent();
        }
        throw new IllegalStateException("Could not locate root GiteeCliCommand.");
    }

    /**
     * 获取 --repo 全局参数；若未通过命令行指定，则自动从当前目录的
     * git remote origin URL 中推断 {@code owner/repo}。
     */
    protected String requireRepo() {
        var repo = root().getRepo();
        if (repo == null || repo.isBlank()) {
            repo = detectRepoFromGit();
        }
        if (repo == null || repo.isBlank()) {
            throw new IllegalArgumentException("Missing required option: --repo <owner/repo>. "
                    + "Specify the target repository with -R or --repo, "
                    + "or run from within a Gitee git repository.");
        }
        return repo;
    }

    /**
     * 将 owner/repo 拆分成 [owner, repo]。
     */
    protected String[] splitRepo(String repo) {
        var parts = repo.split("/", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid repo format: '" + repo + "'. Expected format: OWNER/REPO");
        }
        return parts;
    }

    protected boolean isJsonOutput() {
        return root().isJsonOutput();
    }

    protected boolean isDebug() {
        return root().isDebug();
    }

    // ── 内部方法 ──────────────────────────────────────────────

    /**
     * 通过 {@code git remote get-url origin} 获取远程 URL，
     * 解析出 Gitee 的 owner/repo。
     *
     * @return 解析成功返回 "owner/repo"，否则返回 null
     */
    private static String detectRepoFromGit() {
        try {
            var process = new ProcessBuilder("git", "remote", "get-url", "origin").redirectErrorStream(true).start();
            String url;
            try (var reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                url = reader.readLine();
            }
            int exitCode = process.waitFor();
            if (exitCode != 0 || url == null || url.isBlank()) {
                return null;
            }

            var patternStr = ConfigManager.loadRemotePattern();
            Pattern pattern;
            try {
                pattern = Pattern.compile(patternStr);
            } catch (PatternSyntaxException e) {
                System.err.println("Warning: Invalid remote_pattern in config: " + e.getMessage());
                return null;
            }

            var matcher = pattern.matcher(url.trim());
            if (matcher.find()) {
                // The owner/repo matches are usually expected in group 1 if using (.*?),
                // or group 1 and 2 if explicitly matching ([^/]+)/([^/.]+).
                // Our new default pattern puts the whole path in group 1: (.*?)
                // Legacy pattern puts it in group 1 and 2: ([^/]+)/([^/.]+)
                if (matcher.groupCount() >= 2 && patternStr.contains("([^/]+)/")) {
                    return matcher.group(1) + "/" + matcher.group(2);
                } else if (matcher.groupCount() >= 1) {
                    return matcher.group(1);
                }
            }
            return null;
        } catch (Exception e) {
            // git 不在 PATH、或当前目录不是 git 仓库 — 静默返回 null
            return null;
        }
    }
}
