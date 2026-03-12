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
 * {@code gitee pr comment <number> --body "..."} — 在 PR 下方发表普通评论。
 * <p>
 * Gitee API: POST /projects/:id/merge_requests/:iid/notes
 */
@Command(name = "comment", description = "Add a comment to a pull request.")
public class PrCommentCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Option(names = { "--body", "-b" }, required = true, description = "Comment body text.")
    private String body;

    @Override
    public void run() {
        var repo = requireRepo();

        // POST /projects/:id/merge_requests/:iid/notes
        var payload = new HashMap<String, Object>();
        payload.put("note", body);
        payload.put("noteable_type", "PullRequest");

        var result = GiteeApiClient.getInstance().post(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number + "/notes",
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Comment posted on PR #" + number);
        }
    }
}
