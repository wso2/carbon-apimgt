/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.MicroGatewayAPIUsageConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsageFileWriter;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.WrappedEventFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.EventQueueFullException;
import org.wso2.carbon.databridge.commons.EBCommonsConstants;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This is the DataPublisher implementation for publishing events into a file
 */
public class FileDataPublisher {

    private static final Log log = LogFactory.getLog(FileDataPublisher.class);

    private EventQueue eventQueue = null;

    private static final int FAILED_EVENT_LOG_INTERVAL = 10000;

    /**
     * The last failed event time kept, use to determine when to log an warning
     * message, without continuously doing so.
     */
    private long lastFailedEventTime;

    /**
     * The current failed event count. A normal long is used here, rather
     * than an AtomicLong, since this is not a critical stat.
     */
    private long failedEventCount;

    public FileDataPublisher() throws UsagePublisherException {
        int queueSize = 32768; //Default Value
        try {
            queueSize = AgentHolder.getInstance().getDefaultDataEndpointAgent().getAgentConfiguration().getQueueSize();
        } catch (DataEndpointAgentConfigurationException e) {
            log.warn("Error occurred while getting the Queue size from Agent Configuration. Hence default size " +
                    "(32768) will be used");
        }
        this.eventQueue = new EventQueue(queueSize);
        UsageFileWriter.getInstance();
    }

    public boolean tryPublish(String streamID, long timeStamp, Object[] metaDataArray, Object[] correlationDataArray,
                           Object[] payloadDataArray) {
        return tryPublish(new Event(streamID, timeStamp, metaDataArray, correlationDataArray, payloadDataArray));
    }

    private boolean tryPublish(Event event) {
        boolean sent = true;
        try {
            if (eventQueue != null) {
                eventQueue.tryPut(event);
            }
        } catch (EventQueueFullException e) {
            this.onEventQueueFull(event);
            sent = false;
        }
        return sent;
    }

    private void onEventQueueFull(Event event) {
        this.failedEventCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFailedEventTime > FAILED_EVENT_LOG_INTERVAL) {
            log.warn("Event queue is full, unable to process the event, " + this.failedEventCount
                    + " events dropped so far.");
            this.lastFailedEventTime = currentTime;
        }
        if (log.isDebugEnabled()) {
            log.debug("Dropped Event: " + event.toString());
        }
    }

    public void shutdown() throws UsagePublisherException {
        if (eventQueue != null) {
            eventQueue.shutdown();
        }
        UsageFileWriter.getInstance().closeFileResources();
    }

    static class EventQueue {
        private RingBuffer<WrappedEventFactory.WrappedEvent> ringBuffer = null;
        private Disruptor<WrappedEventFactory.WrappedEvent> eventQueueDisruptor = null;
        private ExecutorService eventQueuePool = null;

        EventQueue(int queueSize) {
            eventQueuePool = Executors.newSingleThreadExecutor(
                    new DataBridgeThreadFactory("EventQueue"));
            eventQueueDisruptor = new Disruptor<>(new WrappedEventFactory(), queueSize, eventQueuePool,
                    ProducerType.MULTI, new BlockingWaitStrategy());
            eventQueueDisruptor.handleEventsWith(new EventQueueWorker());
            this.ringBuffer = eventQueueDisruptor.start();
        }


        private void tryPut(Event event) throws EventQueueFullException {
            long sequence;
            try {
                sequence = this.ringBuffer.tryNext(1);
                WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
                bufferedEvent.setEvent(event);
                this.ringBuffer.publish(sequence);
            } catch (InsufficientCapacityException e) {
                throw new EventQueueFullException("Cannot persist events because the event queue is full", e);
            }
        }

        private void shutdown() {
            eventQueuePool.shutdown();
            eventQueueDisruptor.shutdown();
        }
    }

    static class EventQueueWorker implements EventHandler<WrappedEventFactory.WrappedEvent> {

        @Override
        public void onEvent(WrappedEventFactory.WrappedEvent wrappedEvent, long sequence, boolean endOfBatch) {
            Event event = wrappedEvent.getEvent();
            StringBuilder builder = new StringBuilder();
            builder.append(MicroGatewayAPIUsageConstants.STREAM_ID)
                    .append(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)
                    .append(event.getStreamId())
                    .append(MicroGatewayAPIUsageConstants.EVENT_SEPARATOR);
            builder.append(MicroGatewayAPIUsageConstants.TIME_STAMP)
                    .append(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)
                    .append(event.getTimeStamp())
                    .append(MicroGatewayAPIUsageConstants.EVENT_SEPARATOR);
            builder.append(MicroGatewayAPIUsageConstants.META_DATA)
                    .append(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)
                    .append((event.getMetaData() == null ? null :
                            StringUtils.join(event.getMetaData(), MicroGatewayAPIUsageConstants.OBJECT_SEPARATOR)))
                    .append(MicroGatewayAPIUsageConstants.EVENT_SEPARATOR);
            builder.append(MicroGatewayAPIUsageConstants.CORRELATION_DATA)
                    .append(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)
                    .append((event.getCorrelationData() == null ? null :
                            StringUtils.join(event.getCorrelationData(), MicroGatewayAPIUsageConstants.OBJECT_SEPARATOR)))
                    .append(MicroGatewayAPIUsageConstants.EVENT_SEPARATOR);
            builder.append(MicroGatewayAPIUsageConstants.PAYLOAD_DATA)
                    .append(MicroGatewayAPIUsageConstants.KEY_VALUE_SEPARATOR)
                    .append((event.getPayloadData() == null ? null :
                            StringUtils.join(event.getPayloadData(), MicroGatewayAPIUsageConstants.OBJECT_SEPARATOR)));
            builder.append(MicroGatewayAPIUsageConstants.NEW_LINE);
            try {
                UsageFileWriter.getInstance().writeToFile(builder.toString());
            } catch (UsagePublisherException e) {
                log.warn("Error occurred while getting the Usage File Writer.", e);
            }
        }
    }
}
