package com.github.liliangshan.remoting.cratos.protocol;

/**
 * Request .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class Request {

    private long requestId;
    private Object value;

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

}
