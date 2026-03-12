package com.gitee.cli;

/**
 * Gitee API 调用异常，携带 HTTP 状态码和响应体。
 */
public class GiteeApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public GiteeApiException(int statusCode, String responseBody) {
        super("Gitee API error (HTTP " + statusCode + "): " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
