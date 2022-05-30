package com.github.liliangshan.remoting.cratos;


import com.github.liliangshan.remoting.cratos.client.RemotingClient;
import com.github.liliangshan.remoting.cratos.config.RemotingClientConfig;
import com.github.liliangshan.remoting.cratos.config.RemotingServerConfig;
import com.github.liliangshan.remoting.cratos.processor.RequestProcessor;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import com.github.liliangshan.remoting.cratos.protocol.ResponseFuture;
import com.github.liliangshan.remoting.cratos.serialize.FastJsonSerialization;
import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import com.github.liliangshan.remoting.cratos.server.RemotingServer;
import com.github.liliangshan.remoting.cratos.util.RemotingUtils;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * RemotingClientTests .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class RemotingClientTests {

    private RemotingServer server;
    private RemotingClient client;
    private RemotingClientConfig clientConfig;
    private Request request;

    @Before
    public void setup() throws IOException {
        request = new Request();
        request.setRequestId(RemotingUtils.getRequestId());
        request.setValue("hello netty!");

        Serialization serialization = new FastJsonSerialization();

        clientConfig = new RemotingClientConfig();
        clientConfig.setSerialization(serialization);
        clientConfig.setHost("127.0.0.1");
        clientConfig.setPort(3005);
        clientConfig.setConnectionCount(10);

        RemotingServerConfig serverConfig = new RemotingServerConfig();
        serverConfig.setPort(3005);
        serverConfig.setSerialization(serialization);

        server = new RemotingServer(serverConfig, new RequestProcessor() {
            @Override
            public Object process(Request request) {
                Response response = new Response();
                response.setRequestId(request.getRequestId());
                if (request.getValue() instanceof String) {
                    response.setValue("request value: " + request.getValue() + " requestId: " + request.getRequestId());
                } else {
                    response.setValue(request.getValue());
                }
                return response;
            }
        });
        server.start();
    }

    @Test
    public void testNormal() {
        client = new RemotingClient(clientConfig);
        client.start();
        Response response;

        try {
            response = client.send(request);
            Object result = response.getValue();

            Assert.assertNotNull(result);
            Assert.assertEquals("request value: " + request.getValue() + " requestId: " + request.getRequestId(), result);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testAsync() {
        client = new RemotingClient(clientConfig);
        client.start();
        Response response;

        try {
            response = client.send(request);
            Assert.assertTrue(response instanceof ResponseFuture);
            Object result = response.getValue();
            Assert.assertNotNull(result);
            Assert.assertEquals("request value: " + request.getValue() + " requestId: " + request.getRequestId(), result);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testComplexObject() {
        client = new RemotingClient(clientConfig);
        client.start();
        Response response;
        Request request = new Request();
        request.setRequestId(RemotingUtils.getRequestId());
        request.setValue(Lists.newArrayList("hello", "world", Lists.newArrayList("what", "are")));
        try {
            response = client.send(request);
            Assert.assertTrue(response instanceof ResponseFuture);
            Object result = response.getValue();
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof List);
            System.out.println(result);
        } catch (Exception e) {
            fail();
        }
    }


    @After
    public void clean() throws IOException {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.close();
        }
    }

}
