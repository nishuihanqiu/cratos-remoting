package com.github.liliangshan.remoting.cratos.common;

/**
 * FutureState .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public enum FutureState {

    DOING(0),
    DONE(1),
    CANCELLED(2);

    public final int value;

    private FutureState(int value) {
        this.value = value;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isDone() {
        return this == DONE;
    }

    public boolean isDoing() {
        return this == DOING;
    }

}
