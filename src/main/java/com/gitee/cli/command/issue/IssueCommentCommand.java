package com.gitee.cli.command.issue;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

/**
 * {@code gitee issue comment <number> [flags]} — 在 Issue 下发表、修改或删除评论。
 */
@Command(name = "comment", description = "Add, edit, or delete a comment on an issue.")
public class IssueCommentCommand extends BaseCommand {

    @Parameters(index = "0", description = "Issue number (e.g. I1234A).")
    private String number;

    @Option(names = { "--body", "-b" }, description = "The comment body text")
    private String body;

    /* 暂时不使用这些选项
    @Option(names = { "--body-file", "-F" }, description = "Read body text from file (use \"-\" to read from standard input)")
    private String bodyFile;

    @Option(names = "--edit-last", description = "Edit the last comment of the current user")
    private boolean editLast;

    @Option(names = "--create-if-none", description = "Create a new comment if no comments are found. Can be used only with --edit-last")
    private boolean createIfNone;

    @Option(names = "--delete-last", description = "Delete the last comment of the current user")
    private boolean deleteLast;

    @Option(names = "--yes", description = "Skip the delete confirmation prompt when --delete-last is provided")
    private boolean skipConfirmation;
    */

    @Option(names = { "--editor", "-e" }, description = "Skip prompts and open the text editor to write the body in (Not fully supported)")
    private boolean editor;

    @Option(names = { "--web", "-w" }, description = "Open the web browser to write the comment (Not fully supported)")
    private boolean web;

    @Override
    public void run() {
        if (editor || web) {
            System.err.println("Warning: --editor and --web flags are not fully supported yet. Will proceed with basic input.");
        }

        var repo = requireRepo();
        var encodedRepo = URLEncoder.encode(repo, StandardCharsets.UTF_8);

        // API base for notes on an issue
        String notesApiPath = "/projects/" + encodedRepo + "/issues/" + number + "/notes";

        // 暂时不使用这些实现
        /*
        if (deleteLast) {
            handleDeleteLast(notesApiPath);
            return;
        }

        if (editLast) {
            handleEditLast(notesApiPath);
            return;
        }
        */

        // Default to create
        handleCreate(notesApiPath);
    }

    private void handleDeleteLast(String notesApiPath) {
        JsonNode lastNote = findLastCommentOfCurrentUser(notesApiPath);
        if (lastNote == null) {
            System.err.println("No comments found for the current user.");
            return;
        }

        /* 暂时取消交互输入
        if (!skipConfirmation) {
            System.err.print("Are you sure you want to delete the last comment? [y/N] ");
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("yes")) {
                System.out.println("Aborted.");
                return;
            }
        }
        */

        String noteId = lastNote.path("id").asText();
        GiteeApiClient.getInstance().delete(notesApiPath + "/" + noteId);

        if (isJsonOutput()) {
            OutputHelper.printJson(lastNote);
        } else {
            System.out.println("Deleted comment " + noteId + " on Issue #" + number);
        }
    }

    private void handleEditLast(String notesApiPath) {
        JsonNode lastNote = findLastCommentOfCurrentUser(notesApiPath);
        if (lastNote == null) {
            /* 暂时不使用 createIfNone
            if (createIfNone) {
                handleCreate(notesApiPath);
            } else {
                System.err.println("No comments found for the current user. Use --create-if-none to create one.");
            }
            */
            System.err.println("No comments found for the current user.");
            return;
        }

        String content = resolveBodyContent();
        if (content == null || content.isBlank()) {
            System.err.println("Error: Comment body cannot be empty");
            return;
        }

        String noteId = lastNote.path("id").asText();
        var payload = new HashMap<String, Object>();
        payload.put("body", content);

        var result = GiteeApiClient.getInstance().put(notesApiPath + "/" + noteId, payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Edited comment " + noteId + " on Issue #" + number);
        }
    }

    private void handleCreate(String notesApiPath) {
        String content = resolveBodyContent();
        if (content == null || content.isBlank()) {
            System.err.println("Error: Comment body cannot be empty");
            return;
        }

        var payload = new HashMap<String, Object>();
        payload.put("body", content);

        var result = GiteeApiClient.getInstance().post(notesApiPath, payload);

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Comment posted on Issue #" + number);
            if (result.has("html_url")) {
                System.out.println(result.path("html_url").asText());
            }
        }
    }

    private JsonNode findLastCommentOfCurrentUser(String notesApiPath) {
        // Get current user details
        JsonNode user = GiteeApiClient.getInstance().get("/user", null);
        String currentUserLogin = user.path("login").asText();

        // Optional logic: we'd need to paginate if there are many comments,
        // but for simplicity we fetch the default page (which is usually the latest items on Gitee).
        // Actually Gitee returns ascending or descending. Let's fetch recent by direction=desc if possible to get last comment faster.
        var queryParams = new HashMap<String, String>();
        queryParams.put("sort", "created_at");
        queryParams.put("direction", "desc");
        queryParams.put("per_page", "100");

        JsonNode notes = GiteeApiClient.getInstance().get(notesApiPath, queryParams);

        if (notes.isArray()) {
            // Find the most recent note from current user
            // Since direction=desc, the first one encountered is the latest.
            for (JsonNode note : notes) {
                if (note.has("user") && currentUserLogin.equals(note.path("user").path("login").asText())) {
                    return note;
                }
            }
        }
        return null;
    }

    private String resolveBodyContent() {
        if (body != null && !body.isBlank()) {
            return body;
        }
        /* 暂时不使用标准输入和文件输入
        if (bodyFile != null) {
            if ("-".equals(bodyFile)) {
                return readFromStdin();
            } else {
                try {
                    return Files.readString(Path.of(bodyFile), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                    return null;
                }
            }
        }
        */
        return null; // Empty body, or no provided param
    }

    private String readFromStdin() {
        StringBuilder sb = new StringBuilder();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
