package com.github.liliangshan.remoting.cratos.util;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * SystemUtils .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class SystemUtils {

    public static final String NETTY_EPOLL_ENABLE = System.getProperty("netty.epoll.enable", "true");
    public static final String OS_NAME = System.getProperty("os.name");

    public static boolean useEpoll() {
        if (!OS_NAME.toLowerCase().contains("linux")) {
            return false;
        }
        if (!Epoll.isAvailable()) {
            return false;
        }
        return Boolean.parseBoolean(NETTY_EPOLL_ENABLE);
    }

    public static Class<? extends SocketChannel> getSocketChannelClass() {
        if (useEpoll()) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }

    // remote address + local address 作为连接的唯一标示
    public static String getChannelKey(InetSocketAddress local, InetSocketAddress remote) {
        String key = "";
        if (local == null || local.getAddress() == null) {
            key += "null-";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort() + "-";
        }

        if (remote == null || remote.getAddress() == null) {
            key += "null";
        } else {
            key += remote.getAddress().getHostAddress() + ":" + remote.getPort();
        }

        return key;
    }

}
