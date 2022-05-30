package com.github.liliangshan.remoting.cratos.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * NettyDecoder .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class NettyDecoder extends ReplayingDecoder<NettyDecoderState> {

    private final CommandHeader commandHeader = new CommandHeader();

    public NettyDecoder() {
        super(NettyDecoderState.MAGIC);
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MAGIC:
                checkMagic(in.readShort());
                checkpoint(NettyDecoderState.MESSAGE_TYPE);
            case MESSAGE_TYPE:
                commandHeader.setCommandType(in.readInt());
                checkpoint(NettyDecoderState.REQUEST_ID);
            case REQUEST_ID:
                commandHeader.setRequestId(in.readLong());
                checkpoint(NettyDecoderState.BODY_LENGTH);
            case BODY_LENGTH:
                commandHeader.setBodyLength(in.readInt());
                checkpoint(NettyDecoderState.MESSAGE_BODY);
            case MESSAGE_BODY:
                byte[] body = new byte[commandHeader.getBodyLength()];
                in.readBytes(body);
                Command command = new Command();
                command.setHeader(commandHeader);
                command.setBody(body);
                out.add(command);
                checkpoint(NettyDecoderState.MAGIC);
        }
    }

    private void checkMagic(short magic) {
        if (magic != Command.MAGIC) {
            throw new IllegalArgumentException("illegal command [magic]" + magic);
        }
    }

}
