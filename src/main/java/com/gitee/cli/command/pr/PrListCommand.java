package com.gitee.cli.command.pr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * {@code gitee pr list} — 列出 Pull Requests。
 */
@Command(name = "list", description = "List pull requests in a repository.")
public class PrListCommand extends BaseCommand {

    @Option(names = "--state", defaultValue = "opened", description = "Filter by state: opened, closed, merged, drafted, all. Default: opened.")
    private String state;

    @Option(names = "--source_branch", description = "source_branch. Default: null.")
    private String source_branch;

    @Option(names = "--target_branch", description = "target_branch. Default: null.")
    private String target_branch;

    @Option(names = "--search", description = "Fuzzy search PRs by keyword.")
    private String search;

    @Option(names = "--page", defaultValue = "1", description = "Page number. Default: 1.")
    private int page;

    @Option(names = "--per-page", defaultValue = "10", description = "Results per page. Default: 10.")
    private int perPage;

    @Option(names = "--sort", defaultValue = "desc", description = "Sort field: desc, asc. Default: desc.")
    private String sort;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        var repo = requireRepo();
        var parts = splitRepo(repo);
        var owner = parts[0];
        var repoName = parts[1];

        var params = new LinkedHashMap<String, String>();
        if (!"all".equals(state)) {
            params.put("state", state);
        }
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(perPage));
        params.put("sort", sort);
        if (source_branch != null && source_branch.isBlank()) {
            params.put("source_branch", source_branch);
        }
        if (target_branch != null && target_branch.isBlank()) {
            params.put("target_branch", target_branch);
        }
        if (search != null && !search.isBlank()) {
            params.put("search", search);
        }

        var result = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests", params);

        JsonNode listNode = result.has("data") ? result.path("data") : null;
        JsonNode metaNode = result.has("meta") ? result.path("meta") : null;
        boolean isJson = isJsonOutput();

        if (listNode == null || !listNode.isArray() || listNode.isEmpty()) {
            if (isJson) {
                printEmptyJson(metaNode);
            } else {
                System.out.println("No pull requests found.");
            }
            return;
        }

        if (isJson) {
            buildJsonOutput(listNode, metaNode);
        } else {
            printTerminalOutput(listNode, metaNode, owner, repoName);
        }
    }

    private void printEmptyJson(JsonNode metaNode) {
        var mapper = OutputHelper.getMapper();
        ObjectNode cleanResult = mapper.createObjectNode();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.set("list", mapper.createArrayNode());
        cleanResult.set("data", dataNode);
        if (metaNode != null && !metaNode.isMissingNode()) {
            cleanResult.set("meta", metaNode);
        }
        OutputHelper.printJson(cleanResult);
    }

    private void buildJsonOutput(JsonNode listNode, JsonNode metaNode) {
        var mapper = OutputHelper.getMapper();
        ArrayNode listArray = mapper.createArrayNode();

        for (JsonNode pr : listNode) {
            ObjectNode cleanPr = mapper.createObjectNode();
            cleanPr.put("id", pr.path("iid").asText());
            cleanPr.put("title", pr.path("title").asText());
            cleanPr.put("description", pr.path("description").asText());
            cleanPr.put("source_branch", pr.path("source_branch").asText());
            cleanPr.put("target_branch", pr.path("target_branch").asText());
            cleanPr.put("state", pr.path("state").asText());

            ArrayNode labelsArray = mapper.createArrayNode();
            JsonNode labelsNode = pr.path("labels");
            if (labelsNode != null && labelsNode.isArray()) {
                for (JsonNode label : labelsNode) {
                    labelsArray.addObject().put("name", label.path("name").asText(""));
                }
            }
            cleanPr.set("labels", labelsArray);
            cleanPr.put("updated_at", pr.path("updated_at").asText(""));
            listArray.add(cleanPr);
        }

        ObjectNode cleanResult = mapper.createObjectNode();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.set("list", listArray);
        cleanResult.set("data", dataNode);
        if (metaNode != null && !metaNode.isMissingNode()) {
            cleanResult.set("meta", metaNode);
        }
        OutputHelper.printJson(cleanResult);
    }

    private void printTerminalOutput(JsonNode listNode, JsonNode metaNode, String owner, String repoName) {
        List<String[]> rows = new ArrayList<>();

        for (JsonNode pr : listNode) {
            String iid = pr.path("iid").asText();
            String title = pr.path("title").asText();
            String stateStr = pr.path("state").asText();
            String sourceBranch = pr.path("source_branch").asText();
            String targetBranch = pr.path("target_branch").asText();
            String updatedAt = pr.path("updated_at").asText("");

            String idStr = "#" + iid;
            String updatedStr = formatDateTime(updatedAt);
            rows.add(new String[] { idStr, stateStr, title, sourceBranch, targetBranch, updatedStr });
        }

        int totalCount = metaNode != null ? metaNode.path("total_count").asInt(listNode.size()) : listNode.size();

        int idWidth = "ID".length();
        int stateWidth = "STATE".length();
        int titleWidth = "TITLE".length();
        int sourceBranchWidth = "SOURCE_BRANCH".length();
        int targetBranchWidth = "TARGET_BRANCH".length();
        for (var row : rows) {
            idWidth = Math.max(idWidth, displayWidth(row[0]));
            stateWidth = Math.max(stateWidth, displayWidth(row[1]));
            titleWidth = Math.max(titleWidth, displayWidth(row[2]));
            sourceBranchWidth = Math.max(sourceBranchWidth, displayWidth(row[3]));
            targetBranchWidth = Math.max(targetBranchWidth, displayWidth(row[4]));
        }

        System.out.printf("%nShowing %d of %d pull request(s) in %s/%s%n%n",
                rows.size(), totalCount, owner, repoName);

        System.out.printf("%-" + idWidth + "s  %-" + stateWidth + "s  %-" + titleWidth + "s  %s%n",
                "ID", "STATE", "TITLE", "UPDATED");

        for (var row : rows) {
            System.out.printf("%-" + idWidth + "s  %-" + stateWidth + "s  %-" + titleWidth + "s  %-" +
                    sourceBranchWidth + "s  %-" + targetBranchWidth + "s  %s%n",
                    row[0], row[1], row[2], row[3], row[4], row[5]);
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

    private int displayWidth(String s) {
        if (s == null)
            return 0;
        int width = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN
                    || (c >= 0xFF00 && c <= 0xFFEF)) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }
}
