package com.gitee.cli.command.repo;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;

import java.util.Base64;

/**
 * {@code gitee repo view} — 查看仓库基本信息和 README。
 * <p>
 * Gitee API:
 * <ul>
 * <li>GET /repos/{owner}/{repo} — 仓库元信息</li>
 * <li>GET /repos/{owner}/{repo}/readme — README 内容（base64 编码）</li>
 * </ul>
 */
@Command(name = "view", description = "View repository information and README.")
public class RepoViewCommand extends BaseCommand {

    @Override
    public void run() {
        var repo = requireRepo();
        var parts = splitRepo(repo);
        var owner = parts[0];
        var repoName = parts[1];

        // GET /repos/{owner}/{repo}
        var repoInfo = GiteeApiClient.getInstance().get(
                "/repos/" + owner + "/" + repoName,
                null);

        if (isJsonOutput()) {
            OutputHelper.printJson(repoInfo);
        } else {
            System.out.println("Repository: " + repoInfo.path("full_name").asText());
            System.out.println("Description: " + repoInfo.path("description").asText("(no description)"));
            System.out.println("Stars:     " + repoInfo.path("stargazers_count").asInt());
            System.out.println("Forks:     " + repoInfo.path("forks_count").asInt());
            System.out.println("Watchers:  " + repoInfo.path("watchers_count").asInt());
            System.out.println("Language:  " + repoInfo.path("language").asText("N/A"));
            System.out.println("Default branch: " + repoInfo.path("default_branch").asText());
            System.out.println("URL:       " + repoInfo.path("html_url").asText());
            System.out.println("SSH URL:   " + repoInfo.path("ssh_url").asText());
            System.out.println("Clone URL: " + repoInfo.path("clone_url").asText());

            // 尝试获取 README
            try {
                var readme = GiteeApiClient.getInstance().get(
                        "/repos/" + owner + "/" + repoName + "/readme",
                        null);
                var content = readme.path("content").asText("");
                if (!content.isEmpty()) {
                    // Gitee 返回的 content 是 base64 编码
                    var decoded = new String(
                            Base64.getMimeDecoder().decode(content));
                    System.out.println();
                    System.out.println("--- README ---");
                    System.out.println(decoded);
                }
            } catch (Exception e) {
                // README 不存在或不可读，忽略
                System.out.println();
                System.out.println("(README not available)");
            }
        }
    }
}
