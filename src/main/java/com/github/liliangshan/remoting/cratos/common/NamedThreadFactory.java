package com.github.liliangshan.remoting.cratos.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NamedThreadFactory .
 *
 * @author liliangshan
 * @date 2021/1/15
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final String DEFAULT_NAME = "cratos-thread-";

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup threadGroup;
    private final AtomicInteger currentNumber = new AtomicInteger(1);
    private final String prefixName;
    private final int priority;
    private final boolean daemon;

    public NamedThreadFactory() {
        this(DEFAULT_NAME);
    }

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        this(prefix, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String prefix, boolean daemon, int priority) {
        SecurityManager s = System.getSecurityManager();
        this.threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.prefixName = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
        this.daemon = daemon;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(threadGroup, runnable,
                prefixName + currentNumber.getAndIncrement(), 0);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        return thread;
    }

}
