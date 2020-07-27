package org.wso2.carbon.apimgt.gateway.listeners;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the Thread Factory used to create the threads which retrieve the artifacts from the extension.
 */
public class ArtifactsRetrieverThreadFactory implements ThreadFactory {
    static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    public ArtifactsRetrieverThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "ArtifactRetriever-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    /**
     *
     * @param r
     * @return
     */
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}
