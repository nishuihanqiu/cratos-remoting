package com.github.liliangshan.remoting.cratos.processor;

import com.github.liliangshan.remoting.cratos.protocol.Request;

/**
 * RequestProcessor .
 *
 * @author liliangshan
 * @date 2021/1/17
 */
public interface RequestProcessor extends MessageProcessor<Request> {

    @Override
    Object process(Request request);

}
