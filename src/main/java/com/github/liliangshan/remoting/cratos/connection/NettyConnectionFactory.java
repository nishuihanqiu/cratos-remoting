package com.github.liliangshan.remoting.cratos.connection;

import com.github.liliangshan.remoting.cratos.common.CratosThreadPoolExecutor;
import com.github.liliangshan.remoting.cratos.common.NamedThreadFactory;
import com.github.liliangshan.remoting.cratos.client.RemotingClient;
import com.github.liliangshan.remoting.cratos.common.ReusableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NettyConnectionFactory .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public class NettyConnectionFactory implements ReusableObjectFactory<NettyConnection> {

    private static final Logger logger = LoggerFactory.getLogger(NettyConnectionFactory.class);

    private static final ExecutorService rebuildExecutorService = new CratosThreadPoolExecutor(
            5, 30, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new NamedThreadFactory("NettyConnectionFactory-", true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private final RemotingClient remotingClient;

    public NettyConnectionFactory(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }
    @Override
    public NettyConnection makeObject() {
        return new NettyConnection(remotingClient);
    }

    @Override
    public void rebuild(NettyConnection nettyConnection, boolean async) {
        ReentrantLock lock = nettyConnection.getLock();
        if (!lock.tryLock()) {
            return;
        }
        try {
            if (!async) {
                this.rebuildObject(nettyConnection);
            } else {
                rebuildExecutorService.submit(() -> this.rebuildObject(nettyConnection));
            }
        } catch (Exception e) {
            logger.error("rebuild error: " + this.toString(), e);
        } finally {
            lock.unlock();
        }
    }

    private void rebuildObject(NettyConnection connection) {
        if (connection == null) {
            return;
        }
        if (connection.available() || connection.isReconnected()) {
            return;
        }
        try {
            connection.getLock().lock();
            connection.reconnect();
            connection.close();
            connection.open();
            logger.info("rebuild channel success.");
        } catch (Exception e) {
            logger.error("rebuild error: " + this, e);
        } finally {
            connection.getLock().unlock();
        }
    }

}
