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

package org.apache.synapse.commons.executors.queues;

import org.apache.synapse.commons.executors.InternalQueue;

import java.util.concurrent.locks.Condition;
import java.util.*;

/**
 * A bounded queue implementation for internal queues. This queue is backed by an
 * fixed size array.
 *
 * @param <E> Should implement the Importance interface
 */
public class FixedSizeQueue<E> extends AbstractQueue<E> implements InternalQueue<E> {

    /**
     * Priority of this queue
     */
    private int priority;    

    /**
     * A waiting queue when this queue is full
     */
    private Condition notFullCond;

    /**
     * Array holding the queues
     */
    private E[] array;

    /**
     * Capacity of the queue
     */
    private int capacity;

    /**
     * Number of elements in the queue
     */
    private int count = 0;

    /**
     * Head of the queue
     */
    private int head = 0;

    /**
     * Tail of the queue
     */
    private int tail = 0;

    /**
     * Create a queue with the given priority and capacity.
     * @param priority priority of the elements in the queue
     * @param capacity capacity of the queue
     */
    public FixedSizeQueue(int priority, int capacity) {
        this.priority = priority;        
        this.capacity = capacity;

        array = (E[]) new Object[capacity];
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int p) {
        this.priority = p;
    }    

    public Condition getNotFullCond() {
        return notFullCond;
    }

    public void setNotFullCond(Condition notFullCond) {
        this.notFullCond = notFullCond;
    }

    public Iterator<E> iterator() {
        return new Itr<E>();
    }

    public int size() {
        return count;
    }

    public String toString() {
        return super.toString() + this.priority;
    }

    public boolean offer(E e) {
        if (count == array.length) {
            return false;
        } else {
            insert(e);
            return true;
        }
    }

    public E poll() {
        if (count == 0)
            return null;
        return get();
    }

    public E peek() {
        return (count == 0) ? null : array[head];
    }

    public int remainingCapacity() {
        return capacity - count;        
    }

    public int drainTo(Collection<? super E> c) {
        final E[] items = this.array;
        int i = head;
        int n = 0;
        int max = count;
        while (n < max) {
            c.add(items[i]);
            items[i] = null;
            i = increment(i);
            n++;
        }
        if (n > 0) {
            count = 0;
            tail = 0;
            head = 0;

        }
        return n;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        final E[] items = this.array;
        int i = head;
        int n = 0;
        int max = (maxElements < count) ? maxElements : count;
        while (n < max) {
            c.add(items[i]);
            items[i] = null;
            i = increment(i);
            n++;
        }
        if (n > 0) {
            count -= n;
            head = i;
        }
        return n;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean contains(Object o) {
        for (E e : array) {
            if (e.equals(o)) return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        boolean found = false;
        int i = head;
        int iterations = 0;
        while (iterations++ < count) {
            if (!found && array[i].equals(o)) {
                found = true;
            }

            if (found) {
                int j = increment(i);
                array[i] = array[j];
            }

            i = increment(i);
        }

        if (found) {
            count--;
            tail = decrement(tail);
        }

        return found;
    }

    private class Itr<E> implements Iterator<E> {
	    int index = head;

        public boolean hasNext() {
            return index != tail;
        }

        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return (E) array[index++];

        }

        public void remove() {            
            while (index != tail) {
                int j = increment(index);

                array[index] = array[j];
                index = j;
            }
        }
    }

    private int decrement(int n) {
        return (n == 0) ? array.length - 1 : n;
    }

    private int increment(int n) {
        return (++n == array.length) ? 0 : n;
    }

    private void insert(E e) {
        array[tail] = e;
        tail = increment(tail);
        count++;
    }

    private E get() {
        E e = array[head];
        array[head] = null;
        head = increment(head);
        count--;
        return e;
    }
}
