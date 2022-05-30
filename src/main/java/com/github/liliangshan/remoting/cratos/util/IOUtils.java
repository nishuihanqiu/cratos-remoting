package com.github.liliangshan.remoting.cratos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * IOUtils .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public final class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {

    }

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
