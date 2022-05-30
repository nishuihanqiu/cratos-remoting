package com.github.liliangshan.remoting.cratos.server;

import com.github.liliangshan.remoting.cratos.common.CratosThreadPoolExecutor;
import com.github.liliangshan.remoting.cratos.common.NamedThreadFactory;
import com.github.liliangshan.remoting.cratos.codec.CodecFactory;
import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.config.RemotingServerConfig;
import com.github.liliangshan.remoting.cratos.handler.ServerChannelHandler;
import com.github.liliangshan.remoting.cratos.handler.ShareableConnectionHandler;
import com.github.liliangshan.remoting.cratos.processor.RequestProcessor;
import com.github.liliangshan.remoting.cratos.protocol.NettyDecoder;
import com.github.liliangshan.remoting.cratos.protocol.NettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RemotingServer .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class RemotingServer extends AbstractServer {

    private static final Logger logger = LoggerFactory.getLogger(RemotingServer.class);

    private ShareableConnectionHandler connectionHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final RequestProcessor requestProcessor;
    private ThreadPoolExecutor executor = null;
    private ServerBootstrap serverBootstrap;
    private final AtomicInteger rejectCounter = new AtomicInteger(0);
    public static final int DEFAULT_MAX_IDLE_TIME = 60 * 1000; // 1 minutes

    public RemotingServer(RemotingServerConfig serverConfig, RequestProcessor requestProcessor) {
        super(serverConfig, new CodecFactory().makeObject());
        this.requestProcessor = requestProcessor;
        this.initialized();
    }

    @Override
    protected void initialized() {
        if (state == ConnectionState.INIT) {
            return;
        }
        serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        executor = new CratosThreadPoolExecutor(
                serverConfig.getMinWorkerThread(),
                serverConfig.getMaxWorkerThread(),
                DEFAULT_MAX_IDLE_TIME, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(serverConfig.getWorkerQueueSize()),
                new NamedThreadFactory("RemotingServer-" + serverConfig.getPort() + "-", true)
        );
        executor.prestartAllCoreThreads();
        connectionHandler = new ShareableConnectionHandler(serverConfig.getMaxServerConnection());
        state = ConnectionState.INIT;
        logger.info("RemotingServer ServerChannel init");
    }

    public AtomicInteger getRejectCounter() {
        return rejectCounter;
    }

    @Override
    protected void startInternal() {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("shareable_connection_handler", connectionHandler);
                        pipeline.addLast("decoder", new NettyDecoder());
                        pipeline.addLast("handler", new ServerChannelHandler(
                                RemotingServer.this, codec, requestProcessor, executor)
                        );
                        pipeline.addLast("encoder", new NettyEncoder());
                    }
                });
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(serverConfig.getPort()));
        channelFuture.syncUninterruptibly();
        serverChannel = channelFuture.channel();
        this.setLocalAddress((InetSocketAddress) serverChannel.localAddress());
        if (serverConfig.getPort() == 0) {
            serverConfig.setPort(getLocalAddress().getPort());
        }
        state = ConnectionState.ALIVE;
        logger.info("RemotingServer finish start: address=" + this.getLocalAddress());
    }

    @Override
    protected void closeInternal() {
        try {
            clean();
            if (state.isUnInited()) {
                logger.info("RemotingServer close fail: state={}, address={}", state.value, localAddress);
                return;
            }
            // 设置close状态
            state = ConnectionState.CLOSE;
            logger.info("RemotingServer close Success:  address={}", localAddress);
        } catch (Exception e) {
            logger.error("RemotingServer close Error:  address=" + localAddress, e);
        }
    }

    public void clean() {
        // close listen socket
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        // close all client's channel
        if (connectionHandler != null) {
            connectionHandler.close();
        }
        // shutdown the threadPool
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public ShareableConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }
}
