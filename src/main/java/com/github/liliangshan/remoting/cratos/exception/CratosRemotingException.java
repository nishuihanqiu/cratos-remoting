package com.github.liliangshan.remoting.cratos.exception;


/**
 * CratosRemotingException .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class CratosRemotingException extends CratosException {

    public CratosRemotingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CratosRemotingException(String message) {
        super(message);
    }

    public CratosRemotingException(Throwable cause) {
        super(cause);
    }

}
