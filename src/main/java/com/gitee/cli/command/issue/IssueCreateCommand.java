package com.gitee.cli.command.issue;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * {@code gitee issue create --title "..." --body "..."} — 创建 Issue。
 */
@Command(name = "create", description = "Create a new issue.")
public class IssueCreateCommand extends BaseCommand {

    @Option(names = { "--title", "-t" }, required = true, description = "Title of the issue.")
    private String title;

    @Option(names = { "--description", "--body", "-b" }, description = "Description of the issue.")
    private String description;

    @Option(names = "--due-date", description = "Plan finish time.")
    private String dueDate;

    @Option(names = "--plan-started-at", description = "Plan start time.")
    private String planStartedAt;

    @Option(names = "--iid", description = "Issue iid. 0 for auto-increment, >0 to set explicitly.")
    private Integer iid = 0;

    @Option(names = "--assignee", description = "Assignee ID.")
    private Integer assignee;

    @Option(names = "--labels", description = "Comma-separated list of labels.")
    private String labels;

    @Option(names = "--branch-name", description = "Related branch.")
    private String branchName;

    @Option(names = "--priority", defaultValue = "unassigned", description = "Priority: unassigned, low, medium, high, urgent.")
    private String priority;

    @Override
    public void run() {
        var repo = requireRepo();
        var parts = splitRepo(repo);
        var owner = parts[0];
        var repoName = parts[1];

        var payload = new HashMap<String, Object>();
        payload.put("title", title);

        if (description != null) {
            payload.put("description", description);
        }
        if (dueDate != null) {
            payload.put("due_date", dueDate);
        }
        if (planStartedAt != null) {
            payload.put("plan_started_at", planStartedAt);
        }

        // iid is required according to the docs
        payload.put("iid", iid != null ? iid : 0);

        if (assignee != null) {
            payload.put("assignees", new Object[] {
                    new HashMap<String, Object>() {
                        {
                            put("id", assignee);
                        }
                    }
            });
        }

        if (labels != null && !labels.isBlank()) {
            payload.put("labels", labels);
        }

        if (branchName != null && !branchName.isBlank()) {
            payload.put("branch_name", branchName);
        }

        // priority is required according to the docs
        payload.put("priority", priority != null ? priority : "unassigned");

        // Gitee v5: POST /repos/{owner}/issues — repo 在 body 中传递 (根据原逻辑保留)
        payload.put("repo", repoName);

        var result = GiteeApiClient.getInstance().post(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/issues", payload);

        String issueId = result.has("iid") ? result.path("iid").asText() : result.path("number").asText();
        System.out.println("Created issue #" + issueId + ": " + result.path("title").asText());
        if (result.has("html_url")) {
            System.out.println(result.path("html_url").asText());
        } else if (result.has("url")) {
            System.out.println(result.path("url").asText());
        }
    }
}
