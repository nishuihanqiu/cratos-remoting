package com.github.liliangshan.remoting.cratos.codec;

import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import com.github.liliangshan.remoting.cratos.exception.CratosIOException;

import java.io.*;

/**
 * AbstractCodec .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public abstract class AbstractCodec implements Codec {

    protected void serialize(ObjectOutput output, Object message, Serialization serialize) throws IOException {
        if (message == null) {
            output.writeObject(null);
            return;
        }

        output.writeObject(serialize.serialize(message));
    }

    protected Object deserialize(ObjectInput input, Class<?> type, Serialization serialize)
            throws IOException, ClassNotFoundException {
        byte[] bytes = (byte[]) input.readObject();
        if (bytes == null) {
            return null;
        }

        return serialize.deserialize(bytes, type);
    }

    public ObjectOutput createOutput(OutputStream outputStream) {
        try {
            return new ObjectOutputStream(outputStream);
        } catch (Exception e) {
            throw new CratosIOException(this.getClass().getSimpleName() + " createOutput error", e);
        }
    }

    public ObjectInput createInput(InputStream in) {
        try {
            return new ObjectInputStream(in);
        } catch (Exception e) {
            throw new CratosIOException(this.getClass().getSimpleName() + " createInput error", e);
        }
    }

}
