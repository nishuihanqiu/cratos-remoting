package com.github.liliangshan.remoting.cratos.server;

import com.github.liliangshan.remoting.cratos.config.RemotingServerConfig;

import java.net.InetSocketAddress;

/**
 * Server .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface Server {

    void start();

    boolean available();

    void close();

    boolean isClosed();

    RemotingServerConfig getServerConfig();

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

}
