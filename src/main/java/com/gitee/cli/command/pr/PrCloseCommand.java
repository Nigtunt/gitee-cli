package com.gitee.cli.command.pr;

import com.gitee.cli.GiteeApiClient;
import com.gitee.cli.OutputHelper;
import com.gitee.cli.command.BaseCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * {@code gitee pr close <number>} — 关闭合并请求。
 * <p>
 * Gitee API: POST /projects/:id/merge_requests/:iid
 */
@Command(name = "close", description = "Close a pull request.")
public class PrCloseCommand extends BaseCommand {

    @Parameters(index = "0", description = "Pull request number.")
    private int number;

    @Override
    public void run() {
        var repo = requireRepo();

        var result = GiteeApiClient.getInstance().post(
                "/projects/" + URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/merge_requests/" + number,
                new HashMap<>()); // POST without specific body parameters

        if (isJsonOutput()) {
            OutputHelper.printJson(result);
        } else {
            System.out.println("Pull request closed.");
        }
    }
}
