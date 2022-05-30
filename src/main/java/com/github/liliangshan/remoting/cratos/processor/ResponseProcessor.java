package com.github.liliangshan.remoting.cratos.processor;

import com.github.liliangshan.remoting.cratos.protocol.Response;

/**
 * ResponseProcessor .
 *
 * @author liliangshan
 * @date 2021/1/17
 */
public interface ResponseProcessor extends MessageProcessor<Response> {

    @Override
    Object process(Response response);

}
