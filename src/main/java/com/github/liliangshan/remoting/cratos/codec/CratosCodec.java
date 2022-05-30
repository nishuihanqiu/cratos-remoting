package com.github.liliangshan.remoting.cratos.codec;

import com.github.liliangshan.remoting.cratos.protocol.Command;
import com.github.liliangshan.remoting.cratos.protocol.CommandType;
import com.github.liliangshan.remoting.cratos.protocol.Request;
import com.github.liliangshan.remoting.cratos.protocol.Response;
import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import com.github.liliangshan.remoting.cratos.exception.CratosIOException;

import java.io.*;

/**
 * CratosCodec .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class CratosCodec extends AbstractCodec {

    private static final String NULL_CLASS_NAME = "null";

    public CratosCodec() {
    }

    @Override
    public Command encode(Serialization serialization, Object object) throws CratosIOException {
        try {
            if (object instanceof Request) {
                return encodeRequest(serialization, (Request) object);
            }
            if (object instanceof Response) {
                return encodeResponse(serialization, (Response) object);
            }
        } catch (Exception e) {
            throw new CratosIOException(e.getMessage(), e);
        }
        throw new CratosIOException("command must be instance of request or response");
    }

    private Command encodeRequest(Serialization serialization, Request request) throws IOException {
        byte[] body = this.createBody(serialization, request.getRequestId(), request.getValue());
        return Command.of(body, request.getRequestId(), CommandType.REQUEST);
    }

    private Command encodeResponse(Serialization serialization, Response response) throws IOException {
        CommandType type = response.getException() != null ? CommandType.RESPONSE_ERROR : CommandType.RESPONSE;
        Object value = response.getException() != null ? response.getException()
                : response.getValue();
        byte[] body = this.createBody(serialization, response.getRequestId(), value);
        return Command.of(body, response.getRequestId(), type);
    }

    private byte[] createBody(Serialization serialization, long requestId, Object value) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput output = this.createOutput(outputStream);
        String className = value == null ? NULL_CLASS_NAME : value.getClass().getName();
        output.writeUTF(className);
        this.serialize(output, value, serialization);
        output.flush();
        byte[] body = outputStream.toByteArray();
        output.close();
        return body;
    }

    @Override
    public Object decode(Serialization serialization, Command command) throws CratosIOException {
        try {
            Object value = this.parseBody(serialization, command.getBody());
            if (command.getHeader().getCommandType() == CommandType.REQUEST.getCode()) {
                Request request = new Request();
                request.setRequestId(command.getHeader().getRequestId());
                request.setValue(value);
                return request;
            }
            Response response = new Response();
            response.setRequestId(command.getHeader().getRequestId());
            if (command.getHeader().getCommandType() == CommandType.RESPONSE.getCode()) {
                response.setValue(value);
                return response;
            }
            if (command.getHeader().getCommandType() == CommandType.RESPONSE_ERROR.getCode()) {
                response.setException((Exception) value);
                response.setValue(null);
                return response;
            }
            throw new CratosIOException("command type must be request or response");
        } catch (Exception e) {
            throw new CratosIOException(e.getMessage(), e);
        }
    }

    private Object parseBody(Serialization serialization, byte[] body) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        ObjectInput input = this.createInput(inputStream);
        try {
            String className = input.readUTF();
            if (NULL_CLASS_NAME.equals(className)) {
                return null;
            }
            return this.deserialize(input, Class.forName(className), serialization);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            input.close();
        }
    }

}
