package com.github.liliangshan.remoting.cratos.processor;

/**
 * MessageProcessor .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
interface MessageProcessor<T> {

    Object process(T object);

}
