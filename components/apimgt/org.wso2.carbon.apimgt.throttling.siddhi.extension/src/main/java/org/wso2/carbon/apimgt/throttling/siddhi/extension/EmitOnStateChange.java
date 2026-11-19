/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.throttling.siddhi.extension;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * De-duplicates events going out to GlobalThrottleStream.
 * <p/>
 * Emission rules:
 * <ul>
 *   <li>{@code isThrottled = true} is always emitted, keeping the gateway's throttle cache warm.</li>
 *   <li>{@code isThrottled = false} is emitted only when the key was previously throttled
 *       (the un-throttle notification).</li>
 *   <li>Anything else is dropped: the gateway already treats an absent key as un-throttled.</li>
 * </ul>
 * <p/>
 * Only currently-throttled keys are held in {@code throttleStateMap}, and each entry stores the
 * {@code expiryTimeStamp} of the window that throttled it. A scheduled sweep removes entries whose
 * timestamp has passed and emits an un-throttle event for each one, so the gateway drops its copy
 * too. Without that event the gateway's {@code ThrottleDataHolder.throttleDataMap} only clears a key
 * lazily when that key is requested again, so a key that is throttled and then goes silent is
 * retained for the life of the gateway JVM.
 * <p/>
 * The sweep runs on Siddhi's scheduler rather than a private thread: the scheduler delivers a
 * {@link ComplexEvent.Type#TIMER} event holding the query lock before re-entering {@link #process},
 * which is what makes emitting downstream from the sweep safe. All timing therefore uses
 * {@code executionPlanContext.getTimestampGenerator().currentTime()}, the same clock the scheduler
 * uses.
 * <p/>
 * Usage: {@code throttler:emitOnStateChange(key, isThrottled)}
 */
public class EmitOnStateChange extends StreamProcessor implements SchedulingProcessor {

    private static final long SWEEP_INTERVAL_MILLIS = 60000L;

    /** Bound on un-throttle events per sweep, so a large expiry burst cannot flood the JMS
     *  publisher in one pass. The remainder is picked up by the next sweep. */
    private static final int MAX_EVICTIONS_PER_SWEEP = 2000;

    /**
     * Index of {@code expiryTimeStamp} in the incoming event. Fixed by the exported stream, not by
     * convention: every policy template ends with {@code select *} into
     * {@code org.wso2.throttle.globalThrottle.stream}, whose shape is
     * {@code [0] throttleKey, [1] isThrottled, [2] expiryTimeStamp}. A plan with a different shape
     * does not deploy.
     */
    private static final int EXPIRY_TIMESTAMP_INDEX = 2;
    private static final int THROTTLE_KEY_INDEX = 0;
    private static final int IS_THROTTLED_INDEX = 1;

    private VariableExpressionExecutor keyExpressionExecutor;
    private VariableExpressionExecutor isThrottledExpressionExecutor;

    private Scheduler scheduler;

    /** Key -> expiryTimeStamp of the window that throttled it. Absent or expired means
     *  "not throttled". volatile because {@link #restoreState} replaces the map. */
    private volatile ConcurrentHashMap<String, Long> throttleStateMap = new ConcurrentHashMap<String, Long>();

    /** A clone of the last CURRENT event, used as a template for the un-throttle events built during
     *  the sweep. A StreamProcessor has no StreamEventPool or MetaStreamEvent, so cloning a real
     *  event is the only way to get one with the right shape for this stream. */
    private volatile StreamEvent templateEvent;

    /** When the sweep already requested from the scheduler is due, so repeated events do not queue
     *  duplicate wake-ups. */
    private final AtomicLong nextSweepAt = new AtomicLong(Long.MIN_VALUE);

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        final long currentTime = executionPlanContext.getTimestampGenerator().currentTime();

        while (streamEventChunk.hasNext()) {
            StreamEvent event = streamEventChunk.next();

            // TIMER events drive the sweep and carry no throttle data; running the expression
            // executors over one yields a null key and fails inside compute().
            if (event.getType() != ComplexEvent.Type.CURRENT) {
                streamEventChunk.remove();
                continue;
            }

            Boolean currentThrottleState = (Boolean) isThrottledExpressionExecutor.execute(event);
            String key = (String) keyExpressionExecutor.execute(event);
            final boolean isThrottledNow = Boolean.TRUE.equals(currentThrottleState);
            final long expiryTimeStamp = readExpiryTimeStamp(event, currentTime);

            templateEvent = streamEventCloner.copyStreamEvent(event);

            // compute() so that inspecting the previous state and setting the new one is atomic.
            final boolean[] wasThrottled = { false };
            throttleStateMap.compute(key, (k, existingExpiry) -> {
                if (existingExpiry != null && existingExpiry >= currentTime) {
                    wasThrottled[0] = true;
                }
                if (isThrottledNow) {
                    return expiryTimeStamp;
                }
                return null;
            });

            if (!isThrottledNow && !wasThrottled[0]) {
                streamEventChunk.remove();
            }
        }

        // Only sweep when the scheduled time has arrived, which is when the TIMER event that woke us
        // is delivered. Sweeping on every event would scan the whole map per request for no gain.
        ComplexEventChunk<StreamEvent> evictionChunk = null;
        if (currentTime >= nextSweepAt.get()) {
            evictionChunk = sweepExpired(currentTime, streamEventCloner);
        }
        armSweep(currentTime);

        if (evictionChunk != null) {
            nextProcessor.process(evictionChunk);
        }
        // The chunk can be empty here, after dropped events.
        if (streamEventChunk.getFirst() != null) {
            nextProcessor.process(streamEventChunk);
        }
    }

    /** Falls back to one sweep interval ahead if the attribute is missing, so a malformed plan still
     *  produces entries that expire. */
    private long readExpiryTimeStamp(StreamEvent event, long currentTime) {
        Object[] outputData = event.getOutputData();
        if (outputData != null && outputData.length > EXPIRY_TIMESTAMP_INDEX
                && outputData[EXPIRY_TIMESTAMP_INDEX] instanceof Long) {
            return (Long) outputData[EXPIRY_TIMESTAMP_INDEX];
        }
        return currentTime + SWEEP_INTERVAL_MILLIS;
    }

    /**
     * Removes entries whose window has closed and builds one un-throttle event per removed key.
     *
     * @return the un-throttle events, or null when nothing was evicted
     */
    private ComplexEventChunk<StreamEvent> sweepExpired(long currentTime, StreamEventCloner streamEventCloner) {

        if (throttleStateMap.isEmpty()) {
            return null;
        }

        StreamEvent template = templateEvent;
        ComplexEventChunk<StreamEvent> evictionChunk = null;
        int emitted = 0;

        Iterator<Map.Entry<String, Long>> iterator = throttleStateMap.entrySet().iterator();
        while (iterator.hasNext() && emitted < MAX_EVICTIONS_PER_SWEEP) {
            Map.Entry<String, Long> entry = iterator.next();
            Long expiryTimeStamp = entry.getValue();
            if (expiryTimeStamp >= currentTime) {
                continue;
            }
            // Conditional remove, so a refresh that landed after we read the value wins.
            if (!throttleStateMap.remove(entry.getKey(), expiryTimeStamp)) {
                continue;
            }
            if (template == null) {
                continue;
            }
            StreamEvent unThrottleEvent = streamEventCloner.copyStreamEvent(template);
            unThrottleEvent.setOutputData(entry.getKey(), THROTTLE_KEY_INDEX);
            unThrottleEvent.setOutputData(Boolean.FALSE, IS_THROTTLED_INDEX);
            unThrottleEvent.setType(ComplexEvent.Type.CURRENT);
            unThrottleEvent.setTimestamp(currentTime);
            if (evictionChunk == null) {
                evictionChunk = new ComplexEventChunk<StreamEvent>(true);
            }
            evictionChunk.add(unThrottleEvent);
            emitted++;
        }
        return evictionChunk;
    }

    /** Requests the next sweep, only while there is state to sweep and none is already pending. */
    private void armSweep(long currentTime) {
        if (scheduler == null || throttleStateMap.isEmpty()) {
            return;
        }
        long pending = nextSweepAt.get();
        if (pending > currentTime) {
            return;
        }
        long target = currentTime + SWEEP_INTERVAL_MILLIS;
        if (nextSweepAt.compareAndSet(pending, target)) {
            //sweep happens at correct time
            scheduler.notifyAt(target);
        }
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to throttler:emitOnStateChange" +
                                                       "(key,isThrottled), required 2, but found "
                                                       + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of " +
                                                       "throttler:emitOnStateChange(key,isThrottled), " +
                                                       "required " + Attribute.Type.STRING + ", " +
                                                       "but found " + attributeExpressionExecutors[0].getReturnType());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.BOOL) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument of " +
                                                       "throttler:emitOnStateChange(key,isThrottled), " +
                                                       "required " + Attribute.Type.BOOL + ", but found " +
                                                       attributeExpressionExecutors[1].getReturnType());
        }
        keyExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];
        isThrottledExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[1];
        return new ArrayList<Attribute>();    //this does not introduce any additional output attributes, hence returning an empty list.
    }

    @Override
    public void start() {
        //The sweep is armed from process(), because the scheduler is injected after init().
    }

    @Override
    public void stop() {
        templateEvent = null;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{throttleStateMap};
    }

    @Override
    public void restoreState(Object[] state) {
        // Start empty rather than trusting a snapshot. This map is a de-duplication cache, not
        // authoritative state: a key that is still throttled is re-added by its next event, and the
        // gateway ages out its own entries against the expiryTimeStamp it already holds. Restoring
        // would mean either trusting timestamps from another JVM or evicting them shortly after
        // startup, which would emit a burst of un-throttle events for keys that never resume.
        throttleStateMap = new ConcurrentHashMap<String, Long>();
    }
}
