package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionCounter {

    private static TransactionCounter instance = null;
    private static AtomicInteger transactionCount = new AtomicInteger(0);
    private static ReentrantLock lock = new ReentrantLock();
    private static int MAX_TRANSACTION_COUNT;
    private TransactionCounter(int maxTransactionCount) {
        this.MAX_TRANSACTION_COUNT = maxTransactionCount;
    }

    public static TransactionCounter getInstance(int maxTransactionCount) {
        if(instance == null) {
            instance = new TransactionCounter(maxTransactionCount);
        }
        return instance;
    }
    public static void increment() {
        lock.lock();
        try {
            transactionCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

}
