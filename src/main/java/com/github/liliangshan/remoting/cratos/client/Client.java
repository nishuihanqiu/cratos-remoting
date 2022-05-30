package com.github.liliangshan.remoting.cratos.client;

import com.github.liliangshan.remoting.cratos.config.RemotingClientConfig;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;

import java.net.InetSocketAddress;

/**
 * Client .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface Client {

    void start();

    boolean available();

    void close();

    boolean isClosed();

    Response send(Request request);

    Response ping(Request request);

    RemotingClientConfig getClientConfig();

    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

}
