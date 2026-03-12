package com.gitee.cli.command.pr;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * {@code gitee pr diff <number>} — 获取 PR 的代码差异（用于 LLM 代码审查）。
 */
@Command(name = "diff", description = "View the diff of a pull request (useful for LLM code review).")
public class PrDiffCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Override
    public void run() {
        var repo = requireRepo();

        // Gitee v5: GET /projects/:id/merge_requests/:iid/diffs 获取 PR 文件变更
        var result = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number + "/diffs",
                null);

        // 如果结果包裹在 data 中
        JsonNode files = result.has("data") ? result.path("data") : result;

        if (isJsonOutput()) {
            OutputHelper.printJson(files);
        } else {
            if (files.isArray() && !files.isEmpty()) {
                for (var diff : files) {
                    System.out.println("--- a/" + diff.path("old_path").asText(""));
                    System.out.println("+++ b/" + diff.path("new_path").asText(""));
                    var patch = diff.path("diff").asText("");
                    if (!patch.isEmpty()) {
                        System.out.println(patch);
                    }
                    System.out.println();
                }
            } else {
                System.out.println("No diffs found or PR is empty.");
            }
        }
    }
}
