package com.github.liliangshan.remoting.cratos.client;

import com.github.liliangshan.remoting.cratos.common.CratosThreadPoolExecutor;
import com.github.liliangshan.remoting.cratos.common.NamedThreadFactory;
import com.github.liliangshan.remoting.cratos.util.CollectionUtils;
import com.github.liliangshan.remoting.cratos.util.IOUtils;
import com.github.liliangshan.remoting.cratos.codec.CodecFactory;
import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.common.ReusableObjectFactory;
import com.github.liliangshan.remoting.cratos.config.RemotingClientConfig;
import com.github.liliangshan.remoting.cratos.connection.NettyConnection;
import com.github.liliangshan.remoting.cratos.connection.NettyConnectionFactory;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.handler.ClientChannelHandler;
import com.github.liliangshan.remoting.cratos.processor.ResponseProcessor;
import com.github.liliangshan.remoting.cratos.protocol.*;
import com.github.liliangshan.remoting.cratos.util.MathUtils;
import com.github.liliangshan.remoting.cratos.util.SystemUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RemotingClient .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class RemotingClient extends AbstractClient {

    private static final ThreadPoolExecutor EXECUTOR = new CratosThreadPoolExecutor(
            1, 300, "RemotingClient-InitPool-");
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup workerGroup;
    private final AtomicInteger idx = new AtomicInteger();
    protected ReusableObjectFactory<NettyConnection> factory;
    protected ArrayList<NettyConnection> connections;
    protected int connectionCount;
    protected ConcurrentMap<Long, ResponseFuture> callbackMap = new ConcurrentHashMap<>();
    private final int fusingThreshold;
    private final AtomicLong errorNumber = new AtomicLong(0);


    public RemotingClient(RemotingClientConfig clientConfig) {
        super(clientConfig, new CodecFactory().makeObject());
        ThreadFactory threadFactory = new NamedThreadFactory("RemotingClient-");
        workerGroup = SystemUtils.useEpoll() ? new EpollEventLoopGroup(clientConfig.getWorkerCount(), threadFactory)
                : new NioEventLoopGroup(clientConfig.getWorkerCount(), threadFactory);
        this.connectionCount = clientConfig.getConnectionCount();
        this.fusingThreshold = clientConfig.getFusingThreshold();
    }

    @Override
    protected void startInternal() throws IOException {
        this.bootstrap.group(this.workerGroup)
                .channel(SystemUtils.getSocketChannelClass())
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpDelayed())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceivedBufferSize())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new NettyDecoder());
                        ch.pipeline().addLast(new ClientChannelHandler(RemotingClient.this, new ResponseProcessor() {
                            @Override
                            public Object process(Response response) {
                                ResponseFuture responseFuture = RemotingClient.this.removeCallback(response.getRequestId());
                                if (responseFuture == null) {
                                    logger.warn("RemotingClient has response from server, but responseFuture not exist, requestId={}", response.getRequestId());
                                    return null;
                                }
                                if (response.getException() != null) {
                                    responseFuture.onFailure(response);
                                } else {
                                    responseFuture.onSuccess(response);
                                }
                                return null;
                            }
                        }, codec));
                        ch.pipeline().addLast(new NettyEncoder());
                    }
                });

        // 初始化连接池
        this.initConnectionPool();
    }

    protected void initConnectionPool() {
        factory = createChannelFactory();
        connections = new ArrayList<>(connectionCount);
        for (int i = 0; i < connectionCount; i++) {
            connections.add(factory.makeObject());
        }
        this.initConnections(clientConfig.isInitAsync());
        state = ConnectionState.INIT;
    }

    protected ReusableObjectFactory<NettyConnection> createChannelFactory() {
        return new NettyConnectionFactory(this);
    }

    protected void initConnections(boolean async) {
        if (async) {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    createConnections();
                }
            });
        } else {
            createConnections();
        }
    }

    private void createConnections() {
        for (NettyConnection connection : connections) {
            try {
                connection.open();
            } catch (Exception e) {
                logger.error("init pool create connect Error: " + e.getMessage(), e);
            }
        }
    }

    protected NettyConnection getConnection() {
        int index = MathUtils.getNonNegativeRange24bit(idx.getAndIncrement());
        NettyConnection connection;

        for (int i = index; i < connectionCount + 1 + index; i++) {
            connection = connections.get(i % connectionCount);
            if (!connection.available() && !connection.isClosed()) {
                factory.rebuild(connection, i != connectionCount + 1);
            }
            if (connection.available()) {
                return connection;
            }
        }
        throw new CratosRemotingException("RemotingClient getChannel Error");
    }

    protected void closeAllChannels() {
        if (!CollectionUtils.isEmpty(connections)) {
            for (NettyConnection connection : connections) {
                IOUtils.close(connection);
            }
        }
    }


    @Override
    protected void closeInternal() {
        this.closeAllChannels();
    }

    @Override
    public Response send(Request request) {
        try {
            NettyConnection connection = this.getConnection();
            if (connection == null) {
                logger.error("RemotingClient connection is null");
                return null;
            }
            return connection.send(request);
        } catch (Exception e) {
            logger.error("RemotingClient request Error" + e.getMessage(), e);
            if (e instanceof CratosRemotingException) {
                throw (CratosRemotingException) e;
            } else {
                throw new CratosRemotingException("RemotingClient request Error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Response ping(Request request) {
        return this.send(request);
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void registerCallback(long requestId, ResponseFuture responseFuture) {
        if (this.callbackMap.size() >= clientConfig.getMaxRequest()) {
            // reject request, prevent from OutOfMemoryError
            throw new CratosRemotingException("RemotingClient over of max concurrent request, drop request, address: "
                    + getLocalAddress() + " requestId=" + requestId);
        }

        this.callbackMap.put(requestId, responseFuture);
    }

    public ResponseFuture removeCallback(long requestId) {
        return callbackMap.remove(requestId);
    }

    /**
     * 如果连续失败的次数 >= maxClientConnection, 那么把client设置成不可用状态
     */
    public void incrErrorCount() {
        long count = errorNumber.incrementAndGet();
        if (count >= fusingThreshold && this.available()) {
            synchronized (this) {
                count = errorNumber.longValue();
                if (count >= fusingThreshold && this.available()) {
                    logger.error("RemotingClient unavailable Error");
                    state = ConnectionState.UN_ALIVE;
                }
            }
        }
    }

    /**
     * 重置调用失败的计数, 把节点设置成可用
     */
    public void resetErrorCount() {
        errorNumber.set(0);
        if (state.isAlive()) {
            return;
        }
        synchronized (this) {
            if (state.isAlive()) {
                return;
            }
            if (state.isUnAlive()) {
                long count = errorNumber.longValue();
                if (count < fusingThreshold) {
                    state = ConnectionState.ALIVE;
                    logger.info("RemotingClient recover available");
                }
            }
        }
    }

}
