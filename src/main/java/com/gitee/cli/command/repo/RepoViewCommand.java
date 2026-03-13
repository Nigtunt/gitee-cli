package com.gitee.cli.command.repo;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Option(names = { "-f", "--file" }, description = "Path to the file to view (default is README.md)")
    private String filePath;

    @Override
    public void run() {
        var repo = requireRepo();

        // GET /repos/{owner}/{repo}
        var repoInfo = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8), null);

        if (isJsonOutput()) {
            OutputHelper.printJson(repoInfo);
        } else {
            System.out.println("Repository: " + repoInfo.path("path_with_namespace").asText());
            System.out.println("Description: " + repoInfo.path("description").asText("(no description)"));
            System.out.println("Stars:       " + repoInfo.path("star_count").asInt());
            System.out.println("Forks:       " + repoInfo.path("fork_count").asInt());
            System.out.println("Open Issues: " + repoInfo.path("open_issues_count").asInt());
            System.out.println("Commits:     " + repoInfo.at("/statistics/commit_count").asInt());
            System.out.println("Tags:        " + repoInfo.at("/statistics/tag_count").asInt());
            System.out.println("Default branch: " + repoInfo.path("default_branch").asText());
            System.out.println("Last Activity:  " + repoInfo.path("last_activity_at").asText());
            System.out.println("Web URL:     " + repoInfo.path("web_url").asText());
            System.out.println("SSH URL:     " + repoInfo.path("ssh_url_to_repo").asText());
            System.out.println("HTTP URL:    " + repoInfo.path("http_url_to_repo").asText());

            // 尝试获取文件内容
            String targetFile = (filePath != null && !filePath.isEmpty()) ? filePath : "README.md";
            try {
                String encodedPath = URLEncoder.encode(targetFile, StandardCharsets.UTF_8).replace("+", "%20");
                var fileResponse = GiteeApiClient.getInstance().get(
                        "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/repository/files/"
                                + encodedPath + "?ref=" + repoInfo.path("default_branch").asText("master"),
                        null);
                var content = fileResponse.path("content").asText("");
                if (!content.isEmpty()) {
                    // Gitee 返回的 content 通常是 base64 编码
                    var decoded = new String(Base64.getDecoder().decode(content.replaceAll("\\s", "")), "GBK");
                    System.out.println();
                    System.out.println("--- " + targetFile + " ---");
                    System.out.println(decoded);
                } else {
                    System.out.println();
                    System.out.println("(" + targetFile + " is empty)");
                }
            } catch (Exception e) {
                System.out.println();
                System.out.println("(" + targetFile + " not available or not found)");
            }
        }
    }
}
