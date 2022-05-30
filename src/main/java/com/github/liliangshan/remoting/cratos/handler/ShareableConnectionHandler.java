package com.github.liliangshan.remoting.cratos.handler;

import com.github.liliangshan.remoting.cratos.util.SystemUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ShareableConnectionHandler .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
@ChannelHandler.Sharable
public class ShareableConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ShareableConnectionHandler.class);
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();
    private final int maxChannel;

    public ShareableConnectionHandler(int maxChannel) {
        super();
        this.maxChannel = maxChannel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (channels.size() >= maxChannel) {
            // 超过最大连接数限制，直接close连接
            logger.warn("channel connected channel size out of limit: limit={} current={}", maxChannel, channels.size());
            channel.close();
        } else {
            String channelKey = SystemUtils.getChannelKey((InetSocketAddress) channel.localAddress(),
                    (InetSocketAddress) channel.remoteAddress());
            channels.put(channelKey, channel);
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = SystemUtils.getChannelKey((InetSocketAddress) channel.localAddress(),
                (InetSocketAddress) channel.remoteAddress());
        channels.remove(channelKey);
        ctx.fireChannelUnregistered();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    // close所有的连接
    public void close() {
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            try {
                Channel channel = entry.getValue();
                if (channel != null) {
                    channel.close();
                }
            } catch (Exception e) {
                logger.error("close channel error, key=" + entry.getKey() + " message:" + e.getMessage(), e);
            }
        }
    }

}
