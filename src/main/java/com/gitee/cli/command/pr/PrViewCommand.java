package com.gitee.cli.command.pr;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@code gitee pr view <number>} — 查看 PR 的标题、描述和状态。
 * <p>
 * Gitee API: GET /projects/:id/merge_requests/:iid
 */
@Command(name = "view", description = "View details of a pull request.")
public class PrViewCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        var repo = requireRepo();

        // GET /projects/:id/merge_requests/:iid
        var result = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number,
                null);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("PR #" + result.path("iid").asText());
            System.out.println("Title:       " + result.path("title").asText());
            System.out.println("State:       " + result.path("state").asText());
            System.out.println("Author:      " + result.path("author").path("name").asText("Unknown"));
            System.out.println("Created:     " + formatDateTime(result.path("created_at").asText()));
            System.out.println("Updated:     " + formatDateTime(result.path("updated_at").asText()));

            String headLabel = result.has("source_branch") ? result.path("source_branch").asText()
                    : result.path("head").path("label").asText("");
            String baseLabel = result.has("target_branch") ? result.path("target_branch").asText()
                    : result.path("base").path("label").asText("");
            System.out.println("Branch:      " + headLabel + " -> " + baseLabel);

            System.out.println("URL:         " + result.path("web_url").asText());

            var body = result.has("body") ? result.path("body").asText("") : result.path("description").asText("");
            if (!body.isEmpty()) {
                System.out.println();
                System.out.println("--- Description ---");
                System.out.println(body);
            }
        }
    }

    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "";
        }
        try {
            var dt = OffsetDateTime.parse(isoDateTime, INPUT_FORMAT);
            return dt.format(OUTPUT_FORMAT);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
}
