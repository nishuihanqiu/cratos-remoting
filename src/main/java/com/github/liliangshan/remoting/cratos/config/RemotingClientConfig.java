package com.github.liliangshan.remoting.cratos.config;

import com.github.liliangshan.remoting.cratos.serialize.Serialization;

/**
 * RemotingClientConfig .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class RemotingClientConfig {

    private String host;
    private int port;
    private boolean initAsync = false;
    private int connectionCount = 2;
    private int workerCount = Runtime.getRuntime().availableProcessors();
    private boolean tcpDelayed = false;
    private boolean keepAlive = true;
    private int sendBufferSize = 65535;
    private int receivedBufferSize = 65535;
    private int connectTimeoutMillis = 10000;
    private int readTimeoutMills = 10000;
    private int writeTimeoutMills = 10000;
    private int maxRequest = 100000;
    private Serialization serialization;
    private int fusingThreshold = 10;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isInitAsync() {
        return initAsync;
    }

    public void setInitAsync(boolean initAsync) {
        this.initAsync = initAsync;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public boolean isTcpDelayed() {
        return tcpDelayed;
    }

    public void setTcpDelayed(boolean tcpDelayed) {
        this.tcpDelayed = tcpDelayed;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceivedBufferSize() {
        return receivedBufferSize;
    }

    public void setReceivedBufferSize(int receivedBufferSize) {
        this.receivedBufferSize = receivedBufferSize;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMills() {
        return readTimeoutMills;
    }

    public void setReadTimeoutMills(int readTimeoutMills) {
        this.readTimeoutMills = readTimeoutMills;
    }

    public int getWriteTimeoutMills() {
        return writeTimeoutMills;
    }

    public void setWriteTimeoutMills(int writeTimeoutMills) {
        this.writeTimeoutMills = writeTimeoutMills;
    }

    public int getMaxRequest() {
        return maxRequest;
    }

    public void setMaxRequest(int maxRequest) {
        this.maxRequest = maxRequest;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public int getFusingThreshold() {
        return fusingThreshold;
    }

    public void setFusingThreshold(int fusingThreshold) {
        this.fusingThreshold = fusingThreshold;
    }
}
