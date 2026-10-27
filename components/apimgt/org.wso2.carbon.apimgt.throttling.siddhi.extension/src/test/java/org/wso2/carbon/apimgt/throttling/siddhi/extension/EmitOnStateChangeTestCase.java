/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.timestamp.SystemCurrentTimeMillisTimestampGenerator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tests for {@link EmitOnStateChange} -- the de-duplicating processor that feeds GlobalThrottleStream.
 * <p/>
 * Four concerns are covered:
 * <ol>
 *   <li><b>Emission contract</b> (via a real Siddhi runtime): which events reach the gateway. A
 *       regression here silently breaks throttle enforcement, so each state transition is pinned.</li>
 *   <li><b>Expiry and eviction</b>: entries are keyed on the event's own {@code expiryTimeStamp},
 *       and evicting one emits an un-throttle event so the gateway drops its copy even for keys that
 *       are never requested again.</li>
 *   <li><b>Robustness</b>: TIMER events (delivered by Siddhi's scheduler to drive the sweep) must not
 *       be mistaken for throttle events, and snapshots from older builds must restore rather than
 *       fail an execution plan on startup.</li>
 *   <li><b>Full throttle plan</b>: the window, count and de-duplication wired together as the policy
 *       templates deploy them, asserting on the state map itself so a key that is retained after its
 *       window is caught.</li>
 * </ol>
 */
public class EmitOnStateChangeTestCase {

    /**
     * Three attributes, matching org.wso2.throttle.globalThrottle.stream: the processor reads
     * expiryTimeStamp from index 2.
     */
    private static final String PLAN =
            "define stream ResultStream (throttleKey string, isThrottled bool, expiryTimeStamp long); " +
            "@info(name = 'query1') " +
            "from ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled) " +
            "select * insert into GlobalThrottleStream;";

    private static final long FAR_FUTURE_MILLIS = 600000L;

    /** Window length of {@link #THROTTLE_PLAN}; keep in step with the timeBatch argument. */
    private static final long WINDOW_MILLIS = 2000L;
    private static final int REQUEST_LIMIT = 3;

    /**
     * A request-count policy plan in the shape the policy templates deploy: timeBatch aligned to the
     * epoch, count grouped by throttleKey, then emitOnStateChange. The test namespace resolves to
     * {@link RecordingEmitOnStateChange} so the state map can be inspected.
     */
    private static final String THROTTLE_PLAN =
            "define stream RequestStream (messageID string, throttleKey string); " +
            "from RequestStream#throttler:timeBatch(2 sec, 0) " +
            "select throttleKey, (count(messageID) >= " + REQUEST_LIMIT + ") as isThrottled, expiryTimeStamp " +
            "group by throttleKey " +
            "insert all events into ResultStream; " +
            "@info(name = 'query1') " +
            "from ResultStream#throttlerTest:emitOnStateChange(throttleKey, isThrottled) " +
            "select * insert into GlobalThrottleStream;";

    private ExecutionPlanRuntime runtime;
    private final List<Event> emitted = new CopyOnWriteArrayList<Event>();

    @Before
    public void setUp() {
        emitted.clear();
        RecordingEmitOnStateChange.INSTANCES.clear();
    }

    @After
    public void tearDown() {
        if (runtime != null) {
            runtime.shutdown();
            runtime = null;
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private InputHandler startPlan() {
        return startRuntime(new SiddhiManager(), PLAN, "ResultStream");
    }

    private InputHandler startThrottlePlan() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("throttlerTest:emitOnStateChange", RecordingEmitOnStateChange.class);
        return startRuntime(siddhiManager, THROTTLE_PLAN, "RequestStream");
    }

    private InputHandler startRuntime(SiddhiManager siddhiManager, String plan, String inputStream) {
        runtime = siddhiManager.createExecutionPlanRuntime(plan);
        runtime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        emitted.add(event);
                    }
                }
            }
        });
        InputHandler inputHandler = runtime.getInputHandler(inputStream);
        runtime.start();
        return inputHandler;
    }

    /** Sends an event whose window expiry is far enough ahead that it will not be swept. */
    private void send(InputHandler in, String key, boolean throttled) throws InterruptedException {
        in.send(new Object[]{key, throttled, System.currentTimeMillis() + FAR_FUTURE_MILLIS});
    }

    private void send(InputHandler in, String key, boolean throttled, long expiry) throws InterruptedException {
        in.send(new Object[]{key, throttled, expiry});
    }

    private void settle() throws InterruptedException {
        Thread.sleep(500);
    }

    private long countEmitted(String key, boolean throttled) {
        long n = 0;
        for (Event e : emitted) {
            if (key.equals(e.getData()[0]) && Boolean.valueOf(throttled).equals(e.getData()[1])) {
                n++;
            }
        }
        return n;
    }

    /**
     * Builds a processor that is usable without the Siddhi parser, by injecting the
     * ExecutionPlanContext the parser would normally supply. Enough for the paths that never touch
     * the expression executors: TIMER handling, the sweep, and state restore.
     */
    private static EmitOnStateChange standaloneProcessor() throws Exception {
        EmitOnStateChange processor = new EmitOnStateChange();
        ExecutionPlanContext context = new ExecutionPlanContext();
        context.setTimestampGenerator(new SystemCurrentTimeMillisTimestampGenerator());
        Field field = findField(processor.getClass(), "executionPlanContext");
        field.setAccessible(true);
        field.set(processor, context);
        return processor;
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            }
        }
        throw new NoSuchFieldException(name);
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<String, Long> stateOf(EmitOnStateChange processor) {
        return (ConcurrentHashMap<String, Long>) processor.currentState()[0];
    }

    /**
     * Invokes the private sweep. A null StreamEventCloner is safe here because no template event has
     * been captured, so the sweep evicts without synthesising events.
     */
    private static void invokeSweep(EmitOnStateChange processor, long now) throws Exception {
        Method sweep = EmitOnStateChange.class.getDeclaredMethod(
                "sweepExpired", long.class, StreamEventCloner.class);
        sweep.setAccessible(true);
        sweep.invoke(processor, now, null);
    }

    /**
     * A cloner for a 3-attribute output stream. StreamEventCloner only reads the three data-list
     * sizes from the MetaStreamEvent, so this is enough to clone template events.
     */
    private static StreamEventCloner threeAttributeCloner() {
        MetaStreamEvent meta = new MetaStreamEvent();
        meta.addOutputData(new Attribute("throttleKey", Attribute.Type.STRING));
        meta.addOutputData(new Attribute("isThrottled", Attribute.Type.BOOL));
        meta.addOutputData(new Attribute("expiryTimeStamp", Attribute.Type.LONG));
        return new StreamEventCloner(meta, new StreamEventPool(meta, 4));
    }

    /** Installs the template event the sweep clones from. */
    private static void setTemplate(EmitOnStateChange processor, String key, boolean throttled, long expiry)
            throws Exception {
        StreamEvent template = new StreamEvent(0, 0, 3);
        template.setOutputData(new Object[]{key, throttled, expiry});
        template.setType(ComplexEvent.Type.CURRENT);
        Field field = findField(processor.getClass(), "templateEvent");
        field.setAccessible(true);
        field.set(processor, template);
    }

    /** A single TIMER event, which is what Siddhi's scheduler delivers to drive the sweep. */
    private static ComplexEventChunk<StreamEvent> timerChunk(long now) {
        StreamEvent timerEvent = new StreamEvent(0, 0, 3);
        timerEvent.setType(ComplexEvent.Type.TIMER);
        timerEvent.setTimestamp(now);
        ComplexEventChunk<StreamEvent> chunk = new ComplexEventChunk<StreamEvent>(true);
        chunk.add(timerEvent);
        return chunk;
    }

    private static void invokeProcess(EmitOnStateChange processor, ComplexEventChunk<StreamEvent> chunk,
                                      Processor downstream) throws Exception {
        Method process = EmitOnStateChange.class.getDeclaredMethod("process",
                ComplexEventChunk.class, Processor.class, StreamEventCloner.class, ComplexEventPopulater.class);
        process.setAccessible(true);
        process.invoke(processor, chunk, downstream, threeAttributeCloner(), null);
    }

    /** Records the chunks handed downstream, so emission can be asserted. */
    private static final class CapturingProcessor implements Processor {
        private final List<ComplexEventChunk<StreamEvent>> chunks =
                new CopyOnWriteArrayList<ComplexEventChunk<StreamEvent>>();

        @Override
        @SuppressWarnings("unchecked")
        public void process(ComplexEventChunk complexEventChunk) {
            chunks.add(complexEventChunk);
        }

        @Override
        public Processor getNextProcessor() {
            return null;
        }

        @Override
        public void setNextProcessor(Processor processor) {
        }

        @Override
        public void setToLast(Processor processor) {
        }

        @Override
        public Processor cloneProcessor(String key) {
            return this;
        }
    }

    private static int countEvents(CapturingProcessor downstream) {
        int n = 0;
        for (ComplexEventChunk<StreamEvent> chunk : downstream.chunks) {
            for (ComplexEvent event = chunk.getFirst(); event != null; event = event.getNext()) {
                n++;
            }
        }
        return n;
    }

    /** Remembers every instance Siddhi creates, so a test running a full plan can read its state. */
    public static class RecordingEmitOnStateChange extends EmitOnStateChange {
        static final List<RecordingEmitOnStateChange> INSTANCES =
                new CopyOnWriteArrayList<RecordingEmitOnStateChange>();

        @Override
        protected List<Attribute> init(AbstractDefinition inputDefinition,
                                       ExpressionExecutor[] attributeExpressionExecutors,
                                       ExecutionPlanContext executionPlanContext) {
            INSTANCES.add(this);
            return super.init(inputDefinition, attributeExpressionExecutors, executionPlanContext);
        }
    }

    /** Total entries held across the instances the running plan created. */
    private static int recordedStateSize() {
        Assert.assertFalse("the plan must create an EmitOnStateChange",
                RecordingEmitOnStateChange.INSTANCES.isEmpty());
        int size = 0;
        for (RecordingEmitOnStateChange processor : RecordingEmitOnStateChange.INSTANCES) {
            // Read through Map rather than the current map type, so a build with the old unbounded
            // map fails on the retained entries themselves instead of on a cast.
            size += ((Map<?, ?>) processor.currentState()[0]).size();
        }
        return size;
    }

    private static Long recordedExpiryOf(String key) {
        for (RecordingEmitOnStateChange processor : RecordingEmitOnStateChange.INSTANCES) {
            Long expiry = stateOf(processor).get(key);
            if (expiry != null) {
                return expiry;
            }
        }
        return null;
    }

    /**
     * timeBatch(.., 0) aligns windows to the epoch. If the current window is more than half gone,
     * waits for the next one, so the events a test sends next all land in the same window.
     */
    private static void awaitFreshWindow() throws InterruptedException {
        long intoWindow = System.currentTimeMillis() % WINDOW_MILLIS;
        if (intoWindow > WINDOW_MILLIS / 2) {
            Thread.sleep(WINDOW_MILLIS - intoWindow + 50L);
        }
    }

    // ------------------------------------------------------------------
    // Emission contract
    // ------------------------------------------------------------------

    /**
     * The customer scenario: traffic that never exceeds the limit. Every event carries
     * isThrottled=false for a key the processor has never seen, so nothing should be emitted --
     * the gateway already treats an absent key as un-throttled, making such an event a no-op.
     */
    @Test
    public void underLimitTrafficIsNeverEmitted() throws InterruptedException {
        InputHandler in = startPlan();
        for (int i = 0; i < 500; i++) {
            send(in, "10.0.0." + i + ":/api:1.0.0:Unauthenticated", false);
        }
        settle();
        Assert.assertEquals("under-limit traffic must not reach GlobalThrottleStream", 0, emitted.size());
    }

    /** absent -> true : emit, so the gateway starts blocking. */
    @Test
    public void transitionToThrottledIsEmitted() throws InterruptedException {
        InputHandler in = startPlan();
        send(in, "key1", false);   // dropped, key unknown
        send(in, "key1", true);    // emitted
        settle();
        Assert.assertEquals(1, emitted.size());
        Assert.assertEquals(Boolean.TRUE, emitted.get(0).getData()[1]);
    }

    /** throttled -> false : emit, so the gateway stops blocking. */
    @Test
    public void unThrottleTransitionIsEmitted() throws InterruptedException {
        InputHandler in = startPlan();
        send(in, "key1", true);
        send(in, "key1", false);
        settle();
        Assert.assertEquals(2, emitted.size());
        Assert.assertEquals(Boolean.TRUE, emitted.get(0).getData()[1]);
        Assert.assertEquals(Boolean.FALSE, emitted.get(1).getData()[1]);
    }

    /**
     * throttled -> true repeatedly : every event is emitted, refreshing the gateway's
     * expiryTimeStamp so a still-throttled key is not released early.
     */
    @Test
    public void repeatedThrottledEventsAreAllEmitted() throws InterruptedException {
        InputHandler in = startPlan();
        for (int i = 0; i < 3; i++) {
            send(in, "key1", true);
        }
        settle();
        Assert.assertEquals(3, emitted.size());
    }

    /** After an un-throttle has been sent, further false events are redundant and dropped. */
    @Test
    public void redundantFalseEventsAfterUnThrottleAreDropped() throws InterruptedException {
        InputHandler in = startPlan();
        send(in, "key1", true);     // emit (throttled)
        send(in, "key1", false);    // emit (un-throttle)
        send(in, "key1", false);    // drop
        send(in, "key1", false);    // drop
        settle();
        Assert.assertEquals(2, emitted.size());
    }

    /** Keys must be tracked independently of one another. */
    @Test
    public void keysAreTrackedIndependently() throws InterruptedException {
        InputHandler in = startPlan();
        send(in, "keyA", true);     // emit
        send(in, "keyB", false);    // drop, keyB unknown
        send(in, "keyA", false);    // emit, keyA was throttled
        settle();
        Assert.assertEquals(2, emitted.size());
        Assert.assertEquals("keyA", emitted.get(0).getData()[0]);
        Assert.assertEquals("keyA", emitted.get(1).getData()[0]);
    }

    // ------------------------------------------------------------------
    // Expiry and eviction
    // ------------------------------------------------------------------

    /**
     * The headline behaviour: an entry whose window has closed is evicted, an un-throttle event is
     * built for it, and that event is dispatched downstream. Without it the gateway keeps its copy
     * indefinitely, because ThrottleDataHolder only expires a key lazily when it is requested again.
     * <p/>
     * Driven through {@link EmitOnStateChange#process} with a TIMER event, which is exactly how
     * Siddhi's scheduler delivers the sweep, so the dispatch path is covered too.
     */
    @Test
    public void evictingAnExpiredEntryEmitsAnUnThrottleEvent() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        long now = System.currentTimeMillis();
        stateOf(processor).put("expiring", now - 1000L);
        setTemplate(processor, "someKey", true, now);

        CapturingProcessor downstream = new CapturingProcessor();
        invokeProcess(processor, timerChunk(now), downstream);

        Assert.assertTrue("the expired entry must be removed", stateOf(processor).isEmpty());
        Assert.assertEquals("one chunk must be dispatched downstream", 1, downstream.chunks.size());

        StreamEvent emittedEvent = downstream.chunks.get(0).getFirst();
        Assert.assertNotNull(emittedEvent);
        Assert.assertEquals("expiring", emittedEvent.getOutputData()[0]);
        Assert.assertEquals(Boolean.FALSE, emittedEvent.getOutputData()[1]);
        Assert.assertNull("exactly one event", emittedEvent.getNext());
    }

    /** A live entry must not be evicted or announced. */
    @Test
    public void liveEntryIsNotEvictedOrAnnounced() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        long now = System.currentTimeMillis();
        stateOf(processor).put("live", now + FAR_FUTURE_MILLIS);
        setTemplate(processor, "someKey", true, now);

        CapturingProcessor downstream = new CapturingProcessor();
        invokeProcess(processor, timerChunk(now), downstream);

        Assert.assertEquals(1, stateOf(processor).size());
        Assert.assertTrue("nothing should be dispatched", downstream.chunks.isEmpty());
    }

    /**
     * The stored deadline comes from the event's own expiryTimeStamp at index 2, not from a fixed
     * TTL. Index 2 is fixed by the exported GlobalThrottleStream shape.
     */
    @Test
    public void expiryIsReadFromTheEventsExpiryTimeStamp() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        long windowEnd = 1786532760000L;

        StreamEvent event = new StreamEvent(0, 0, 3);
        event.setOutputData(new Object[]{"key1", Boolean.TRUE, windowEnd});

        Method read = EmitOnStateChange.class.getDeclaredMethod(
                "readExpiryTimeStamp", StreamEvent.class, long.class);
        read.setAccessible(true);
        Assert.assertEquals(windowEnd, ((Long) read.invoke(processor, event, 0L)).longValue());
    }

    /** A plan without a LONG at index 2 must still produce an entry that expires. */
    @Test
    public void missingExpiryTimeStampFallsBackToAnExpiringValue() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();

        StreamEvent twoAttributeEvent = new StreamEvent(0, 0, 2);
        twoAttributeEvent.setOutputData(new Object[]{"key1", Boolean.TRUE});

        Method read = EmitOnStateChange.class.getDeclaredMethod(
                "readExpiryTimeStamp", StreamEvent.class, long.class);
        read.setAccessible(true);
        long now = System.currentTimeMillis();
        long fallback = (Long) read.invoke(processor, twoAttributeEvent, now);

        Assert.assertTrue("fallback must be in the future so the entry expires", fallback > now);
    }

    /** An un-throttle transition removes the entry, so the sweep has nothing left to announce. */
    @Test
    public void unThrottledKeyIsNotEvictedAgain() throws InterruptedException {
        InputHandler in = startPlan();
        send(in, "key1", true);
        send(in, "key1", false);   // explicit un-throttle: entry removed here
        settle();

        Assert.assertEquals("exactly one un-throttle, from the transition",
                1, countEmitted("key1", false));
    }

    /** The sweep evicts entries whose deadline has passed and leaves live ones alone. */
    @Test
    public void sweepEvictsOnlyExpiredEntries() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        long now = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> state = stateOf(processor);
        state.put("expired", now - 1000L);
        state.put("live", now + FAR_FUTURE_MILLIS);

        invokeSweep(processor, now);

        Assert.assertEquals(1, stateOf(processor).size());
        Assert.assertTrue("live entry must survive", stateOf(processor).containsKey("live"));
        Assert.assertFalse("expired entry must be evicted", stateOf(processor).containsKey("expired"));
    }

    /** With nothing ever seen there is no template event to clone; the sweep must still not throw. */
    @Test
    public void sweepWithoutATemplateEventIsSafe() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        long now = System.currentTimeMillis();
        stateOf(processor).put("expired", now - 1000L);

        invokeSweep(processor, now);

        Assert.assertTrue("entry is still evicted even without a template",
                stateOf(processor).isEmpty());
    }

    /**
     * A large expiry burst is announced in bounded batches so one sweep cannot flood the JMS
     * publisher. What is left over stays in the map and goes out on the next sweep. With no scheduler
     * set, every TIMER chunk sweeps, which lets the test drive two sweeps back to back.
     */
    @Test
    public void sweepAnnouncesAtMostTheBoundAndLeavesTheRestForTheNextSweep() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();
        Field bound = EmitOnStateChange.class.getDeclaredField("MAX_EVICTIONS_PER_SWEEP");
        bound.setAccessible(true);
        int maxPerSweep = bound.getInt(null);
        int leftOver = 500;

        long now = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> state = stateOf(processor);
        for (int i = 0; i < maxPerSweep + leftOver; i++) {
            state.put("expired-" + i, now - 1000L);
        }
        setTemplate(processor, "someKey", true, now);

        CapturingProcessor firstSweep = new CapturingProcessor();
        invokeProcess(processor, timerChunk(now), firstSweep);
        Assert.assertEquals(maxPerSweep, countEvents(firstSweep));
        Assert.assertEquals("the rest must wait for the next sweep", leftOver, stateOf(processor).size());

        CapturingProcessor secondSweep = new CapturingProcessor();
        invokeProcess(processor, timerChunk(now), secondSweep);
        Assert.assertEquals(leftOver, countEvents(secondSweep));
        Assert.assertTrue(stateOf(processor).isEmpty());
    }

    // ------------------------------------------------------------------
    // Robustness
    // ------------------------------------------------------------------

    /**
     * Siddhi's scheduler delivers TIMER events to drive the sweep. They carry no throttle data, so
     * running the expression executors over one would yield a null key and throw inside
     * ConcurrentHashMap.compute() -- and AbstractStreamProcessor swallows that as a "Dropping event
     * chunk" log, making it a silent failure. The type guard must drop them instead.
     */
    @Test
    public void timerEventIsDroppedWithoutTouchingTheMap() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();

        StreamEvent timerEvent = new StreamEvent(0, 0, 3);
        timerEvent.setType(ComplexEvent.Type.TIMER);
        timerEvent.setTimestamp(System.currentTimeMillis());
        ComplexEventChunk<StreamEvent> chunk = new ComplexEventChunk<StreamEvent>(true);
        chunk.add(timerEvent);

        // nextProcessor may be null: the chunk is empty once the TIMER event is dropped, so nothing
        // is forwarded. Reaching a NullPointerException here would mean the guard did not fire.
        Method process = EmitOnStateChange.class.getDeclaredMethod("process",
                ComplexEventChunk.class,
                org.wso2.siddhi.core.query.processor.Processor.class,
                org.wso2.siddhi.core.event.stream.StreamEventCloner.class,
                org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater.class);
        process.setAccessible(true);
        process.invoke(processor, chunk, null, null, null);

        Assert.assertTrue("a TIMER event must not create map state", stateOf(processor).isEmpty());
    }

    /**
     * Restore starts from an empty map instead of trusting a snapshot, so it accepts any payload --
     * including the legacy {@code Map<String, Boolean>} shape and null -- without throwing. The
     * pre-fix implementation cast {@code state[0]} unchecked, which failed the execution plan on
     * startup. Starting empty also means a restart cannot emit a burst of un-throttle events for
     * keys that never resume: a still-throttled key is re-added by its next event, and the gateway
     * ages out its own entries against the expiryTimeStamp it already holds.
     */
    @Test
    public void restoreStateStartsEmptyAndAcceptsAnyPayload() throws Exception {
        EmitOnStateChange processor = standaloneProcessor();

        Map<String, Boolean> legacy = new HashMap<String, Boolean>();
        legacy.put("throttledKey", Boolean.TRUE);
        legacy.put("unthrottledKey", Boolean.FALSE);
        processor.restoreState(new Object[]{legacy});
        Assert.assertTrue("a legacy Boolean snapshot must not be carried over",
                stateOf(processor).isEmpty());

        ConcurrentHashMap<String, Long> current = new ConcurrentHashMap<String, Long>();
        current.put("key1", System.currentTimeMillis() + FAR_FUTURE_MILLIS);
        processor.restoreState(new Object[]{current});
        Assert.assertTrue(stateOf(processor).isEmpty());

        processor.restoreState(new Object[]{null});
        Assert.assertTrue(stateOf(processor).isEmpty());

        processor.restoreState(new Object[]{"not a map"});
        Assert.assertTrue(stateOf(processor).isEmpty());

        processor.restoreState(new Object[0]);
        Assert.assertTrue(stateOf(processor).isEmpty());
    }

    // ------------------------------------------------------------------
    // Full throttle plan
    // ------------------------------------------------------------------

    /**
     * The reported leak: an unauthenticated API keyed on client IP, with every caller under the
     * limit. Each caller is a new key, and before the fix every one of them stayed in
     * throttleStateMap for the life of the JVM.
     */
    @Test
    public void underLimitTrafficFromManyClientIpsLeavesNoState() throws InterruptedException {
        InputHandler in = startThrottlePlan();
        for (int i = 0; i < 1000; i++) {
            String clientIp = "10.0." + (i / 256) + "." + (i % 256);
            in.send(new Object[]{"msg-" + i, clientIp + ":/api/1.0.0:1.0.0:Unauthenticated"});
        }
        settle();

        Assert.assertEquals("under-limit keys must not be retained", 0, recordedStateSize());
        Assert.assertEquals("under-limit traffic must not reach GlobalThrottleStream", 0, emitted.size());
    }

    /**
     * A key over the limit is announced with its window's expiry and held only for that window. Once
     * the window closes, the count starts again, the key's next event drops the stale entry, and no
     * un-throttle is sent for it: the gateway has already released the key against the
     * expiryTimeStamp it was given. Going over the limit again throttles it for the new window.
     */
    @Test
    public void overLimitKeyIsHeldOnlyForItsWindow() throws InterruptedException {
        String key = "10.0.0.1:/api/1.0.0:1.0.0:Unauthenticated";
        InputHandler in = startThrottlePlan();
        awaitFreshWindow();
        for (int i = 0; i < REQUEST_LIMIT; i++) {
            in.send(new Object[]{"msg-" + i, key});
        }
        settle();

        Assert.assertEquals("only the request that reaches the limit is announced", 1, emitted.size());
        Assert.assertEquals(Boolean.TRUE, emitted.get(0).getData()[1]);
        long firstWindowExpiry = (Long) emitted.get(0).getData()[2];
        Assert.assertEquals(1, recordedStateSize());
        Assert.assertEquals(Long.valueOf(firstWindowExpiry), recordedExpiryOf(key));

        Thread.sleep(Math.max(0L, firstWindowExpiry - System.currentTimeMillis()) + 200L);
        in.send(new Object[]{"msg-next-window-0", key});
        settle();

        Assert.assertEquals("no un-throttle once the window has closed", 1, emitted.size());
        Assert.assertEquals("the closed window's entry must be dropped", 0, recordedStateSize());

        for (int i = 1; i < REQUEST_LIMIT; i++) {
            in.send(new Object[]{"msg-next-window-" + i, key});
        }
        settle();

        Assert.assertEquals("the count restarts, so the key throttles again", 2, emitted.size());
        long secondWindowExpiry = (Long) emitted.get(1).getData()[2];
        Assert.assertTrue("the new entry carries the new window's expiry", secondWindowExpiry > firstWindowExpiry);
        Assert.assertEquals(Long.valueOf(secondWindowExpiry), recordedExpiryOf(key));
    }
}
