package com.github.liliangshan.remoting.cratos.common;

import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FutureTask .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class FutureTask implements Future {

    private static final Logger logger = LoggerFactory.getLogger(FutureTask.class);

    private final Object lock = new Object();
    private volatile FutureState state = FutureState.DOING;
    protected long createTime = System.currentTimeMillis();
    private Exception exception;

    public FutureTask() {
    }

    @Override
    public void cancel() {
        this.cancel(new CratosRemotingException("response timeout..."));
    }

    @Override
    public boolean isCancelled() {
        return state.isCancelled();
    }

    @Override
    public boolean isDone() {
        return state.isDone();
    }

    @Override
    public boolean isSuccess() {
        return state.isDone() && exception == null;
    }

    @Override
    public Object getValue() {
        return this.getValue(-1);
    }

    @Override
    public Object getValue(int timeout) {
        synchronized (lock) {
            if (!state.isDoing()) {
                return this.getValueOrThrowable();
            }
            if (timeout <= 0) {
                return this.waitLock();
            }
            return this.waitLock(timeout);
        }
    }

    private Object waitLock() {
        try {
            lock.wait();
        } catch (Exception e) {
            this.cancel(new CratosRemotingException(e.getMessage(), e));
        }
        return this.getValueOrThrowable();
    }

    private Object waitLock(int timeout) {
        long waitTime = timeout - (System.currentTimeMillis() - createTime);
        while (waitTime > 0) {
            try {
                lock.wait(waitTime);
            } catch (InterruptedException ignored) {
            }
            if (!state.isDoing()) {
                break;
            }
            waitTime = timeout - (System.currentTimeMillis() - createTime);
        }
        if (state.isDoing()) {
            long processTime = System.currentTimeMillis() - createTime;
            this.cancel(new CratosRemotingException("request timeout: " + processTime));
        }
        return this.getValueOrThrowable();
    }

    private void cancel(Exception e) {
        synchronized (lock) {
            if (!state.isDoing()) {
                return;
            }
            state = FutureState.CANCELLED;
            exception = e;
            lock.notifyAll();
        }
    }

    private Object getValueOrThrowable() {
        if (exception != null) {
            logger.error(exception.getMessage(), exception);
            throw (exception instanceof RuntimeException) ? (RuntimeException) exception :
                    new CratosRemotingException(exception.getMessage(), exception);
        }
        return null;
    }

    public void done() {
        synchronized (lock) {
            if (!state.isDoing()) {
                return;
            }
            state = FutureState.DONE;
            lock.notifyAll();
        }
    }

    @Override
    public Exception getException() {
        return exception;
    }

}
