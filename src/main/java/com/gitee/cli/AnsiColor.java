package com.gitee.cli;

/**
 * ANSI 终端颜色工具 — 为 CLI 输出提供彩色文本支持。
 * <p>
 * 使用 JAnsi 在 Windows CMD 上启用 Virtual Terminal Processing，
 * 确保 ANSI 转义码在所有终端中正常显示。
 * <p>
 * 当检测到非终端环境（如管道、重定向）或设置了 NO_COLOR 时，自动降级为无色输出。
 */
public final class AnsiColor {

    // ── 颜色码 ──────────────────────────────────────────────
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String CYAN = "\033[36m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";

    /** 是否启用颜色 */
    private static final boolean ENABLED;

    static {
        ENABLED = detectColorSupport();
    }

    private AnsiColor() {
    }

    // ── 公共 API ─────────────────────────────────────────────

    /** 成功信息（绿色） */
    public static String green(String text) {
        return colorize(GREEN, text);
    }

    /** 错误信息（红色） */
    public static String red(String text) {
        return colorize(RED, text);
    }

    /** 警告信息（黄色） */
    public static String yellow(String text) {
        return colorize(YELLOW, text);
    }

    /** 提示信息（青色） */
    public static String cyan(String text) {
        return colorize(CYAN, text);
    }

    /** 加粗 */
    public static String bold(String text) {
        return colorize(BOLD, text);
    }

    /** 暗淡 */
    public static String dim(String text) {
        return colorize(DIM, text);
    }

    // ── 便捷方法 ─────────────────────────────────────────────

    /** 成功标记：绿色 ✓ */
    public static String success(String message) {
        return green("✓ " + message);
    }

    /** 失败标记：红色 ✗ */
    public static String error(String message) {
        return red("✗ " + message);
    }

    /** 警告标记：黄色 ⚠ */
    public static String warn(String message) {
        return yellow("⚠ " + message);
    }

    /** 信息标记：青色 ℹ */
    public static String info(String message) {
        return cyan("ℹ " + message);
    }

    // ── 内部方法 ──────────────────────────────────────────────

    private static String colorize(String color, String text) {
        if (!ENABLED) {
            return text;
        }
        return color + text + RESET;
    }

    private static boolean detectColorSupport() {
        // NO_COLOR 环境变量 (https://no-color.org/)
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        // 如果 console 为 null，通常表示输出被重定向到管道
        return System.console() != null;
    }
}
