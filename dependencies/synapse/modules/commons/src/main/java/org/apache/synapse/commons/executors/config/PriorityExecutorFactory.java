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

package org.apache.synapse.commons.executors.config;

import org.apache.synapse.commons.executors.*;
import org.apache.synapse.commons.executors.queues.FixedSizeQueue;
import org.apache.synapse.commons.executors.queues.UnboundedQueue;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;


public class PriorityExecutorFactory {
    private static Log log = LogFactory.getLog(PriorityExecutorFactory.class);

    public static final QName NAME_ATT = new QName(ExecutorConstants.NAME);
    public static final QName SIZE_ATT   = new QName(ExecutorConstants.SIZE);
    public static final QName PRIORITY_ATT   = new QName(ExecutorConstants.PRIORITY);

    public static final QName IS_FIXED_ATT = new QName(ExecutorConstants.IS_FIXED_SIZE);

    public static final QName BEFORE_EXECUTE_HANDLER =
            new QName(ExecutorConstants.BEFORE_EXECUTE_HANDLER);

    public static final QName NEXT_QUEUE_ATT = new QName(ExecutorConstants.NEXT_QUEUE);

    public static final QName MAX_ATT = new QName(ExecutorConstants.MAX);
    public static final QName CORE_ATT = new QName(ExecutorConstants.CORE);
    public static final QName KEEP_ALIVE_ATT = new QName(ExecutorConstants.KEEP_ALIVE);

    @SuppressWarnings({"UnusedDeclaration"})
    public static PriorityExecutor createExecutor(String namespace, OMElement e,
                                                  boolean requireName,
                                                  Properties properties) throws AxisFault {
        
        QName queuesQName = createQname(namespace, ExecutorConstants.QUEUES);
        QName queueQName = createQname(namespace, ExecutorConstants.QUEUE);
        QName threadsQName = createQname(namespace, ExecutorConstants.THREADS);

        PriorityExecutor executor = new PriorityExecutor();

        OMAttribute nameAtt = e.getAttribute(NAME_ATT);
        if (nameAtt != null && !"".equals(nameAtt.getAttributeValue())) {
            executor.setName(nameAtt.getAttributeValue());
        } else if (requireName){
            handlerException(ExecutorConstants.NAME +
                    " is required for a " + ExecutorConstants.PRIORITY_EXECUTOR);
        }

        // set the handler for calling before the message is put in to the queue
        OMAttribute handlerAtt = e.getAttribute(BEFORE_EXECUTE_HANDLER);
        if (handlerAtt != null) {
            BeforeExecuteHandler beh =
                    createExecuteBeforeHandler(handlerAtt.getAttributeValue());
            executor.setBeforeExecuteHandler(beh);
        }

        // create the queue configuration
        OMElement queuesEle = e.getFirstChildWithName(queuesQName);
        if (queuesEle != null) {
            OMAttribute nextQueueAtt = queuesEle.getAttribute(NEXT_QUEUE_ATT);

            NextQueueAlgorithm<Runnable> nqa = null;
            if (nextQueueAtt != null) {
                 nqa = createNextQueueAlgo(nextQueueAtt.getAttributeValue());
            }

            boolean isFixedSize = true;

            OMAttribute fixedSizeAtt = queuesEle.getAttribute(IS_FIXED_ATT);
            if (fixedSizeAtt != null) {
                isFixedSize = Boolean.parseBoolean(fixedSizeAtt.getAttributeValue());
            }

            // create the queue configuration
            List<InternalQueue<Runnable>> intQueues
                    = createQueues(queueQName, queuesEle, isFixedSize);

            MultiPriorityBlockingQueue<Runnable> queue =
                    new MultiPriorityBlockingQueue<Runnable>(intQueues, isFixedSize, nqa);

            executor.setQueue(queue);
        } else {
            handlerException("Queues configuration is mandatory");
        }

        OMElement threadsEle = e.getFirstChildWithName(threadsQName);
        if (threadsEle != null) {
            OMAttribute maxAttr = threadsEle.getAttribute(MAX_ATT);
            if (maxAttr != null) {
                executor.setMax(Integer.parseInt(maxAttr.getAttributeValue()));
            }
            OMAttribute coreAttr = threadsEle.getAttribute(CORE_ATT);
            if (coreAttr != null) {
                executor.setCore(Integer.parseInt(coreAttr.getAttributeValue()));
            }
            OMAttribute keepAliveAttr = threadsEle.getAttribute(KEEP_ALIVE_ATT);
            if (keepAliveAttr != null) {
                executor.setKeepAlive(Integer.parseInt(keepAliveAttr.getAttributeValue()));
            }
        }
                
        return executor;
    }

