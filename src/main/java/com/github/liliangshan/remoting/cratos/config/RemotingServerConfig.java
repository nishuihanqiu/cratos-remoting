package com.github.liliangshan.remoting.cratos.config;

import com.github.liliangshan.remoting.cratos.serialize.Serialization;

/**
 * RemotingServerConfig .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class RemotingServerConfig {

    private int port = 3000;
    private boolean shareChannel = false;
    private int maxServerConnection = 10000;
    private int workerQueueSize = 1000;
    private int minWorkerThread = 20;
    private int maxWorkerThread = 200;
    private Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isShareChannel() {
        return shareChannel;
    }

    public void setShareChannel(boolean shareChannel) {
        this.shareChannel = shareChannel;
    }

    public int getMaxServerConnection() {
        return maxServerConnection;
    }

    public void setMaxServerConnection(int maxServerConnection) {
        this.maxServerConnection = maxServerConnection;
    }

    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    public void setWorkerQueueSize(int workerQueueSize) {
        this.workerQueueSize = workerQueueSize;
    }

    public int getMinWorkerThread() {
        return minWorkerThread;
    }

    public void setMinWorkerThread(int minWorkerThread) {
        this.minWorkerThread = minWorkerThread;
    }

    public int getMaxWorkerThread() {
        return maxWorkerThread;
    }

    public void setMaxWorkerThread(int maxWorkerThread) {
        this.maxWorkerThread = maxWorkerThread;
    }

}
