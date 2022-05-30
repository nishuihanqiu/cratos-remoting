package com.github.liliangshan.remoting.cratos.common;

/**
 * ConnectionState .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public enum ConnectionState {

    /**
     * 未初始化完成
     **/
    UN_INIT(0),
    /**
     * 初始化完成
     **/
    INIT(1),
    /**
     * 存活可用状态
     **/
    ALIVE(2),
    /**
     * 不存活可用状态
     **/
    UN_ALIVE(3),
    /**
     * 关闭状态
     **/
    CLOSE(4);

    public final int value;

    ConnectionState(int value) {
        this.value = value;
    }

    public boolean isUnInited() {
        return this == UN_INIT;
    }

    public boolean isInited() {
        return this == INIT;
    }

    public boolean isAlive() {
        return this == ALIVE;
    }

    public boolean isUnAlive() {
        return this == UN_ALIVE;
    }

    public boolean isClosed() {
        return this == CLOSE;
    }

}
