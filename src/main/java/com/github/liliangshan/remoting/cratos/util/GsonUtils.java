package com.github.liliangshan.remoting.cratos.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * GsonUtils .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public final class GsonUtils {

    private static final Gson GSON;
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
        builder.setDateFormat(YYYY_MM_DD_HH_MM_SS);
        GSON = builder.create();
    }

    private GsonUtils() {

    }

    public static <T> String toJson(T obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

    public static <T> byte[] toJsonBytes(T obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T fromJsonBytes(byte[] bytes, Class<T> tClass) {
        if (bytes == null) {
            return null;
        }
        return fromJson(new String(bytes, StandardCharsets.UTF_8), tClass);
    }

    public static <T> T fromParameterizedJsonBytes(byte[] bytes, Class<T> tClass, Class<?>... argumentClass) {
        if (bytes == null) {
            return null;
        }
        return fromParameterizedJson(new String(bytes, StandardCharsets.UTF_8), tClass, argumentClass);
    }

    public static <T> T fromJson(String json, Class<T> tClass) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return GSON.fromJson(json, tClass);
    }

    public static <T> T fromParameterizedJson(String json, Class<T> tClass, Class<?>... argumentClass) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        if (argumentClass == null || argumentClass.length == 0) {
            return fromJson(json, tClass);
        }
        Type type = new GsonParameterizedType(tClass, argumentClass);
        return GSON.fromJson(json, type);
    }

    private static class GsonParameterizedType implements ParameterizedType {

        private final Class<?> tClass;
        private final Type[] argumentTypes;

        public GsonParameterizedType(Class<?> tClass, Type[] argumentTypes) {
            this.tClass = tClass;
            this.argumentTypes = argumentTypes;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return argumentTypes;
        }

        @Override
        public Type getRawType() {
            return tClass;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

}
