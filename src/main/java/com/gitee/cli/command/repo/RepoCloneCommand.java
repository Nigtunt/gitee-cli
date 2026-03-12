package com.gitee.cli.command.repo;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

/**
 * {@code gitee repo clone} — 克隆仓库到本地。
 * <p>
 * 先通过 GET /repos/{owner}/{repo} 获取 clone URL，
 * 再调用系统 {@code git clone} 命令执行克隆。
 * <p>
 * 默认使用 HTTPS clone URL，可通过 {@code --ssh} 切换为 SSH URL。
 */
@Command(name = "clone", description = "Clone a repository to local filesystem.")
public class RepoCloneCommand extends BaseCommand {

    @Option(names = "--ssh", description = "Use SSH URL instead of HTTPS. Default: false.")
    private boolean useSsh;

    @Option(names = "--depth", description = "Create a shallow clone with the given depth.")
    private Integer depth;

    @Override
    public void run() {
        var repo = requireRepo();
        var parts = splitRepo(repo);
        var owner = parts[0];
        var repoName = parts[1];

        // GET /repos/{owner}/{repo} 获取 clone URL
        var repoInfo = GiteeApiClient.getInstance().get(
                "/projects/" + owner + "/" + repoName,
                null);

        var cloneUrl = useSsh
                ? repoInfo.path("ssh_url").asText()
                : repoInfo.path("clone_url").asText();

        if (cloneUrl == null || cloneUrl.isBlank()) {
            throw new IllegalStateException("Could not determine clone URL for " + repo);
        }

        // 构建 git clone 命令
        var command = new java.util.ArrayList<String>();
        command.add("git");
        command.add("clone");
        if (depth != null && depth > 0) {
            command.add("--depth");
            command.add(String.valueOf(depth));
        }
        command.add(cloneUrl);

        System.out.println("Cloning " + cloneUrl + " ...");

        try {
            var process = new ProcessBuilder(command)
                    .inheritIO()
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("git clone exited with code " + exitCode);
            }
            System.out.println("Clone complete.");
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to execute git clone. Make sure git is installed and on PATH.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("git clone was interrupted.", e);
        }
    }
}
