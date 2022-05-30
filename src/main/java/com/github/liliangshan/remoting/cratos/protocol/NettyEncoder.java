package com.github.liliangshan.remoting.cratos.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * NettyEncoder .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class NettyEncoder extends MessageToByteEncoder<Command> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Command command, ByteBuf out) throws Exception {
        if (command == null) {
            throw new Exception("encode msg is null");
        }
        out.writeShort(command.getMagic());
        out.writeInt(command.getHeader().getCommandType());
        out.writeLong(command.getHeader().getRequestId());
        out.writeInt(command.getHeader().getBodyLength());
        out.writeBytes(command.getBody());
    }


}
