package com.gitee.cli.command.pr;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * {@code gitee pr create} — 创建合并请求。
 * <p>
 * Gitee API: POST /projects/:id/merge_requests
 */
@Command(name = "create", description = "Create a pull request.")
public class PrCreateCommand extends BaseCommand {

    @Option(names = { "--head",
            "-H" }, required = true, description = "The branch that contains commits for your pull request.")
    private String head;

    @Option(names = { "--base",
            "-B" }, required = true, description = "The branch into which you want your code merged.")
    private String base;

    @Option(names = { "--title", "-t" }, required = true, description = "Title for the pull request.")
    private String title;

    @Option(names = { "--body", "-b" }, description = "Body for the pull request.")
    private String body;

    @Option(names = "--remove-source-branch", description = "Remove source branch after merge.")
    private boolean removeSourceBranch;

    @Option(names = "--primary_reviewer_num", defaultValue = "0", description = "Minimum number of primary reviewers. Default: 1.")
    private int primaryReviewerNum;

    @Option(names = "--primary_reviewer_ids", split = ",", description = "Comma-separated primary reviewer IDs (e.g. 2,admin).")
    private String[] primaryReviewerIds;

    @Override
    public void run() {
        var repo = requireRepo();

        var payload = new HashMap<String, Object>();
        payload.put("source_branch", head);
        payload.put("target_branch", base);
        payload.put("title", title);

        if (body != null && !body.isBlank()) {
            payload.put("description", body);
        }
        if (removeSourceBranch) {
            payload.put("remove_source_branch", true);
        }

        payload.put("primary_reviewer_num", primaryReviewerNum);

        if (primaryReviewerIds != null && primaryReviewerIds.length > 0) {
            payload.put("primary_reviewer_ids", primaryReviewerIds);
        }

        var result = GiteeApiClient.getInstance().post(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests",
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Pull request created!");
            System.out.println(result.path("html_url").asText());
        }
    }
}
