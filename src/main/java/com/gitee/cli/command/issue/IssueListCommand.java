package com.gitee.cli.command.issue;

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
 * {@code gitee issue list --state open} — 列出 Issues。
 */
@Command(name = "list", description = "List issues in a repository.")
public class IssueListCommand extends BaseCommand {

    @Option(names = "--state", defaultValue = "created", description = "Filter by state: created, processing, rejected, finished, all. Default: created.")
    private String state;

    @Option(names = "--search", description = "Fuzzy search issues by keyword.")
    private String search;

    @Option(names = "--page", defaultValue = "1", description = "Page number. Default: 1.")
    private int page;

    @Option(names = "--per-page", defaultValue = "30", description = "Results per page. Default: 30.")
    private int perPage;

    @Option(names = "--sort", defaultValue = "created", description = "Sort field: created, updated. Default: created.")
    private String sort;

    @Option(names = "--direction", defaultValue = "desc", description = "Sort direction: asc, desc. Default: desc.")
    private String direction;

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
        params.put("direction", direction);
        if (search != null && !search.isBlank()) {
            params.put("search", search);
        }

        var result = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/issues", params);

        // 解析新数据结构: {data: {list: [...]}, meta: {current_page, total_count, total_pages}}
        JsonNode listNode = result.path("data").path("list");
        JsonNode metaNode = result.path("meta");
        boolean isJson = isJsonOutput();

        if (listNode == null || !listNode.isArray() || listNode.isEmpty()) {
            if (isJson) {
                printEmptyJson(metaNode);
            } else {
                System.out.println("No issues found.");
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

        for (JsonNode issue : listNode) {
            ObjectNode cleanIssue = mapper.createObjectNode();
            cleanIssue.put("id", issue.path("iid").asText());
            cleanIssue.put("title", issue.path("title").asText());
            cleanIssue.put("description", issue.has("body") ? issue.path("body").asText() : issue.path("description").asText());
            
            ArrayNode labelsArray = mapper.createArrayNode();
            JsonNode labelsNode = issue.path("labels");
            if (labelsNode != null && labelsNode.isArray()) {
                for (JsonNode label : labelsNode) {
                    labelsArray.addObject().put("name", label.path("name").asText(""));
                }
            }
            cleanIssue.set("labels", labelsArray);
            cleanIssue.put("updated_at", issue.path("updated_at").asText(""));
            listArray.add(cleanIssue);
        }

        ObjectNode cleanResult = mapper.createObjectNode();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.set("list", listArray);
        cleanResult.set("data", dataNode);
        cleanResult.set("meta", metaNode);
        OutputHelper.printJson(cleanResult);
    }

    private void printTerminalOutput(JsonNode listNode, JsonNode metaNode, String owner, String repoName) {
        List<String[]> rows = new ArrayList<>();

        for (JsonNode issue : listNode) {
            String iid = issue.path("iid").asText();
            String title = issue.path("title").asText();
            String updatedAt = issue.path("updated_at").asText("");
            JsonNode labelsNode = issue.path("labels");

            String idStr = "#" + iid;
            String labelsStr = formatLabels(labelsNode);
            String updatedStr = formatDateTime(updatedAt);
            rows.add(new String[] { idStr, title, labelsStr, updatedStr });
        }

        int totalCount = metaNode.path("total_count").asInt(0);

        // 计算各列最大宽度
        int idWidth = "ID".length();
        int titleWidth = "TITLE".length();
        int labelsWidth = "LABELS".length();
        for (var row : rows) {
            idWidth = Math.max(idWidth, displayWidth(row[0]));
            titleWidth = Math.max(titleWidth, displayWidth(row[1]));
            labelsWidth = Math.max(labelsWidth, displayWidth(row[2]));
        }

        // 打印摘要行
        System.out.printf("%nShowing %d of %d issue(s) in %s/%s%n%n",
                rows.size(), totalCount, owner, repoName);

        // 打印表头
        System.out.printf("%-" + idWidth + "s  %-" + titleWidth + "s  %-" + labelsWidth + "s  %s%n",
                "ID", "TITLE", "LABELS", "UPDATED");

        // 打印数据行
        for (var row : rows) {
            System.out.printf("%-" + idWidth + "s  %-" + titleWidth + "s  %-" + labelsWidth + "s  %s%n",
                    row[0], row[1], row[2], row[3]);
        }
    }

    /**
     * 将 labels 数组 [{name:"缺陷"}, {name:"bug"}] 格式化为逗号分隔的字符串。
     */
    private String formatLabels(JsonNode labelsNode) {
        if (labelsNode == null || !labelsNode.isArray() || labelsNode.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (var label : labelsNode) {
            String name = label.path("name").asText("");
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return String.join(", ", names);
    }

    /**
     * 将 ISO 8601 日期时间字符串格式化为 yyyy-MM-dd HH:mm:ss。
     */
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

    /**
     * 计算字符串的显示宽度（中文字符占2个宽度）。
     */
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
