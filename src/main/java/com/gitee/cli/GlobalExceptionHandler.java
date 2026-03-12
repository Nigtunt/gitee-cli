package com.gitee.cli;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * 全局异常处理器 — 将未捕获异常映射到统一退出码，错误信息输出到 stderr。
 */
public class GlobalExceptionHandler implements IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {
        // 错误信息输出到 stderr（红色）
        System.err.println(AnsiColor.red("Error: " + ex.getMessage()));

        // Java 21 pattern matching for instanceof
        if (ex instanceof GiteeApiException apiEx) {
            int status = apiEx.getStatusCode();
            return (status >= 400 && status < 500) ? ExitCode.CLIENT_ERROR : ExitCode.SERVER_ERROR;
        }

        if (ex instanceof IllegalArgumentException) {
            return ExitCode.CLIENT_ERROR;
        }

        // 其他未知异常视为服务端/网络错误
        return ExitCode.SERVER_ERROR;
    }
}
