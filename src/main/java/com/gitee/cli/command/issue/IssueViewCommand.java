package com.gitee.cli.command.issue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.cli.AnsiColor;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * {@code gitee issue view <number>} — 查看 Issue 的详细信息。
 * <p>
 * Gitee API: GET /projects/{owner/repo}/issues/{number}
 */
@Command(name = "view", description = "View details of an issue.")
public class IssueViewCommand extends BaseCommand {

    @Parameters(index = "0", description = "Issue number (e.g. I1234A).")
    private String number;

    @Option(names = { "--comments", "-c" }, description = "View the full conversation including all comments.")
    private boolean showComments;

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        var repo = requireRepo();

        // GET /projects/{owner/repo}/issues/{number}
        var result = GiteeApiClient.getInstance().get(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/issues/" + number,
                null);

        if (showComments && result instanceof ObjectNode objNode) {
            String discussionsUrl = "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8)
                    + "/issues/" + number + "/discussions";
            JsonNode comments = GiteeApiClient.getInstance().get(discussionsUrl, null);
            if (comments != null && comments.isArray()) {
                objNode.set("notes", comments);
            }
        }

        if (isJsonOutput()) {
            buildJsonOutput(result, repo);
        } else {
            printTerminalOutput(result, repo);
        }
    }

    private void buildJsonOutput(JsonNode result, String repo) {
        String iid = result.path("iid").asText();
        String title = result.path("title").asText("");
        String state = result.path("state").asText("");
        String priority = result.path("priority").asText("");
        String authorName = result.path("author").path("name").asText("");
        String createdAt = result.path("created_at").asText("");
        String rawDescription = result.has("body") ? result.path("body").asText("")
                : result.path("description").asText("");
        JsonNode labelsNode = result.path("labels");
        JsonNode notesNode = result.path("notes");

        var mapper = OutputHelper.getMapper();
        ObjectNode cleanResult = mapper.createObjectNode();

        cleanResult.put("id", iid);
        cleanResult.put("title", title);
        cleanResult.put("description", rawDescription);
        cleanResult.put("state", state);
        cleanResult.put("priority", priority);
        cleanResult.put("author", authorName);
        cleanResult.put("created_at", createdAt);

        ArrayNode labelsArray = mapper.createArrayNode();
        if (labelsNode != null && labelsNode.isArray()) {
            for (JsonNode label : labelsNode) {
                labelsArray.addObject().put("name", label.path("name").asText(""));
            }
        }
        cleanResult.set("labels", labelsArray);

        // 包含评论内容
        ArrayNode commentsArray = mapper.createArrayNode();
        if (notesNode != null && notesNode.isArray()) {
            for (JsonNode note : notesNode) {
                ObjectNode cleanNote = mapper.createObjectNode();
                cleanNote.put("author", note.path("author").path("name").asText(""));
                cleanNote.put("created_at", note.path("created_at").asText(""));
                cleanNote.put("body", note.has("body") ? note.path("body").asText("") : note.path("note").asText(""));
                commentsArray.add(cleanNote);
            }
        }
        cleanResult.set("comments", commentsArray);

        OutputHelper.printJson(cleanResult);
    }

    private void printTerminalOutput(JsonNode result, String repo) {
        String title = result.path("title").asText("");
        String ident = result.path("ident").asText("");
        String state = result.path("state").asText("");
        String priority = result.path("priority").asText("");
        String authorName = result.path("author").path("name").asText("");
        String createdAt = result.path("created_at").asText("");
        String projectFullName = result.path("jump_info").path("full_name").asText(repo);
        String rawDescription = result.has("body") ? result.path("body").asText("")
                : result.path("description").asText("");
        JsonNode labelsNode = result.path("labels");
        JsonNode notesNode = result.path("notes");
        int noteCount = result.path("noteable_count").asInt(0);

        String description = stripHtml(rawDescription);

        // ── 第 1 行: title project#ident ──────────────────────
        System.out.println(AnsiColor.bold(title) + " " + AnsiColor.dim(projectFullName + "#" + ident));

        // ── 第 2 行: state • author opened X ago • N comments ─
        String stateDisplay = mapState(state);
        String formattedTime = formatDateTime(createdAt);
        String commentText = noteCount == 1 ? "1 comment" : noteCount + " comments";
        System.out.println(AnsiColor.green(stateDisplay) + " • "
                + authorName + " opened " + formattedTime + " • " + commentText);

        // ── Labels ────────────────────────────────────────────
        String labels = formatLabels(labelsNode);
        if (!labels.isEmpty()) {
            System.out.println("Labels: " + labels);
        }

        // ── Priority & State ──────────────────────────────────
        System.out.println();
        if (!priority.isEmpty()) {
            System.out.println("Priority: " + priority);
        }
        System.out.println("State:    " + state);

        // ── Description ───────────────────────────────────────
        if (!description.isBlank()) {
            System.out.println();
            System.out.println("  " + description.replace("\n", "\n  "));
        }

        // ── Comments ──────────────────────────────────────────
        handleCommentsOutput(notesNode);
    }

    private void handleCommentsOutput(JsonNode notesNode) {
        if (notesNode == null || !notesNode.isArray() || notesNode.isEmpty()) {
            return;
        }

        System.out.println();

        if (showComments) {
            // 显示所有评论
            for (var note : notesNode) {
                printComment(note, false);
            }
        } else {
            // 只显示最新评论
            int total = notesNode.size();
            int hidden = total - 1;

            if (hidden > 0) {
                System.out.println();
                System.out.println(AnsiColor.dim("———————— Not showing " + hidden + " comment"
                        + (hidden != 1 ? "s" : "") + " ————————"));
                System.out.println();
            }

            // 最后一条评论
            var lastNote = notesNode.get(total - 1);
            printComment(lastNote, true);

            if (hidden > 0) {
                System.out.println();
                System.out.println(AnsiColor.dim("Use --comments to view the full conversation"));
            }
        }
    }

    /**
     * 打印单条评论。
     */
    private void printComment(JsonNode note, boolean isNewest) {
        String commentAuthor = note.path("author").path("name").asText("Unknown");
        String noteText = stripHtml(note.has("body") ? note.path("body").asText("") : note.path("note").asText(""));
        String noteTime = formatDateTime(note.path("created_at").asText(""));

        String header = AnsiColor.bold(commentAuthor) + " • " + noteTime;
        if (isNewest) {
            header += " • Newest comment";
        }

        System.out.println(header);
        System.out.println();
        System.out.println("  " + noteText.replace("\n", "\n  "));
    }

    /**
     * 将 state 映射为可读名称。
     */
    private String mapState(String state) {
        return switch (state) {
            case "created" -> "Created";
            case "processing" -> "Processing";
            case "finished" -> "Finished";
            case "rejected" -> "Rejected";
            default -> state;
        };
    }

    /**
     * 将 labels 数组格式化为逗号分隔的字符串。
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
     * 简单去除 HTML 标签。
     */
    private String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * 将 ISO 8601 日期时间字符串格式化为 yyyy-MM-dd HH:mm:ss。
     */
    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "";
        }
        try {
            var dt = OffsetDateTime.parse(isoDateTime, ISO_FORMAT);
            return dt.format(OUTPUT_FORMAT);
        } catch (Exception e) {
            return isoDateTime;
        }
    }
}
