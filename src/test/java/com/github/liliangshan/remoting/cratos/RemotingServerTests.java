package com.github.liliangshan.remoting.cratos;

import com.github.liliangshan.remoting.cratos.client.RemotingClient;
import com.github.liliangshan.remoting.cratos.config.RemotingClientConfig;
import com.github.liliangshan.remoting.cratos.config.RemotingServerConfig;
import com.github.liliangshan.remoting.cratos.processor.RequestProcessor;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import com.github.liliangshan.remoting.cratos.serialize.FastJsonSerialization;
import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import com.github.liliangshan.remoting.cratos.server.RemotingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * RemotingServerTests .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class RemotingServerTests {

    private RemotingClientConfig clientConfig;
    private RemotingClient client;
    private RemotingServer server;
    private RemotingServerConfig serverConfig;
    private static final int minClientConnection = 5;
    private static final int maxServerConnection = 7;

    @Before
    public void setup() {
        serverConfig = new RemotingServerConfig();
        serverConfig.setPort(3000);
        serverConfig.setMaxServerConnection(maxServerConnection);
        Serialization serialization = new FastJsonSerialization();
        serverConfig.setSerialization(serialization);

        clientConfig = new RemotingClientConfig();
        clientConfig.setConnectionCount(minClientConnection);
        clientConfig.setSerialization(serialization);
        clientConfig.setHost("127.0.0.1");
        clientConfig.setPort(3000);
        client = new RemotingClient(clientConfig);
    }

    @Test
    public void testMaxServerConnection() throws InterruptedException {
        server = new RemotingServer(serverConfig, new RequestProcessor() {
            @Override
            public Object process(Request request) {
                Response response = new Response();
                response.setRequestId(request.getRequestId());
                response.setValue("request value: " + request.getValue() + " requestId: " + request.getRequestId());
                return response;
            }
        });
        server.start();

        Assert.assertEquals(0, server.getConnectionHandler().getChannels().size());

        client.start();
        Thread.sleep(100);
        Assert.assertEquals(minClientConnection, server.getConnectionHandler().getChannels().size());

        RemotingClient remotingClient = new RemotingClient(clientConfig);
        remotingClient.start();
        Thread.sleep(100);
        Assert.assertTrue(server.getConnectionHandler().getChannels().size() < minClientConnection * 2);

        client.close();
        remotingClient.close();
        Thread.sleep(100);
        Assert.assertEquals(0, server.getConnectionHandler().getChannels().size());
    }

    @After
    public void clean() {
        if (server != null) {
            server.close();
        }
    }

}
