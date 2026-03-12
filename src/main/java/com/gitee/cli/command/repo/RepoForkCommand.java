package com.gitee.cli.command.repo;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;

import java.util.HashMap;

/**
 * {@code gitee repo fork} — Fork 仓库到当前认证用户名下。
 * <p>
 * Gitee API: POST /repos/{owner}/{repo}/forks
 */
@Command(name = "fork", description = "Fork a repository to your account.")
public class RepoForkCommand extends BaseCommand {

    @Override
    public void run() {
        var repo = requireRepo();
        var parts = splitRepo(repo);
        var owner = parts[0];
        var repoName = parts[1];

        // POST /repos/{owner}/{repo}/forks
        var payload = new HashMap<String, Object>();

        var result = GiteeApiClient.getInstance().post(
                "/repos/" + owner + "/" + repoName + "/forks",
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Forked " + repo + " → " + result.path("full_name").asText());
            System.out.println(result.path("html_url").asText());
        }
    }
}
