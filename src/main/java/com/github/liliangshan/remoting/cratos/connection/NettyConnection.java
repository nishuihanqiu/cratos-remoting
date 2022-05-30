package com.github.liliangshan.remoting.cratos.connection;

import com.github.liliangshan.remoting.cratos.client.RemotingClient;
import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.common.ConnectionState;
import com.github.liliangshan.remoting.cratos.common.Future;
import com.github.liliangshan.remoting.cratos.common.FutureListener;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NettyConnection .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class NettyConnection extends AbstractConnection {

    private final RemotingClient remotingClient;
    private Channel channel;
    private final Codec codec;
    private final InetSocketAddress remoteAddress;
    private InetSocketAddress localAddress = null;
    private final ReentrantLock lock = new ReentrantLock();

    public NettyConnection(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
        this.remoteAddress = remotingClient.getRemoteAddress();
        this.codec = remotingClient.getCodec();
    }

    @Override
    protected void openInternal() {
        ChannelFuture channelFuture;
        try {
            long start = System.currentTimeMillis();
            channelFuture = remotingClient.getBootstrap().connect(remoteAddress);
            int timeout = remotingClient.getClientConfig().getConnectTimeoutMillis();
            if (timeout <= 0) {
                throw new CratosRemotingException("RemotingClient init Error: timeout(" + timeout + ") <= 0 is forbid.");
            }

            boolean result = channelFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS);
            boolean success = channelFuture.isSuccess();
            if (result && success) {
                channel = channelFuture.channel();
                if (channel.localAddress() != null && channel.localAddress() instanceof InetSocketAddress) {
                    localAddress = (InetSocketAddress) channel.localAddress();
                }
                state = ConnectionState.ALIVE;
                return;
            }

            //
            boolean connected = false;
            channel = channelFuture.channel();
            if (channel != null) {
                connected = channel.isActive();
            }
            if (channelFuture.cause() != null) {
                channelFuture.cancel(true);
                throw new IOException("NettyConnection failed to connect to server, remote address: "
                        + remotingClient.getRemoteAddress() + ", result: " + result + ", success: "
                        + success + ", connected: " + connected, channelFuture.cause());
            } else {
                channelFuture.cancel(true);
                throw new IOException("NettyConnection connect to server timeout remote address: "
                        + remotingClient.getRemoteAddress() + ", cost: " + (System.currentTimeMillis() - start)
                        + ", result: " + result + ", success: " + success + ", connected: " + connected);
            }
        } catch (Exception e) {
            if (channel != null) {
                channel.close();
                channel = null;
            }
            throw new CratosRemotingException("NettyConnection failed to connect to server, remote address: "
                    + remotingClient.getRemoteAddress(), e);
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Response send(Request request) {
        int timeout = remotingClient.getClientConfig().getReadTimeoutMills();
        if (timeout <= 0) {
            throw new CratosRemotingException("NettyClient init Error: timeout(" + timeout + ") <= 0 is forbid.");
        }
        ResponseFuture response = new ResponseFuture();
        remotingClient.registerCallback(request.getRequestId(), response);
        Command command = this.encodeCommand(request);
        ChannelFuture writeFuture = channel.writeAndFlush(command);
        boolean result = writeFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS);

        if (result && writeFuture.isSuccess()) {
            response.addListener(new FutureListener() {
                @Override
                public void onCompleted(Future future) throws Exception {
                    if (future.isSuccess()) {
                        remotingClient.resetErrorCount();
                    } else {
                        remotingClient.incrErrorCount();
                    }
                }
            });
            return response;
        }

        writeFuture.cancel(true);
        response = remotingClient.removeCallback(request.getRequestId());
        if (response != null) {
            response.cancel();
        }
        // 失败的调用
        remotingClient.incrErrorCount();
        if (writeFuture.cause() != null) {
            throw new CratosRemotingException("NettyConnection send request to server Error: remote address="
                    + remotingClient.getRemoteAddress() + " local address=" + localAddress, writeFuture.cause());
        } else {
            throw new CratosRemotingException("NettyConnection send request to server Timeout: remote address="
                    + remotingClient.getRemoteAddress() + " local address=" + localAddress);
        }
    }

    private Command encodeCommand(Object msg) {
        if (!(msg instanceof Response)) {
            return codec.encode(remotingClient.getClientConfig().getSerialization(), msg);
        }
        try {
            return codec.encode(remotingClient.getClientConfig().getSerialization(), msg);
        } catch (Exception e) {
            logger.error("NettyEncoder encode error", e);
            Response oriResponse = (Response) msg;
            Response response = new Response();
            response.setException(e);
            response.setRequestId(oriResponse.getRequestId());
            return codec.encode(remotingClient.getClientConfig().getSerialization(), response);
        }
    }

    @Override
    protected void closeInternal() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception e) {
            logger.error("NettyConnection close error. remote address:" + remotingClient.getRemoteAddress() + " local=" + localAddress, e);
        }
    }

}
