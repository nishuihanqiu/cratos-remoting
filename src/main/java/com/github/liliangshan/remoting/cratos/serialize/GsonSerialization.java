package com.github.liliangshan.remoting.cratos.serialize;

import com.github.liliangshan.remoting.cratos.util.GsonUtils;

/**
 * GsonSerialization .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class GsonSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        return GsonUtils.toJsonBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) {
        return GsonUtils.fromJsonBytes(bytes, tClass);
    }

}
