package com.github.liliangshan.remoting.cratos.protocol;

import com.github.liliangshan.remoting.cratos.common.Future;
import com.github.liliangshan.remoting.cratos.common.FutureListener;
import com.github.liliangshan.remoting.cratos.common.FutureTask;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ResponseFuture .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class ResponseFuture extends Response implements Future {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFuture.class);

    private final List<FutureListener> listeners = new ArrayList<>();
    private final FutureTask task = new FutureTask();
    protected Exception exception = null;
    private Object result = null;

    public ResponseFuture() {
    }

    @Override
    public void cancel() {
        this.task.cancel();
        this.notifyListeners();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public boolean isDone() {
        return task.isDone();
    }

    @Override
    public boolean isSuccess() {
        return task.isDone() && exception == null;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public Object getValue() {
        task.getValue();
        this.throwException();
        return result;
    }

    @Override
    public Object getValue(int timeout) {
        task.getValue(timeout);
        this.throwException();
        return result;
    }

    private void throwException() {
        if (exception != null) {
            throw (exception instanceof RuntimeException) ? (RuntimeException) exception :
                    new CratosRemotingException(exception.getMessage(), exception);
        }
    }

    public void onSuccess(Response response) {
        this.result = response.getValue();
        this.setRequestId(response.getRequestId());
        task.done();
        this.notifyListeners();
    }

    public void onFailure(Response response) {
        this.exception = response.getException();
        this.setRequestId(response.getRequestId());
        task.done();
        this.notifyListeners();
    }

    public void addListener(FutureListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (FutureListener listener : listeners) {
            this.notifyListener(listener);
        }
    }

    private void notifyListener(FutureListener listener) {
        try {
            listener.onCompleted(this);
        } catch (Exception e) {
            logger.error(" notifyListener Error: " + listener.getClass().getSimpleName(), e);
        }
    }

}
