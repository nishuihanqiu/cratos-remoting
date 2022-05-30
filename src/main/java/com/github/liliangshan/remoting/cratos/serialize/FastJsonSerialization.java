package com.github.liliangshan.remoting.cratos.serialize;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;

/**
 * FastJsonSerialization .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class FastJsonSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) {
        if (bytes == null) {
            return null;
        }
        return JSON.parseObject(bytes, tClass);
    }

}
