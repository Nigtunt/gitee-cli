package com.gitee.cli.command.issue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * {@code gitee issue close <number>} — 关闭 Issue。
 * <p>
 * Gitee API: PUT /projects/{owner/repo}/issues/{number}
 * state=rejected
 */
@Command(name = "close", description = "Close an issue.")
public class IssueCloseCommand extends BaseCommand {

    @Parameters(index = "0", description = "Issue number (e.g. I1234A).")
    private String number;

    @Override
    public void run() {
        var repo = requireRepo();

        var payload = new HashMap<String, Object>();
        payload.put("state", "rejected");

        // PUT /projects/{owner/repo}/issues/{number}
        var result = GiteeApiClient.getInstance().put(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/issues/" + number,
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            if (result != null && result.has("iid")) {
                System.out.println("Closed issue " + result.path("iid").asText());
            } else {
                System.err.println("Failed to close issue " + number);
            }
        }
    }
}