    private static List<InternalQueue<Runnable>> createQueues(
            QName qQName, OMElement queuesEle, boolean isFixedSize) throws AxisFault {

        List<InternalQueue<Runnable>> internalQueues =
                new ArrayList<InternalQueue<Runnable>>();

        Iterator it = queuesEle.getChildrenWithName(qQName);
        while (it.hasNext()) {
            OMElement qElement = (OMElement) it.next();
            String size = qElement.getAttributeValue(SIZE_ATT);
            String priority = qElement.getAttributeValue(PRIORITY_ATT);
            int s = 0;
            int p = 0;
            if (priority != null) {
                p = Integer.parseInt(priority);
            } else {
                handlerException("Priority must be specified");
            }

            if (size != null) {
                s = Integer.parseInt(size);
                isFixedSize = true;
            } else if (isFixedSize) {
                handlerException("Queues should have a " + ExecutorConstants.SIZE);
            }

            InternalQueue<Runnable> queue;
            if (isFixedSize) {
                queue = new FixedSizeQueue<Runnable>(p, s);
            } else {
                queue = new UnboundedQueue<Runnable>(p);
            }

            internalQueues.add(queue);
        }

        return internalQueues;
    }

    private static BeforeExecuteHandler createExecuteBeforeHandler(
            String className) throws AxisFault {
        try {
            Class c = Class.forName(className);
            Object o = c.newInstance();

            if (o instanceof BeforeExecuteHandler) {
                return (BeforeExecuteHandler) o;
            } else {
                handlerException("Before execute handler class, " +
                        className +
                        " is not type of BeforeExecuteHandler");
            }
        } catch (ClassNotFoundException e1) {
            handlerException("Before execute handler class, " +
                        className +
                        " is not found");
        } catch (IllegalAccessException e1) {
            handlerException("Before execute handler class, " +
                        className +
                        " cannot be accessed");
        } catch (InstantiationException e1) {
            handlerException("Before execute handler class, " +
                        className +
                        " cannot be instantiated");
        }
        return null;
    }

    private static NextQueueAlgorithm<Runnable> createNextQueueAlgo(
            String className) throws AxisFault {
        try {
            Class c = Class.forName(className);
            Object o = c.newInstance();

            if (o instanceof NextQueueAlgorithm) {
                return (NextQueueAlgorithm<Runnable>) o;
            } else {
                handlerException("NextQueue algorithm class, " +
                        className +
                        " is not type of BeforeExecuteHandler");
            }
        } catch (ClassNotFoundException e1) {
            handlerException("NextQueue algorithm class, " +
                    className + " is not found");
        } catch (IllegalAccessException e1) {
            handlerException("NextQueue algorithm class, " +
                    className + " cannot be accessed");
        } catch (InstantiationException e1) {
            handlerException("NextQueue algorithm class, " +
                    className + " cannot be instantiated");
        }
        return null;
    }

    private static QName createQname(String namespace, String name) {
        if (namespace == null) {
            return new QName(name);
        }
        return new QName(namespace, name);
    }

    private static void handlerException(String message) throws AxisFault {
        log.error(message);
        throw new AxisFault(message);
    }      
}
