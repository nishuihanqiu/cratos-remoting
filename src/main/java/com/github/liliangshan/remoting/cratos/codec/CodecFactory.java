package com.github.liliangshan.remoting.cratos.codec;

import com.github.liliangshan.remoting.cratos.common.ObjectFactory;

/**
 * CodecFactory .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class CodecFactory implements ObjectFactory<Codec> {

    @Override
    public Codec makeObject() {
        return new CratosCodec();
    }

}
