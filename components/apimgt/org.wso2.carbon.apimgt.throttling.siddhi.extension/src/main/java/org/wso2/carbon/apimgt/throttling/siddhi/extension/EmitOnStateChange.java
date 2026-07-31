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
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Custom Siddhi StreamProcessor used by the API-M Traffic Manager's throttle pipeline
 * to de-duplicate outgoing GlobalThrottleStream events.
 * <p/>
 * Emission rules (unchanged from the original implementation):
 * <ul>
 *   <li>An event with {@code isThrottled = true} is <b>always</b> emitted, regardless of the
 *       previous state. This keeps the downstream gateway's throttle cache warm for
 *       currently-blocked keys.</li>
 *   <li>An event with {@code isThrottled = false} is emitted only when the previous state
 *       for that key was {@code true} (transition true -> false, i.e. un-throttle notification).</li>
 *   <li>Repeated {@code false} events for the same key are dropped (dedup).</li>
 *   <li>A brand-new key seen with {@code isThrottled = false} is dropped: the gateway
 *       already treats absent keys as un-throttled, so the event would be a no-op.</li>
 * </ul>
 * <p/>
 * Memory-bounding: only currently-throttled keys are retained in {@code throttleStateMap}, and each
 * entry has a configurable TTL. Once the TTL expires (or the key transitions back to un-throttled),
 * the entry is removed. Keys that are not throttled are never stored. The gateway's downstream
 * throttle cache ages out its own entries via {@code expiryTimeStamp}, so a Traffic Manager
 * restart / eviction is safe.
 * <p/>
 * Concurrency:
 * <ul>
 *   <li>Per-key state transitions in {@link #process} use {@link ConcurrentHashMap#compute} so the
 *       "check previous state, decide new value" step happens atomically inside the map's bin
 *       lock. Two concurrent {@code process} invocations against the same key cannot lose or
 *       stale-overwrite each other's updates.</li>
 *   <li>{@code throttleStateMap} is {@code volatile}: {@link #restoreState} reassigns it to a new
 *       instance, and readers on other threads must observe the swap immediately.</li>
 *   <li>Expired-entry cleanup runs off the event-processing hot path on a daemon
 *       {@link ScheduledExecutorService} started in {@link #start()} and shut down in
 *       {@link #stop()}. Under high-cardinality traffic this keeps the O(n) map sweep from
 *       introducing latency spikes on the thread dispatching events.</li>
 * </ul>
 * <p/>
 * Timing uses {@link System#nanoTime()} exclusively for expiry and sweep decisions. This makes
 * the map robust to wall-clock adjustments (NTP jumps, admin changes). The trade-off is that
 * a snapshot taken in one JVM cannot be replayed literally into another, because {@code nanoTime}
 * is per-JVM. {@link #restoreState(Object[])} therefore treats restored entries as
 * "recently throttled" and reassigns them a fresh TTL.
 * <p/>
 * TTL is set via the system property {@code siddhi.throttle.state.ttl.minutes} (default: 24 hours).
 * The default covers every batch window shape APIM ships (per-minute, per-hour, per-day quotas)
 * with headroom, so a key throttled at the very start of any window is guaranteed to still be in
 * the map when the next window's traffic arrives. Operators can lower it for extreme-cardinality
 * traffic where a tighter memory ceiling matters more than covering the longest windows.
 * <p/>
 * Usage:
 * <pre>
 * throttler:emitOnStateChange(key, isThrottled)
 * </pre>
 * <ul>
 *   <li>{@code key}: throttle key evaluated by the enclosing query.</li>
 *   <li>{@code isThrottled}: throttle decision made by the enclosing query.</li>
 * </ul>
 * Example:
 * <pre>
 * from DecisionStream#throttler:emitOnStateChange(key, isThrottled)
 * select *
 * insert into AlertStream;
 * </pre>
 */
public class EmitOnStateChange extends StreamProcessor {

    private static final String TTL_SYSTEM_PROPERTY = "siddhi.throttle.state.ttl.minutes";
    // 24 hours -- comfortably above the longest batch window shape APIM supports (daily quotas),
    // while still keeping the map memory-bounded. Operators can shrink it via the system property
    // if extreme-cardinality traffic warrants a tighter ceiling.
    private static final long DEFAULT_TTL_MINUTES = 24L * 60L;
    private static final long SWEEP_INTERVAL_SECONDS = 60L;

    private VariableExpressionExecutor keyExpressionExecutor;
    private VariableExpressionExecutor isThrottledExpressionExecutor;

    // Map value = absolute expiry deadline expressed in System.nanoTime() units.
    // Presence with an unexpired value == "was throttled recently".
    // Absence or expired == "was not throttled" (treated identically).
    // volatile: restoreState() reassigns this field to a new map; readers must observe the swap.
    private volatile ConcurrentHashMap<String, Long> throttleStateMap = new ConcurrentHashMap<String, Long>();

    private final long ttlNanos = resolveTtlNanos();

    // Daemon-thread scheduler that runs the expired-entry sweep off the event hot path.
    private volatile ScheduledExecutorService sweepExecutor;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        final long nowNanos = System.nanoTime();
        while (streamEventChunk.hasNext()) {
            StreamEvent event = streamEventChunk.next();
            Boolean currentThrottleState = (Boolean) isThrottledExpressionExecutor.execute(event);
            String key = (String) keyExpressionExecutor.execute(event);
            final boolean isThrottledNow = Boolean.TRUE.equals(currentThrottleState);

            // Atomically inspect the previous state and set the new one, so a concurrent
            // put/remove for the same key cannot interleave.
            //   - previous entry present & unexpired -> "was throttled"
            //   - isThrottledNow == true             -> refresh expiry (keep in map)
            //   - isThrottledNow == false            -> drop from map (remove or stay absent)
            final boolean[] wasThrottled = { false };
            throttleStateMap.compute(key, (k, existingExpiry) -> {
                if (existingExpiry != null && nowNanos - existingExpiry <= 0) {
                    wasThrottled[0] = true;
                }
                if (isThrottledNow) {
                    return nowNanos + ttlNanos;
                }
                return null;
            });

            // Drop only when both prior and current state are "not throttled".
            if (!isThrottledNow && !wasThrottled[0]) {
                streamEventChunk.remove();
            }
        }

        nextProcessor.process(streamEventChunk);
    }

    private void sweepExpired() {
        long nowNanos = System.nanoTime();
        Iterator<Map.Entry<String, Long>> it = throttleStateMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            // Subtraction handles the theoretical wraparound of nanoTime cleanly.
            if (nowNanos - entry.getValue() > 0) {
                it.remove();
            }
        }
    }

    private static long resolveTtlNanos() {
        long minutes = DEFAULT_TTL_MINUTES;
        String raw = System.getProperty(TTL_SYSTEM_PROPERTY);
        if (raw != null && !raw.isEmpty()) {
            try {
                long parsed = Long.parseLong(raw.trim());
                if (parsed > 0) {
                    minutes = parsed;
                }
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return minutes * 60L * 1000L * 1000L * 1000L;
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
        if (sweepExecutor == null) {
            sweepExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread t = new Thread(runnable, "throttle-state-sweeper");
                t.setDaemon(true);
                return t;
            });
            sweepExecutor.scheduleAtFixedRate(this::sweepExpired,
                    SWEEP_INTERVAL_SECONDS, SWEEP_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        ScheduledExecutorService current = sweepExecutor;
        sweepExecutor = null;
        if (current != null) {
            current.shutdownNow();
        }
    }

    @Override
    public Object[] currentState() {
        return new Object[]{throttleStateMap};
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreState(Object[] state) {
        Object stored = state[0];
        ConcurrentHashMap<String, Long> replacement = new ConcurrentHashMap<String, Long>();
        if (stored instanceof Map) {
            Map<String, ?> snapshot = (Map<String, ?>) stored;
            // Snapshot values are opaque here: they may be nanoTime deadlines from this same JVM,
            // legacy millis-based values from an earlier build of this extension, or Boolean flags
            // from the pre-fix HashMap<String, Boolean> shape. Since nanoTime is per-JVM (not
            // portable across restarts or HA failover), treat every restored key as "recently
            // throttled" and reassign a fresh TTL. Legacy FALSE entries are dropped because in
            // the new model they are equivalent to "not in map".
            long freshDeadlineNanos = System.nanoTime() + ttlNanos;
            for (Map.Entry<String, ?> entry : snapshot.entrySet()) {
                Object v = entry.getValue();
                if (Boolean.FALSE.equals(v)) {
                    continue;
                }
                replacement.put(entry.getKey(), freshDeadlineNanos);
            }
        }
        throttleStateMap = replacement;
    }
}
