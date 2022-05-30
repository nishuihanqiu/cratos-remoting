package com.github.liliangshan.remoting.cratos;

import com.github.liliangshan.remoting.cratos.codec.Codec;
import com.github.liliangshan.remoting.cratos.codec.CratosCodec;
import com.github.liliangshan.remoting.cratos.exception.CratosRemotingException;
import com.github.liliangshan.remoting.cratos.protocol.Command;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import com.github.liliangshan.remoting.cratos.serialize.FastJsonSerialization;
import com.github.liliangshan.remoting.cratos.serialize.GsonSerialization;
import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * CratosCodecTests .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class CratosCodecTests {

    private Request request;
    private Response response;
    private Response responseException;
    private Codec codec;
    private Serialization serialization;
    private final List<Request> objects = new ArrayList<>();

    @Before
    public void setup() {
        request = new Request();
        request.setRequestId(1);
        request.setValue("hello");

        response = new Response();
        response.setRequestId(request.getRequestId());
        response.setValue(request.getValue() + "world");

        responseException = new Response();
        responseException.setRequestId(request.getRequestId());
        responseException.setException(new CratosRemotingException("this is exception!"));

        objects.add(0, request);

        codec = new CratosCodec();
        serialization = new GsonSerialization();
    }

    @Test
    public void testCodecRequest() {
        try {
            Command command = codec.encode(serialization, request);
            Object result = codec.decode(serialization, command);
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof Request);
            Assert.assertEquals(request.getRequestId(), ((Request) result).getRequestId());
            Assert.assertEquals(request.getValue(), ((Request) result).getValue());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testCodecResponse() {
        try {
            Command command = codec.encode(serialization, response);
            Object result = codec.decode(serialization, command);
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof Response);
            Assert.assertEquals(response.getRequestId(), ((Response) result).getRequestId());
            Assert.assertEquals(response.getValue(), ((Response) result).getValue());
            Assert.assertNull(((Response) result).getException());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testCodecException() {
        try {
            Command command = codec.encode(serialization, responseException);
            Object result = codec.decode(serialization, command);
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof Response);
            Assert.assertEquals(responseException.getRequestId(), ((Response) result).getRequestId());
            Assert.assertNotNull(((Response) result).getException());
            Assert.assertEquals(responseException.getException().getMessage(),
                    ((Response) result).getException().getMessage());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRequestResponse() {
        try {
            Command command = codec.encode(serialization, response);
            Object result = codec.decode(serialization, command);
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof Response);
            Assert.assertEquals(request.getRequestId(), ((Response) result).getRequestId());
            Assert.assertEquals(request.getValue() + "world", ((Response) result).getValue());
            Assert.assertNull(((Response) result).getException());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testCodecList() {
        try {
            Serialization serialization = new FastJsonSerialization();
            Request request = new Request();
            request.setValue(objects);
            Command command = codec.encode(serialization, request);
            Object res = codec.decode(serialization, command);
            Assert.assertNotNull(res);
            Assert.assertTrue(res instanceof Request);
            Object result = ((Request) res).getValue();
            Assert.assertNotNull(result);
            Assert.assertTrue(result instanceof List);
            Assert.assertEquals(((List<?>) result).size(), 1);

            Object re = ((List<?>) result).get(0);
            String name = re.getClass().getName();
            System.out.println(name);
//            Request request1 = (Request) (re);
//            Assert.assertEquals(request1.getValue(), request.getValue());

//            Response response1 = (Response) (((List<?>) result).get(1));
//            Assert.assertEquals(response1.getValue(), response.getValue());
//
//            Response response2 = (Response) (((List<?>) result).get(2));
//            Assert.assertEquals(response2.getException().getMessage(),
//                    responseException.getException().getMessage());

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
