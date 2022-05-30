package com.github.liliangshan.remoting.cratos.protocol;

/**
 * NettyDecoderState .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public enum NettyDecoderState {

    MAGIC,
    MESSAGE_TYPE,
    REQUEST_ID,
    BODY_LENGTH,
    MESSAGE_BODY;

}
