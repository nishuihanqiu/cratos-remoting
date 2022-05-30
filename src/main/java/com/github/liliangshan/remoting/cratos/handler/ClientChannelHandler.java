package com.github.liliangshan.remoting.cratos.handler;

import com.github.liliangshan.remoting.cratos.client.RemotingClient;
import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.processor.ResponseProcessor;
import com.github.liliangshan.remoting.cratos.protocol.Command;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientChannelHandler .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);

    private final RemotingClient remotingClient;
    private final ResponseProcessor responseProcessor;
    private final Codec codec;

    public ClientChannelHandler(RemotingClient remotingClient, ResponseProcessor responseProcessor, Codec codec) {
        this.remotingClient = remotingClient;
        this.responseProcessor = responseProcessor;
        this.codec = codec;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Command)) {
            logger.error(" messageReceived type not support: class=" + msg.getClass());
            throw new CratosRemotingException(" messageReceived type not support: class=" + msg.getClass());
        }
        this.processMessage((Command) msg);
    }

    private void processMessage(Command msg) {
        Response response;
        try {
            response = (Response) codec.decode(remotingClient.getClientConfig().getSerialization(), msg);
        } catch (Exception e) {
            logger.error("NettyDecoder decode fail, requestId:" + msg.getHeader().getRequestId()
                    + ", size:" + msg.getBody().length + ", e:" + e.getMessage());
            response = new Response();
            response.setRequestId(msg.getHeader().getRequestId());
            response.setException(e);
        }
        responseProcessor.process(response);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(" channelActive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info(" channelInactive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(" exceptionCaught: remote={} local={} event={}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause.getMessage(), cause);
        ctx.channel().close();
    }

}
