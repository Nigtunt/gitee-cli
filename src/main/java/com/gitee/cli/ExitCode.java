package com.gitee.cli;

/**
 * 统一退出码定义。
 * <ul>
 * <li>{@code 0} — 正常结束</li>
 * <li>{@code 1} — 客户端错误 / 参数错误</li>
 * <li>{@code 2} — 服务端 / 网络异常</li>
 * </ul>
 */
public final class ExitCode {

    public static final int SUCCESS = 0;

    /** 客户端错误：参数缺失、格式错误等 */
    public static final int CLIENT_ERROR = 1;

    /** 服务端 / 网络错误 */
    public static final int SERVER_ERROR = 2;

    private ExitCode() {
        // 不允许实例化
    }
}
