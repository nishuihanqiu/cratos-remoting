package com.github.liliangshan.remoting.cratos.protocol;

/**
 * Response .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class Response {

    private long requestId;
    private Object value;
    private Exception exception;

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public void onCall() {
        
    }

}
