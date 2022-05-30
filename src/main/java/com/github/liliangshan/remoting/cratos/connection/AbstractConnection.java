package com.github.liliangshan.remoting.cratos.connection;

import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractConnection .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public abstract class AbstractConnection implements Connection {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected volatile ConnectionState state = ConnectionState.UN_INIT;
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    final public void open() {
        if (this.available()) {
            logger.warn("the channel already open, local: " + this.getLocalAddress()
                    + " remote: " + this.getRemoteAddress());
            return;
        }
        if (opened.compareAndSet(false, true)) {
            this.openInternal();
            closed.set(false);
        }
    }

    protected abstract void openInternal();

    public abstract InetSocketAddress getLocalAddress();

    public abstract InetSocketAddress getRemoteAddress();

    public abstract Response send(Request request);

    @Override
    public boolean available() {
        return state.isAlive();
    }

    public void reconnect() {
        state = ConnectionState.INIT;
    }

    public boolean isReconnected() {
        return state.isInited();
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed()) {
            return;
        }
        if (closed.get()) {
            return;
        }
        if (closed.compareAndSet(false, true)) {
            this.closeInternal();
            state = ConnectionState.CLOSE;
        }
    }

    protected abstract void closeInternal();

    @Override
    public boolean isClosed() {
        return state.isClosed();
    }
}

