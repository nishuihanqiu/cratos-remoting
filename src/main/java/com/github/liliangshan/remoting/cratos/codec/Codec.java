package com.github.liliangshan.remoting.cratos.codec;

import com.github.liliangshan.remoting.cratos.protocol.Command;
import com.github.liliangshan.remoting.cratos.serialize.Serialization;
import com.github.liliangshan.remoting.cratos.exception.CratosIOException;


/**
 * Codec .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface Codec {

    Command encode(Serialization serialization, Object object) throws CratosIOException;

    Object decode(Serialization serialization, Command command) throws CratosIOException;

}
