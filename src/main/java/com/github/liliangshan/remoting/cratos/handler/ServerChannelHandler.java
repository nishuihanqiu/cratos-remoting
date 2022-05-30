package com.github.liliangshan.remoting.cratos.handler;

import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.processor.RequestProcessor;
import com.github.liliangshan.remoting.cratos.protocol.Command;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import com.github.liliangshan.remoting.cratos.server.RemotingServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ServerChannelHandler .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final RemotingServer remotingServer;
    private final Codec codec;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final RequestProcessor requestProcessor;

    public ServerChannelHandler(RemotingServer remotingServer, Codec codec, RequestProcessor requestProcessor,
                                ThreadPoolExecutor threadPoolExecutor) {
        this.remotingServer = remotingServer;
        this.codec = codec;
        this.threadPoolExecutor = threadPoolExecutor;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof Command) {
            if (threadPoolExecutor != null) {
                try {
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            processMessage(ctx, ((Command) msg));
                        }
                    });
                } catch (RejectedExecutionException rejectException) {
                    rejectMessage(ctx, (Command) msg);
                }
            } else {
                processMessage(ctx, (Command) msg);
            }
        } else {
            logger.error(" messageReceived type not support: class=" + msg.getClass());
            throw new CratosRemotingException("RemotingServerChannelHandler messageReceived type not support: class=" + msg.getClass());
        }
    }

    private void rejectMessage(ChannelHandlerContext ctx, Command msg) {
        Response response = new Response();
        response.setRequestId(msg.getHeader().getRequestId());
        response.setException(new CratosRemotingException("process thread pool is full, reject by server: " + ctx.channel().localAddress()));
        this.sendResponse(ctx, response);
        logger.error("process thread pool is full, reject, active={} poolSize={} corePoolSize={} maxPoolSize={} taskCount={} requestId={}",
                threadPoolExecutor.getActiveCount(), threadPoolExecutor.getPoolSize(), threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getTaskCount(), msg.getHeader().getRequestId());
        remotingServer.getRejectCounter().incrementAndGet();
    }

    private void processMessage(ChannelHandlerContext ctx, Command msg) {
        try {
            Object result = codec.decode(remotingServer.getServerConfig().getSerialization(), msg);
            processRequest(ctx, (Request) result);
        } catch (Exception e) {
            logger.error("codec decode fail! requestId" + msg.getHeader().getRequestId()
                    + ", size:" + msg.getBody().length + ", e:" + e.getMessage());
            Response response = new Response();
            response.setException(e);
            response.setRequestId(msg.getHeader().getRequestId());
            this.sendResponse(ctx, response);
        }
    }

    private void processRequest(final ChannelHandlerContext ctx, final Request request) {
        Response response;
        try {
            response = (Response) requestProcessor.process(request);
        } catch (Exception e) {
            logger.error("NettyChannelHandler processRequest fail! requestId:" + request.getRequestId(), e);
            response = new Response();
            response.setRequestId(request.getRequestId());
            response.setException(new CratosRemotingException("process request fail. errmsg:" + e.getMessage()));
        }
        ChannelFuture channelFuture = this.sendResponse(ctx, response);
        if (channelFuture != null) {
            final Response resp = response;
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (resp != null) {
                        resp.onCall();
                    }
                }
            });
        }
    }

    private ChannelFuture sendResponse(ChannelHandlerContext ctx, Response response) {
        Command msg = null;
        try {
            msg = codec.encode(remotingServer.getServerConfig().getSerialization(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (ctx.channel().isActive()) {
            return ctx.channel().writeAndFlush(msg);
        }
        return null;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught: remote={} local={} event={}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause.getMessage(), cause);
        ctx.channel().close();
    }

}
