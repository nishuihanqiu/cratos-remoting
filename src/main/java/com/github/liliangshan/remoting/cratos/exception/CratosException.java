package com.github.liliangshan.remoting.cratos.exception;

/**
 * CratosException .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class CratosException extends RuntimeException {

    private final String message;
    protected final String code = this.getClass().getSimpleName();

    public CratosException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public CratosException(String message) {
        this(message, null);
    }

    public CratosException(Throwable cause) {
        this("", cause);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

}
