package com.github.liliangshan.remoting.cratos.protocol;

/**
 * CommandType .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public enum CommandType {

    REQUEST(0, "请求"),
    RESPONSE(1, "响应"),
    RESPONSE_ERROR(2, "响应错误");

    private final int code;
    private final String description;

    CommandType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
