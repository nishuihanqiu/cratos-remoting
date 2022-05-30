package com.github.liliangshan.remoting.cratos.server;

import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.config.RemotingServerConfig;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractServer .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public abstract class AbstractServer implements Server {

    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    protected RemotingServerConfig serverConfig;
    protected Codec codec;
    protected AtomicBoolean opened = new AtomicBoolean(false);
    protected AtomicBoolean closed = new AtomicBoolean(false);
    protected ConnectionState state = ConnectionState.UN_INIT;

    public AbstractServer(RemotingServerConfig serverConfig, Codec codec) {
        this.serverConfig = serverConfig;
        this.codec = codec;
    }

    protected abstract void initialized();

    @Override
    final public void start() {
        if (opened.get()) {
            return;
        }
        if (opened.compareAndSet(false, true)) {
            try {
                this.startInternal();
                state = ConnectionState.ALIVE;
            } catch (Exception e) {
                opened.set(false);
                throw e;
            }
        }
    }

    protected abstract void startInternal();

    @Override
    public boolean available() {
        return state.isAlive() && opened.get();
    }

    @Override
    final public void close() {
        if (isClosed()) {
            return;
        }
        if (closed.compareAndSet(false, true)) {
            this.closeInternal();
        }
    }

    protected abstract void closeInternal();

    @Override
    public boolean isClosed() {
        return state.isClosed() && closed.get();
    }

    @Override
    public RemotingServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
