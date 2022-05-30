package com.github.liliangshan.remoting.cratos.client;

import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.config.RemotingClientConfig;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractClient .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public abstract class AbstractClient implements Client {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected InetSocketAddress localAddress;
    protected InetSocketAddress remoteAddress;

    protected RemotingClientConfig clientConfig;
    protected Codec codec;
    protected AtomicBoolean opened = new AtomicBoolean(false);
    protected AtomicBoolean closed = new AtomicBoolean(false);
    protected ConnectionState state = ConnectionState.UN_INIT;

    public AbstractClient(RemotingClientConfig clientConfig, Codec codec) {
        this.clientConfig = clientConfig;
        this.codec = codec;
        this.remoteAddress = new InetSocketAddress(clientConfig.getHost(), clientConfig.getPort());
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public Response ping(Request request) {
        throw new CratosRemotingException("ping not supported.");
    }

    @Override
    public RemotingClientConfig getClientConfig() {
        return clientConfig;
    }

    public Codec getCodec() {
        return codec;
    }

    @Override
    final public void start() {
        if (opened.compareAndSet(false, true)) {
            try {
                this.startInternal();
                state = ConnectionState.ALIVE;
            } catch (IOException e) {
                opened.set(false);
                logger.error(e.getMessage(), e);
                throw new CratosRemotingException(e.getMessage(), e);
            }
        }
    }

    protected abstract void startInternal() throws IOException;

    @Override
    public boolean available() {
        return state.isAlive();
    }

    @Override
    final public void close() {
        if (this.isClosed()) {
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
