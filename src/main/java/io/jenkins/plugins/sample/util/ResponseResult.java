package io.jenkins.plugins.sample.util;

import com.google.api.client.util.Key;

import java.io.Serializable;

public class ResponseResult<T> implements Serializable {

    @Key("body")
    T body;
    @Key("code")
    int code = 200;
    @Key("message")
    private String message = "success";

    /**
     * default response:
     * code=200
     * message=success
     */
    public ResponseResult() {
    }

    public ResponseResult(String message) {
        this.message = message;
    }

    public ResponseResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult(T body, int code, String message) {
        this.body = body;
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}

