/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.commons.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.*;

/**
 * This queue implements the BlockingQueue interface. The element should implement the
 * Importance interface. </p>
 *
 * <p> Internally Queue is implemented as a set of multiple queues corresponding to some
 * fixed priorities. When inserting an element, it will be put in to one of these queues
 * depending on its importance.</p>
 *
 * @param <E> E should implement the Importance interface.
 */
public class MultiPriorityBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E> {
    
    /** List of queues corresponding to different priorities */
    private List<InternalQueue<E>> queues;

    /** Number of items in the queue */
    private int count = 0;

    /** Lock held by take, poll, etc */
    private final ReentrantLock lock = new ReentrantLock();

    /** Waiting queue for takes */
    private final Condition notEmpty = lock.newCondition();

    private int capacity = Integer.MAX_VALUE;

    /** Algorithm for determining next queue */
    private NextQueueAlgorithm<E> nextQueueAlgorithm;
    
    /** whether fixed size queues are used */
    private boolean isFixedSizeQueues;

    /**
     * Create a queue with the given queues. </p>
     *
     * <p> This method will create a Queue that accepts objects with only the priorities specified.
     * If a object is submitted with a different priority it will result in an
     * IllegalArgumentException. If the algorithm is null, this queue will use the
     * PRRNextQueueAlgorithm.</p>
     * 
     * @param queues list of InternalQueue to be used
     * @param isFixedQueues weather fixed size queues are used
     * @param algorithm algorithm for calculating next queue
     */
    public MultiPriorityBlockingQueue(List<InternalQueue<E>> queues,
                         boolean isFixedQueues, NextQueueAlgorithm<E> algorithm) {

        this.queues = queues;
        this.isFixedSizeQueues = isFixedQueues;
        capacity = Integer.MAX_VALUE;

        if (isFixedQueues) {
            capacity = 0;
            for (InternalQueue<E> q : queues) {
                capacity += q.getCapacity();
            }
        }

        Collections.sort(this.queues, new Comparator<InternalQueue<E>>() {
            public int compare(InternalQueue<E> o1, InternalQueue<E> o2) {
                return o2.getPriority() - o1.getPriority();
            }
        });

        for (InternalQueue<E> queue : this.queues) {
            queue.setNotFullCond(lock.newCondition());
        }

        if (algorithm == null) {
            nextQueueAlgorithm = new PRRNextQueueAlgorithm<E>();
        } else {
            nextQueueAlgorithm = algorithm;
        }
        // initialize the algorithm
        nextQueueAlgorithm.init(queues);
    }

