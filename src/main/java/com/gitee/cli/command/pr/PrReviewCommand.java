package com.gitee.cli.command.pr;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * {@code gitee pr review <number> --body "..."} — 提交 PR 审查意见（用于 LLM 提交 Code Review）。
 */
@Command(name = "review", description = "Submit a review comment on a pull request.")
public class PrReviewCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Option(names = { "--body", "-b" }, required = false, description = "Review comment body.")
    private String body;

    @Option(names = "--state", defaultValue = "commented", description = "Review state: commented, approved, rejected, initialized. Default: commented.")
    private String state;

    private static final java.util.List<String> VALID_STATES = java.util.List.of("commented", "approved", "rejected", "initialized");

    @Override
    public void run() {
        if (!VALID_STATES.contains(state)) {
            throw new IllegalArgumentException("Invalid state: " + state + ". Allowed values are commented, approved, rejected, initialized.");
        }

        var repo = requireRepo();

        // Gitee v5: POST /projects/:id/merge_requests/:iid/reviews
        var payload = new HashMap<String, Object>();
        payload.put("state", state);
        if (body != null && !body.isBlank()) {
            payload.put("comment", body);
        }

        var result = GiteeApiClient.getInstance().post(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number + "/reviews",
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Review submitted on PR #" + number + " with state: " + state);
        }
    }
}
