package com.github.liliangshan.remoting.cratos.serialize;

/**
 * Serialization .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface Serialization {

    <T> byte[] serialize(T object);

    <T> T deserialize(byte[] bytes, Class<T> tClass);

}