    /**
     * Put the specified value in to the queue. The put will block until space available
     * in the corresponding internal queue.
     *
     * @param e object that implements the Importance interface
     * @throws InterruptedException
     */
    public void put(E e) throws InterruptedException {
        Importance i = (Importance) e;
        InternalQueue<E> internalQueue = getQueueForPriority(i.getPriority());
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            try {
                while (internalQueue.remainingCapacity() == 0) {
                    internalQueue.getNotFullCond().await();
                }
            } catch (InterruptedException ie) {
                internalQueue.getNotFullCond().signal();
                throw ie;
            }

            internalQueue.offer(e);
            count++;

            notEmpty.signal();
        } finally {
            lock.unlock();
        }        
    }

    /**
     * Add the element if space available in the internal queue corresponding to the
     * priority of the object.
     *
     * @param e element to be added
     * @return true if element is added
     */
    public boolean offer(E e) {
        Importance i = (Importance) e;
        InternalQueue<E> internalQueue = getQueueForPriority(i.getPriority());
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (internalQueue.remainingCapacity() > 0) {
                internalQueue.offer(e);
                count++;
                notEmpty.signal();
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Try to add the element within the given time period. Wait the specified time for
     * space to be available. This method will put the object in to the internal queue with the
     * corresponding priority. This method blocks only if that internal queue is full.
     *
     * @param e element to be added
     * @param timeout time to wait if space not available
     * @param unit time unit
     * @return true if the element is added
     * @throws InterruptedException if the thread is interrupted
     */
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Importance i = (Importance) e;
        InternalQueue<E> internalQueue = getQueueForPriority(i.getPriority());

        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                if (internalQueue.remainingCapacity() > 0) {
                    internalQueue.offer(e);
                    count++;
                    notEmpty.signal();
                    return true;
                }
                if (nanos <= 0)
                    return false;
                try {
                    nanos = internalQueue.getNotFullCond().awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    internalQueue.getNotFullCond().signal();
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get an element. Block until an element is available
     * @return an element
     * @throws InterruptedException if the thread is interrupted
     */
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            InternalQueue<E> internalQueue = nextQueueAlgorithm.getNextQueue();
            try {
                while (internalQueue == null) {
                    notEmpty.await();
                    internalQueue = nextQueueAlgorithm.getNextQueue();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal();
                throw ie;
            }
            E e = internalQueue.poll();
            count--;
            internalQueue.getNotFullCond().signal();
            return e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the element from the top of the queue. If an element is not available wait
     * the specified timeout.
     *
     * @param timeout waiting time for element to be available
     * @param unit time unit
     * @return an object
     * @throws InterruptedException
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                InternalQueue<E> internalQueue = nextQueueAlgorithm.getNextQueue();
                if (internalQueue != null) {
                    E e = internalQueue.poll();
                    count--;
                    internalQueue.getNotFullCond().signal();
                    return e;
                }
                if (nanos <= 0)
                    return null;
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal();
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * We always give high priority to highest priority elements. We try to drain all the
     * high priority items first.
     *
     * @param c collection to drain the items     
     * @return number of elements copied
     */
    public int drainTo(Collection<? super E> c) {
        int count = 0;
        final ReentrantLock lock = this.lock;
        lock.lock();

        try {
            for (InternalQueue<E> internalQueue : queues) {
                count += internalQueue.drainTo(c);
            }
            this.count = this.count - count;
        } finally {
            lock.unlock();
        }
        return count;
    }

    /**
     * We always give high priority to highest priotiry elements. We try to drain all the
     * high priority items first.
     *
     * @param c collection to drain the itemd
     * @param maxElements maximum elements to copy
     * @return number of elements copied
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        int elementsCopied = 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (InternalQueue<E> internalQueue : queues) {
                elementsCopied += internalQueue.drainTo(c,
                        internalQueue.size() > (maxElements - elementsCopied) ?
                                (maxElements - elementsCopied) : internalQueue.size());
            }
            count = count - elementsCopied;
        } finally {
            lock.unlock();
        }
        return elementsCopied;
    }

    /**
     * Block indefinitely until a object is available for retrieval.
     *
     * @return an object
     */
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            InternalQueue<E> internalQueue = nextQueueAlgorithm.getNextQueue();
            if (internalQueue != null) {
                count--;
                E e = internalQueue.poll();
                internalQueue.getNotFullCond().signal();
                return e;
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return capacity - count;
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            InternalQueue<E> internalQueue = nextQueueAlgorithm.getNextQueue();
            if (internalQueue != null) {
                return internalQueue.peek();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new QueueIterator(toArray());
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (InternalQueue<E> internalQueue : queues) {
                if (internalQueue.remove(o)) {
                    count--;
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (InternalQueue<E> internalQueue : queues) {
                if (internalQueue.contains(o)) return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            StringBuffer s = new StringBuffer();
            for (InternalQueue<E> internalQueue : queues) {
                s.append(internalQueue.toString());
            }
            return s.toString();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (InternalQueue<E> intQueue : queues) {
                intQueue.clear();
            }
            count = 0;
        } finally {
            lock.unlock();
        }                
    }

    @SuppressWarnings({"SuspiciousToArrayCall"})
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            List<E> list = new ArrayList<E>();
            for (InternalQueue<E> internalQueue : queues) {
                list.addAll(internalQueue);
            }
            return list.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            List<E> list = new ArrayList<E>();
            for (InternalQueue<E> internalQueue : queues) {
                list.addAll(internalQueue);
            }
            return list.toArray();
        } finally {
            lock.unlock();
        }
    }

    private InternalQueue<E> getQueueForPriority(int priority) {
        for (InternalQueue<E> q : queues) {
            if (q.getPriority() == priority) {
                return q;
            }
        }
        throw new IllegalArgumentException();
    }

    private class QueueIterator implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet;

        QueueIterator(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E) array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            Object x = array[lastRet];
            lastRet = -1;
            lock.lock();
            try {
                for (InternalQueue<E> internalQueue : queues) {
                    for (Iterator<E> it = internalQueue.iterator(); it.hasNext();) {
                        if (it.next() == x) {
                            it.remove();
                            return;
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public List<InternalQueue<E>> getQueues() {
        return queues;
    }

    public NextQueueAlgorithm<E> getNextQueueAlgorithm() {
        return nextQueueAlgorithm;
    }

    public boolean isFixedSizeQueues() {
        return isFixedSizeQueues;
    }
}
