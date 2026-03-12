package com.gitee.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 输出辅助工具 — 根据 {@code --json} flag 决定输出格式。
 */
public final class OutputHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private OutputHelper() {
    }

    /**
     * 将对象输出到 stdout。当 {@code jsonOutput} 为 true 时输出 JSON，否则输出纯文本。
     *
     * @param data       要输出的对象
     * @param plainText  纯文本形式（当 jsonOutput=false 时使用）
     * @param jsonOutput 是否强制 JSON 输出
     */
    public static void print(Object data, String plainText, boolean jsonOutput) {
        if (jsonOutput) {
            try {
                System.out.println(MAPPER.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                System.err.println("Error: Failed to serialize output to JSON: " + e.getMessage());
            }
        } else {
            System.out.println(plainText);
        }
    }

    /**
     * 直接将对象序列化为 JSON 输出到 stdout。
     */
    public static void printJson(Object data) {
        print(data, "", true);
    }

    /**
     * 获取共享的 ObjectMapper 实例。
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
