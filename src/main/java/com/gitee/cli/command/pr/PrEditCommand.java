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
 * {@code gitee pr edit <number>} — 编辑合并请求。
 * <p>
 * Gitee API: PUT /projects/:id/merge_requests/:iid
 */
@Command(name = "edit", description = "Edit a pull request.")
public class PrEditCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Option(names = {"--title", "-t"}, description = "Title for the pull request.")
    private String title;

    @Option(names = {"--body", "-b"}, description = "Body for the pull request.")
    private String body;

    @Option(names = "--state-event", defaultValue = "opened", description = "State event: closed, opened, drafted. Default: opened.")
    private String stateEvent;

    private static final java.util.List<String> VALID_STATE_EVENTS = java.util.List.of("closed", "opened", "drafted");

    @Override
    public void run() {
        if (!VALID_STATE_EVENTS.contains(stateEvent)) {
            throw new IllegalArgumentException("Invalid state event: " + stateEvent + ". Allowed values are closed, opened, drafted.");
        }

        var repo = requireRepo();

        var payload = new HashMap<String, Object>();
        payload.put("state_event", stateEvent);
        if (title != null && !title.isBlank()) {
            payload.put("title", title);
        }
        if (body != null && !body.isBlank()) {
            payload.put("description", body);
        }

        var result = GiteeApiClient.getInstance().put(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number,
                payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Pull request edited.");
        }
    }
}
