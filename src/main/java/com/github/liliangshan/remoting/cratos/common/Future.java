package com.github.liliangshan.remoting.cratos.common;

/**
 * Future .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public interface Future {

    void cancel();

    boolean isCancelled();

    boolean isDone();

    boolean isSuccess();

    Object getValue();

    Object getValue(int timeout);

    Exception getException();

}
