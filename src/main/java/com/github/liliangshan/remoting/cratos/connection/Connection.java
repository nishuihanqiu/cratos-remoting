package com.github.liliangshan.remoting.cratos.connection;

import java.io.Closeable;
import java.io.IOException;

/**
 * Connection .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface Connection extends Closeable {

    void open();

    boolean available();

    void close() throws IOException;

    boolean isClosed();

}
