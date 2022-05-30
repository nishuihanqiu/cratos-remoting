package com.github.liliangshan.remoting.cratos.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RemotingUtils .
 *
 * @author liliangshan
 * @date 2021/1/16
 */
public final class RemotingUtils {

    protected static final AtomicLong offset = new AtomicLong(0);
    protected static final int BITS = 20;
    protected static final long MAX_COUNT_PER_MILLIS = 1 << BITS;

    private RemotingUtils() {

    }

    public static long getRequestId() {
        long currentTime = System.currentTimeMillis();
        long count = offset.incrementAndGet();
        while (count >= MAX_COUNT_PER_MILLIS) {
            synchronized (RemotingUtils.class) {
                if (offset.get() >= MAX_COUNT_PER_MILLIS) {
                    offset.set(0);
                }
            }
            count = offset.incrementAndGet();
        }
        return (currentTime << BITS) + count;
    }
}
