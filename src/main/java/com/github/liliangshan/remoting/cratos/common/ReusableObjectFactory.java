package com.github.liliangshan.remoting.cratos.common;

import com.github.liliangshan.remoting.cratos.common.ObjectFactory;

/**
 * ReusableObjectFactory .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public interface ReusableObjectFactory<T> extends ObjectFactory<T> {

    void rebuild(T object, boolean async);

}
