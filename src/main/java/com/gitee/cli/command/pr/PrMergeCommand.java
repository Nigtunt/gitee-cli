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
 * {@code gitee pr merge <number>} — 通过合并请求。
 * <p>
 * Gitee API: PUT /projects/:id/merge_requests/:iid/merge
 */
@Command(name = "merge", description = "Merge a pull request.")
public class PrMergeCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Option(names = "--merge-type", defaultValue = "merge", description = "Merge type: merge, squash, fast_forward, rebase. Default: merge.")
    private String mergeType;

    @Option(names = "--merge-commit-message", description = "Merge commit message.")
    private String mergeCommitMessage;

    @Option(names = "--squash", description = "If true, the commits will be squashed into a single commit.")
    private boolean squash;

    @Override
    public void run() {
        var repo = requireRepo();

        var payload = new HashMap<String, Object>();
        payload.put("merge_type", mergeType);
        
        if (mergeCommitMessage != null && !mergeCommitMessage.isBlank()) {
            payload.put("merge_commit_message", mergeCommitMessage);
        }
        if (squash) {
            payload.put("squash", true);
        }

        var result = GiteeApiClient.getInstance().put(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number + "/merge",
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Pull request merged.");
        }
    }
}
