/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.throttle.core.*;
import org.apache.synapse.commons.throttle.core.internal.DistributedThrottleProcessor;
import org.apache.synapse.commons.throttle.core.internal.ThrottleServiceDataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottleUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for processing throttle conditions in order to throttle based on subscription burst
 * controlling and backend throttling requests. The throttle condition evaluation is done considering a time window
 * which is based on the start timestamp of the initial request.
 */
public class HybridThrottleProcessor implements DistributedThrottleProcessor {
    private static Log log = LogFactory.getLog(HybridThrottleProcessor.class.getName());
    private static final String WSO2_SYNC_MODE_INIT_CHANNEL = "wso2_sync_mode_init_channel";
    /**
     * callerContextId to nextTimeWindow mapping
     */
    ConcurrentHashMap<String, String> syncModeNotifiedMap = new ConcurrentHashMap<>();
    JedisPool redisPool;
    private ThrottleDataHolder dataHolder;
    private String gatewayId;
    private static final String SYNC_MODE_MSG_PART_DELIMITER = "___";

    public HybridThrottleProcessor() {
        redisPool = ServiceReferenceHolder.getInstance().getRedisPool();
        RedisConfig redisConfig = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getRedisConfig();
        gatewayId = redisConfig.getGatewayId();

        ScheduledExecutorService syncModeInitChannelSubscriptionExecutor = Executors.newScheduledThreadPool(1);
        syncModeInitChannelSubscriptionExecutor.scheduleAtFixedRate(new SyncModeInitChannelSubscription(), 0, 1,
                TimeUnit.MILLISECONDS);

        ScheduledExecutorService channelSubscriptionCounterExecutor = Executors.newScheduledThreadPool(1);
        int gatewayCountCheckingFrequency = 10000;
        channelSubscriptionCounterExecutor.scheduleAtFixedRate(new ChannelSubscriptionCounterTask(), 15000,
                gatewayCountCheckingFrequency, TimeUnit.MILLISECONDS);
    }

    private class SyncModeInitChannelSubscription implements Runnable {
        private static final int initialRedisConnectionRetryInterval = 5000;
        long redisConnectionRetryInterval = initialRedisConnectionRetryInterval;

        public void run() {
            if (log.isTraceEnabled()) {
                log.trace("SyncModeInitChannelSubscription Thread Running");
            }
            JedisPubSub jedisPubSub = new JedisPubSub() {
                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    super.onSubscribe(channel, subscribedChannels);
                    if (log.isTraceEnabled()) {
                        log.trace("Gateway is Subscribed to " + channel);
                    }
                    redisConnectionRetryInterval = initialRedisConnectionRetryInterval;
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    super.onUnsubscribe(channel, subscribedChannels);
                    if (log.isWarnEnabled()) {
                        log.warn("Gateway client is Unsubscribed from channel: " + channel);
                    }
                }

                @Override
                public void onMessage(String channel, String syncModeInitMsg) {
                    super.onMessage(channel, syncModeInitMsg);
                    if (log.isTraceEnabled()) {
                        log.trace("Sync mode changed message received to this node " + gatewayId + ". Channel: "
                                + channel + " " + "Msg : " + syncModeInitMsg);
                    }
                    if (syncModeInitMsg.startsWith(gatewayId)) {
                        if (log.isTraceEnabled()) {
                            log.trace("Ignoring as message received to own node ! ");
                        }
                        return;
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("Message received from channel: " + channel + " Message: " + syncModeInitMsg);
                    }
                    String[] messageParts = syncModeInitMsg.split(SYNC_MODE_MSG_PART_DELIMITER);
                    // messageParts[0] = gatewayId , messageParts[1] = callerContextId , messageParts[2] = nextTimeWindow
                    String callerContextId = messageParts[1];
                    String nextTimeWindow = messageParts[2];
                    if (log.isTraceEnabled()) {
                        log.trace("Going to put callerContextId " + callerContextId
                                + " into syncModeNotifiedSet with nextTimeWindow " + nextTimeWindow);
                    }

                    syncModeNotifiedMap.put(callerContextId, nextTimeWindow);
                    if (log.isTraceEnabled()) {
                        log.trace("Caller " + callerContextId + " SWITCHED TO SYNC MODE by message received ! :");
                    }
                    // sync throttle params to redis to consider local unpublished request counts in distributed counters
                    if (dataHolder != null) {
                        if (log.isTraceEnabled()) {
                            log.trace(
                                    "DataHolder is not null so running syncing tasks." + " message:" + syncModeInitMsg);
                        }

                        CallerContext callerContext = dataHolder.getCallerContext(callerContextId);
                        if (callerContext != null) {
                            if (log.isTraceEnabled()) {
                                log.trace("Running forced syncing tasks for callerContext: " + callerContext.getId()
                                        + " message:" + syncModeInitMsg);
                            }
                            synchronized (callerContext.getId().intern()) {
                                if (SharedParamManager.lockSharedKeys(callerContext.getId(), gatewayId)) {
                                    long syncingStartTime = System.currentTimeMillis();
                                    syncThrottleWindowParams(callerContext, false);
                                    syncThrottleCounterParams(callerContext, false,
                                            new RequestContext(System.currentTimeMillis()));
                                    SharedParamManager.releaseSharedKeys(callerContext.getId());
                                    long timeNow = System.currentTimeMillis();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Current time:" + timeNow
                                                + "In force syncing process, Lock released in " + (timeNow
                                                - syncingStartTime) + " ms for callerContext: "
                                                + callerContext.getId());
                                    }

                                } else {
                                    if (log.isTraceEnabled()) {
                                        log.trace("Current time:" + System.currentTimeMillis()
                                                + " Failed to acquire lock for callerContext: " + callerContext.getId()
                                                + " message:" + syncModeInitMsg);
                                    }
                                }
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace("CallerContext is null so not running syncing tasks. message:"
                                        + syncModeInitMsg);
                            }
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace(
                                    "DataHolder is null so not running syncing tasks. message:" + syncModeInitMsg);
                        }
                    }
                }
            };
            subscribeWithRetry(jedisPubSub);
        }

        /**
         * This method is used to subscribe to the channel in Redis with reconnection tries if connection was broken.
         */
        private void subscribeWithRetry(JedisPubSub jedisPubSub) {
            try (Jedis jedis = redisPool.getResource()) {
                jedis.subscribe(jedisPubSub, WSO2_SYNC_MODE_INIT_CHANNEL);
            } catch (JedisConnectionException e) {
                log.error("Could not establish connection by retrieving a resource from the redis pool. So error "
                        + "occurred while subscribing to channel: " + WSO2_SYNC_MODE_INIT_CHANNEL, e);
                log.info("Next retry to subscribe to channel " + WSO2_SYNC_MODE_INIT_CHANNEL + " in "
                        + redisConnectionRetryInterval + " seconds");
                try {
                    // sleep for given duration before retrying to subscribe, if the redis channel
                    // subscription failed
                    Thread.sleep(redisConnectionRetryInterval);
                    redisConnectionRetryInterval *= 2;
                } catch (InterruptedException ex) {
                    log.error("Error while sleeping before retrying to subscribe to channel: "
                            + WSO2_SYNC_MODE_INIT_CHANNEL, ex);
                }
                subscribeWithRetry(jedisPubSub);
            }
        }
    }

    /**
     * This task is used to count the number of gateways subscribed to the channel in Redis.
     */
    private class ChannelSubscriptionCounterTask implements Runnable {

        @Override public void run() {
            Map<String, String> channelCountMap;
            try (Jedis jedis = redisPool.getResource()) {
                channelCountMap = jedis.pubsubNumSub(WSO2_SYNC_MODE_INIT_CHANNEL);
            }
            for (Map.Entry<String, String> entry : channelCountMap.entrySet()) {
                String channel = entry.getKey();
                int gatewayCount = Integer.parseInt(entry.getValue());
                ServiceReferenceHolder.getInstance().setGatewayCount(gatewayCount);
                if (log.isTraceEnabled()) {
                    log.trace("ChannelSubscriptionCounterTask : channel = " + channel + ". Set Gateway count to "
                            + gatewayCount);
                }
            }
        }
    }

    /**
     * Evaluate whether the request is allowed or not based on the window of unit time
     *
     * @return boolean value indicating whether the request can be accessed or not
     */
    @Override
    public boolean canAccessBasedOnUnitTime(CallerContext callerContext, CallerConfiguration configuration,
            ThrottleContext throttleContext, RequestContext requestContext) {
        if (log.isTraceEnabled()) {
            log.trace("Starting evaluating whether can access based on unit time.");
        }
        setLocalQuota(callerContext, configuration);
        setThrottleParamSyncMode(callerContext, requestContext);

        if (dataHolder == null) {
            dataHolder = (ThrottleDataHolder) throttleContext.getConfigurationContext()
                    .getPropertyNonReplicable(ThrottleConstants.THROTTLE_INFO_KEY);
        }

        boolean canAccess;
        if (callerContext.getNextTimeWindow() > requestContext.getRequestTime()) {
            canAccess = canAccessIfUnitTimeNotOver(callerContext, configuration, throttleContext, requestContext);
        } else {
            canAccess = canAccessIfUnitTimeOver(callerContext, configuration, throttleContext, requestContext);
        }
        if (canAccess) {
            callerContext.incrementLocalHits();
            if (log.isTraceEnabled()) {
                log.trace("localHits after deciding whether can access:" + callerContext.getLocalHits());
            }
        }
        if (gatewayId == null || gatewayId.isEmpty()) {
            log.error("gateway_id is not configured in deployment.toml. So the process of syncing Throttle params, "
                    + "skipped. Please add the gateway_id configuration under [apim.redis_config] section in "
                    + "deployment.toml.");
            return canAccess;
        }

        /* Convert the sync mode to sync and publish mode-changing message to redis only if the syncing mode is still
         async and requests are not yet throttled. "canAccess == true" condition is checked to avoid unnecessary syncings
         and publishing messages to redis when the requests are already throttled. (after the requests are
         throttled, next requests are processed in async mode) */
        if (callerContext.getLocalHits() == callerContext.getLocalQuota()
                && !callerContext.isThrottleParamSyncingModeSync() && canAccess == true) {
            if (log.isTraceEnabled()) {
                log.trace("Local quota reached. SWITCHED TO SYNC MODE !!! local hits = "
                        + callerContext.getLocalHits());
            }
            callerContext.setIsThrottleParamSyncingModeSync(true);
            String message =
                    gatewayId + SYNC_MODE_MSG_PART_DELIMITER + callerContext.getId() + SYNC_MODE_MSG_PART_DELIMITER
                            + callerContext.getNextTimeWindow();

            if (dataHolder != null) {
                if (log.isTraceEnabled()) {
                    log.trace("DataHolder is not null so running syncing tasks");
                }
                synchronized (callerContext.getId().intern()) {
                    if (SharedParamManager.lockSharedKeys(callerContext.getId(), gatewayId)) {
                        long syncingStartTime = System.currentTimeMillis();
                        syncThrottleWindowParams(callerContext, true);
                        syncThrottleCounterParams(callerContext, false, requestContext);
                        SharedParamManager.releaseSharedKeys(callerContext.getId());
                        long timeNow = System.currentTimeMillis();
                        if (log.isDebugEnabled()) {
                            log.debug("timeNow : " + timeNow
                                    + ". Evaluating whether can access based on unit time. Lock released in " + (timeNow
                                    - syncingStartTime) + " ms for callerContext " + callerContext.getId());
                        }
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Current time:" + System.currentTimeMillis()
                                    + "Evaluating whether can access based on unit time."
                                    + "  Failed to lock shared keys, hence skipped " + "syncing tasks. key =  "
                                    + callerContext.getId());
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "Sync mode started: request time = " + requestContext.getRequestTime() + ", firstAccessTime = "
                                + callerContext.getFirstAccessTime() + ", next time window = "
                                + callerContext.getNextTimeWindow() + ", nextAccessTime = "
                                + callerContext.getNextAccessTime() + ", localHits = " + callerContext.getLocalHits()
                                + ", localQuota = " + callerContext.getLocalQuota());
            }

            syncModeNotifiedMap.put(callerContext.getId(), String.valueOf(callerContext.getNextTimeWindow()));
            try (Jedis jedis = redisPool.getResource()) {
                if (log.isTraceEnabled()) {
                    log.trace("Publishing message to channel. message: " + message);
                }
                jedis.publish(WSO2_SYNC_MODE_INIT_CHANNEL, message);
            }
        }
        return canAccess;
    }

    /**
     * Evaluate whether the request is allowed or not when the unit time window is not over
     *
     * @return boolean value indicating whether the request can be accessed or not
     */
    @Override
    public boolean canAccessIfUnitTimeNotOver(CallerContext callerContext, CallerConfiguration configuration,
            ThrottleContext throttleContext, RequestContext requestContext) {
        if (log.isTraceEnabled()) {
            log.trace("Starting evaluating whether can access if unit time is not over. ");
        }
        boolean canAccess = false;
        int maxRequest = configuration.getMaximumRequestPerUnitTime();
        boolean localCounterResettingDone = true;
        if (maxRequest != 0) {
            if (callerContext.isThrottleParamSyncingModeSync()) {
                if (log.isTraceEnabled()) {
                    log.trace("Going to run throttle param syncing in sync mode");
                }
                synchronized (callerContext.getId().intern()) {
                    if (SharedParamManager.lockSharedKeys(callerContext.getId(), gatewayId)) {
                        long syncingStartTime = System.currentTimeMillis();
                        syncThrottleWindowParams(callerContext, true);
                        // add piled items and new request item to shared-counter (increments before allowing the request)
                        syncThrottleCounterParams(callerContext, true, requestContext);
                        SharedParamManager.releaseSharedKeys(callerContext.getId());
                        long timeNow = System.currentTimeMillis();
                        if (log.isDebugEnabled()) {
                            log.debug("Current time:" + timeNow
                                    + "Evaluating whether can access if unit time is not over. Lock released in " + (
                                    System.currentTimeMillis() - syncingStartTime) + " ms for callerContext: "
                                    + callerContext.getId());
                        }
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Current time : " + System.currentTimeMillis()
                                    + " Evaluating whether can access if unit time is not over. Failed to lock shared keys, hence skipped syncing tasks. key="
                                    + callerContext.getId());
                            callerContext.incrementLocalCounter(); // increment local counter since, sync tasks didn't run
                            // where incrementing should have happened (https://github.com/wso2/api-manager/issues/1982#issuecomment-1624920455)
                        }
                    }
                }
            } else { //async mode
                if (log.isTraceEnabled()) {
                    log.trace(
                            "Evaluating whether can access if unit time is not over. Serving api calls in async mode");
                }
                callerContext.incrementLocalCounter();
                localCounterResettingDone = false;
            }

            if (log.isTraceEnabled()) {
                log.trace("CallerContext Checking access if unit time is not over and less than max count>> Access "
                        + "allowed=" + maxRequest + " available=" + (maxRequest - (
                        callerContext.getGlobalCounter() + callerContext.getLocalCounter() - 1)) + " key" + " = "
                        + callerContext.getId() + ", currentGlobalCount = " + callerContext.getGlobalCounter()
                        + ", requestTime = " + requestContext.getRequestTime() + ", nextTimeWindow = "
                        + callerContext.getNextTimeWindow() + ", currentLocalCount = " + callerContext.getLocalCounter()
                        + " tier = " + configuration.getID() + ", nextAccessTime = " + callerContext.getNextAccessTime()
                        + ", firstAccessTime = " + callerContext.getFirstAccessTime());
            }

            if (callerContext.getFirstAccessTime() >= callerContext.getNextAccessTime()) {
                callerContext.setNextAccessTime(0);
                if (log.isTraceEnabled()) {
                    log.trace("Evaluating whether can access if unit time is not over. nextAccessTime is set to 0");
                }
            }

            if (callerContext.getGlobalCounter()
                    <= maxRequest) {    //(If the globalCount is less than max request). // Very first requests to cluster hits into this block
                if (log.isTraceEnabled()) {
                    log.trace(
                            "If the globalCount is less than max request : (global count + local count) = "
                                    + (callerContext.getGlobalCounter() + callerContext.getLocalCounter()));
                    log.trace("Evaluating whether can access if unit time is not over." + "Values: allowed = "
                            + maxRequest + " available" + "=" + (maxRequest - (callerContext.getGlobalCounter()
                            + callerContext.getLocalCounter())) + ", key = " + callerContext.getId()
                            + ", currentGlobalCount = " + callerContext.getGlobalCounter() + ", requestTime = "
                            + requestContext.getRequestTime() + ", nextTimeWindow = "
                            + callerContext.getNextTimeWindow() + ", currentLocalCount = "
                            + callerContext.getLocalCounter() + ", tier = " + configuration.getID()
                            + ", nextAccessTime = " + callerContext.getNextAccessTime() + ", firstAccessTime = "
                            + callerContext.getFirstAccessTime());
                }
                canAccess = true; // can continue access
                if (log.isTraceEnabled()) {
                    log.trace("Evaluating whether can access if unit time is not over. localCount:"
                            + callerContext.getLocalCounter());
                }

                throttleContext.flushCallerContext(callerContext, callerContext.getId());
                // can complete access
            } else { // if  count has exceeded max request count : set the nextAccessTime
                // if first exceeding request  (nextAccessTime = 0)
                // if caller has not already prohibit (nextAccessTime == 0)");
                // and if there is no prohibit time  period in configuration
                if (callerContext.getNextAccessTime() == 0) {
                    long prohibitTime = configuration.getProhibitTimePeriod();
                    if (log.isTraceEnabled()) {
                        log.trace(
                                "Evaluating whether can access if unit time is not over. prohibitTime:" + prohibitTime);
                    }
                    if (prohibitTime == 0) {
                        //prohibit access until unit time period is over
                        callerContext.setNextAccessTime(
                                callerContext.getFirstAccessTime() + configuration.getUnitTime());
                    } else {
                        //if there is a prohibit time period in configuration ,then set it as prohibit period
                        callerContext.setNextAccessTime(requestContext.getRequestTime() + prohibitTime);
                    }
                    if (log.isTraceEnabled()) {
                        String type = ThrottleConstants.IP_BASE == configuration.getType() ? "IP address" : "domain";
                        log.trace("Maximum Number of requests are reached for caller with " + type + " - "
                                + callerContext.getId());
                    }
                    // Send the current state to others (clustered env)
                    throttleContext.flushCallerContext(callerContext, callerContext.getId());

                    // No need to process/sync throttle params in sync mode from now onwards, as the requests will not be allowed anyhow
                    callerContext.setIsThrottleParamSyncingModeSync(false);
                    syncModeNotifiedMap.remove(callerContext.getId());
                    if (log.isTraceEnabled()) {
                        log.trace("mode set back to async since request count has exceeded max limit");
                    }
                } else { // second to onwards exceeding requests : conditions based on prohibit time period comes into
                    // action here onwards since 1st exceeding request had set the prohibit period if there is any
                    // if the caller has already prohibit and prohibit time period has already over
                    if (callerContext.getNextAccessTime() <= requestContext.getRequestTime()) {
                        if (log.isTraceEnabled()) {
                            log.trace(
                                    "CallerContext Checking access if unit time is not over before time window exceed: "
                                            + "Access allowed = " + maxRequest + ", available = " + (maxRequest - (
                                            callerContext.getGlobalCounter() + callerContext.getLocalCounter()))
                                            + " key=" + callerContext.getId() + ", currentGlobalCount = "
                                            + callerContext.getGlobalCounter() + ", currentTime = "
                                            + requestContext.getRequestTime() + ", nextTimeWindow = "
                                            + callerContext.getNextTimeWindow() + " currentLocalCount = "
                                            + callerContext.getLocalCounter() + ", Tier = " + configuration.getID()
                                            + ", nextAccessTime = " + callerContext.getNextAccessTime());
                        }
                        // remove previous caller context
                        if (callerContext.getNextTimeWindow() != 0) {
                            throttleContext.removeCallerContext(callerContext.getId());
                        }
                        // reset the states so that, this is the first access
                        if (log.isTraceEnabled()) {
                            log.trace(
                                    "Evaluating whether can access if unit time is not over. nextAccessTime is set to 0");
                        }

                        callerContext.setNextAccessTime(0);
                        canAccess = true;

                        callerContext.setIsThrottleParamSyncingModeSync(false); // as this is the first access
                        syncModeNotifiedMap.remove(callerContext.getId());

                        callerContext.setGlobalCounter(0);// can access the system and this is same as first access
                        callerContext.setLocalCounter(1);
                        callerContext.setLocalHits(0);
                        callerContext.setFirstAccessTime(requestContext.getRequestTime());
                        callerContext.setNextTimeWindow(requestContext.getRequestTime() + configuration.getUnitTime());
                        if (log.isTraceEnabled()) {
                            log.trace("Evaluating whether can access if unit time is not over: globalCount = "
                                    + callerContext.getGlobalCounter() + " , localCount = "
                                    + callerContext.getLocalCounter() + ", firstAccessTime = "
                                    + callerContext.getFirstAccessTime() + " , nextTimeWindow = "
                                    + callerContext.getNextTimeWindow());
                        }

                        throttleContext.addAndFlushCallerContext(callerContext, callerContext.getId());

                        if (log.isTraceEnabled()) {
                            log.trace("Caller=" + callerContext.getId()
                                    + " has reset counters and added for replication when unit time is not over");
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            String type =
                                    ThrottleConstants.IP_BASE == configuration.getType() ? "IP address" : "domain";
                            log.trace(
                                    "There is no prohibit period or the prohibit period is not yet over for caller with "
                                            + type + " - " + callerContext.getId());
                        }
                    }
                }
            }
            // if throttle param processing was async and if the request was not allowed, then need to reset the local counter and hits
            if (!localCounterResettingDone && canAccess == false) {
                callerContext.resetLocalCounter(); //
                callerContext.setLocalHits(0);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(" request time = " + requestContext.getRequestTime() + " : " +
                    requestContext.getRequestTime()
                    + ". Evaluating whether can access if unit time is not over.  DECISION MADE. CAN ACCESS: " + canAccess
                    + ", firstAccessTime = " + callerContext.getFirstAccessTime() + ", next time window = "
                    + callerContext.getNextTimeWindow()  + ", nextAccessTime = "
                    + callerContext.getNextAccessTime()  + ", localHits = " + callerContext.getLocalHits()
                    + ", globalHits = " + callerContext.getGlobalCounter());
        }

        return canAccess;
    }

    /**
     * Evaluate whether the request is allowed or not wheN the unit time window is over
     *
     * @return boolean value indicating whether the request can be accessed or not
     */
    @Override
    public boolean canAccessIfUnitTimeOver(CallerContext callerContext, CallerConfiguration configuration,
            ThrottleContext throttleContext, RequestContext requestContext) {
        if (log.isTraceEnabled()) {
            log.trace("Evaluating whether can access if unit time is over. ");
        }
        boolean canAccess = false;
        // if number of access for a unit time is less than MAX and if the unit time period (session time) has just over
        int maxRequest = configuration.getMaximumRequestPerUnitTime();
        if (log.isTraceEnabled()) {
            log.trace("Evaluating whether can access if unit time is over. globalCount = "
                    + callerContext.getGlobalCounter() + " , localCount = " + callerContext.getLocalCounter()
                    + ", firstAccessTime = " + callerContext.getFirstAccessTime() + ", nextTimeWindow = "
                    + callerContext.getNextTimeWindow() + ", localHits = " + callerContext.getLocalHits()
                    + ", isThrottleParamSyncingModeSync = " + callerContext.isThrottleParamSyncingModeSync()
                    + ", nextAccessTime = " + callerContext.getNextAccessTime());
        }

        if (callerContext.isThrottleParamSyncingModeSync()) {
            if (log.isTraceEnabled()) {
                log.trace("Going to run throttle param syncing");
            }
            synchronized (callerContext.getId().intern()) {
                if (SharedParamManager.lockSharedKeys(callerContext.getId(), gatewayId)) {
                    long syncingStartTime = System.currentTimeMillis();
                    syncThrottleWindowParams(callerContext, true);
                    // add piled items and new request item to shared-counter (increments before allowing the request)
                    syncThrottleCounterParams(callerContext, true, requestContext);
                    SharedParamManager.releaseSharedKeys(callerContext.getId());
                    long timeNow = System.currentTimeMillis();

                    if (log.isDebugEnabled()) {
                        log.debug("current time:" + timeNow
                                + "Evaluating whether can access if unit time is over. Lock released in " + (timeNow
                                - syncingStartTime) + " ms for callerContext " + callerContext.getId());
                    }

                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("current time:" + System.currentTimeMillis()
                                + " Evaluating whether can access if unit time is over. Failed to lock shared keys, "
                                + "hence skipped syncing tasks. key = " + callerContext.getId());
                    }
                    // increment local counter since, sync tasks didn't run where incrementing should have happened
                    // (https://github.com/wso2/api-manager/issues/1982#issuecomment-1624920455)
                    callerContext.incrementLocalCounter();
                }
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Evaluating whether can access if unit time is over.  Serving api calls in async mode");
            }
        }
        if (maxRequest != 0) {
            // first req, after exceeding previous window if, in previous window the max limit was not exceeded
            if ((callerContext.getGlobalCounter() + callerContext.getLocalCounter()) < maxRequest) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "Evaluating whether can access if unit time is over. First req, after exceeding previous window if, in previous "
                                    + "window the max limit was not exceeded");
                }
                if (callerContext.getNextTimeWindow() != 0) {
                    if (log.isTraceEnabled()) {
                        log.trace("Evaluating whether can access if unit time is over. if NextTimeWindow != 0");
                    }
                    // Removes and sends the current state to others  (clustered env)
                    //remove previous callercontext instance
                    throttleContext.removeCallerContext(callerContext.getId());
                    callerContext.setGlobalCounter(0);// can access the system   and this is same as first access
                    callerContext.setLocalCounter(1);
                    callerContext.setLocalHits(0);
                    callerContext.setFirstAccessTime(requestContext.getRequestTime());
                    callerContext.setNextTimeWindow(requestContext.getRequestTime() + configuration.getUnitTime());
                    throttleContext.addAndFlushCallerContext(callerContext, callerContext.getId());
                    if (log.isTraceEnabled()) {
                        log.trace("Evaluating whether can access if unit time is over:  globalCount = "
                                + callerContext.getGlobalCounter() + " , localCount = "
                                + callerContext.getLocalCounter() + ", firstAccessTime = "
                                + callerContext.getFirstAccessTime() + " , nextTimeWindow = "
                                + callerContext.getNextTimeWindow());
                    }
                }
                if (log.isTraceEnabled()) {
                    log.trace("CallerContext Checking access if unit time over next time window: Access allowed = "
                            + maxRequest + ", available = " + (maxRequest - (callerContext.getGlobalCounter()
                            + callerContext.getLocalCounter())) + ", key = " + callerContext.getId()
                            + ", currentGlobalCount = " + callerContext.getGlobalCounter() + ", requestTime = "
                            + requestContext.getRequestTime() + ", nextTimeWindow = "
                            + callerContext.getNextTimeWindow() + ", currentLocalCount = "
                            + callerContext.getLocalCounter() + " Tier=" + configuration.getID() + ", nextAccessTime = "
                            + callerContext.getNextAccessTime());
                }
                if (callerContext.getGlobalCounter() <= maxRequest) {
                    canAccess = true;
                }
                //next time callers can access as a new one
            } else { // if in previous window, the max limit was exceeded
                // if caller in prohibit session  and prohibit period has just over
                if ((callerContext.getNextAccessTime() == 0) || (callerContext.getNextAccessTime()
                        <= requestContext.getRequestTime())) {
                    if (log.isTraceEnabled()) {
                        log.trace("CallerContext Checking access if unit time over>> Access allowed=" + maxRequest
                                + " available = " + (maxRequest - (callerContext.getGlobalCounter()
                                + callerContext.getLocalCounter())) + " key=" + callerContext.getId()
                                + " currentGlobalCount = " + callerContext.getGlobalCounter() + " currentTime = "
                                + requestContext.getRequestTime() + " nextTimeWindow = "
                                + callerContext.getNextTimeWindow() + " currentLocalCount = "
                                + callerContext.getLocalCounter() + " Tier=" + configuration.getID()
                                + " nextAccessTime = " + callerContext.getNextAccessTime());
                    }

                    //remove previous callerContext instance
                    if (callerContext.getNextTimeWindow() != 0) {
                        throttleContext.removeCallerContext(callerContext.getId());
                    }
                    // reset the states so that, this is the first access
                    callerContext.setNextAccessTime(0);
                    canAccess = true;
                    callerContext.setLocalHits(0);
                    callerContext.setGlobalCounter(0);// can access the system and this is same as first access
                    callerContext.setLocalCounter(1);
                    callerContext.setFirstAccessTime(requestContext.getRequestTime());

                    callerContext.setNextTimeWindow(requestContext.getRequestTime() + configuration.getUnitTime());
                    // registers caller and send the current state to others (clustered env)
                    throttleContext.addAndFlushCallerContext(callerContext, callerContext.getId());
                    if (log.isTraceEnabled()) {
                        log.trace("Evaluating whether can access if unit time is over.  globalCount = "
                                + callerContext.getGlobalCounter() + " , localCount = "
                                + callerContext.getLocalCounter() + ", firstAccessTime = "
                                + callerContext.getFirstAccessTime() + " , nextTimeWindow = "
                                + callerContext.getNextTimeWindow());
                        log.trace("Caller = " + callerContext.getId()
                                + " has reset counters and added for replication when unit time is over");
                    }

                    if (callerContext.getGlobalCounter() <= maxRequest) {
                        canAccess = true;
                    }
                } else {
                    // if caller in prohibit session  and prohibit period has not  over
                    if (log.isTraceEnabled()) {
                        String type = ThrottleConstants.IP_BASE == configuration.getType() ? "IP address" : "domain";
                        log.trace("Even unit time has over , CallerContext in prohibit state :" + type + " - "
                                + callerContext.getId());
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Evaluating whether can access if unit time is over. DECISION MADE. CAN ACCESS: " + canAccess
                    + ", request time = " + requestContext.getRequestTime() + ", firstAccessTime = "
                    + callerContext.getFirstAccessTime() + ",  nextTimeWindow " + callerContext.getNextTimeWindow()
                    + ", nextAccessTime = " + callerContext.getNextAccessTime() + ", localHits = "
                    + callerContext.getLocalHits() + ", globalHits = " + callerContext.getGlobalCounter());
        }

        return canAccess;
    }

    /**
     * Set the throttle param sync mode for the callerContext
     */
    private void setThrottleParamSyncMode(CallerContext callerContext, RequestContext requestContext) {
        if (log.isTraceEnabled()) {
            log.trace("Setting ThrottleParam Sync Mode for callerContext." + callerContext.getId()
                    + ". syncModeNotifiedMap:" + syncModeNotifiedMap.entrySet());
        }
        if (callerContext.isThrottleParamSyncingModeSync()) {
            if (log.isTraceEnabled()) {
                log.trace("ThrottleParamSyncingModeSync is already true for callerContext: " + callerContext.getId());
            }
            /* previous time window is exceeded and this is the first request in new window
            normally SyncModeLastUpdatedTime is less than NextTimeWindow. If so we need to check if this nextTimeWindow
            is an old one too. (previous window is passed now) */
            if (requestContext.getRequestTime() > callerContext.getNextTimeWindow()) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "currentTime has exceeded NextTimeWindow. So setting it to false. So setting it to false.");
                }
                callerContext.setIsThrottleParamSyncingModeSync(false);
            }
        } else {
            // if a sync mode switching msg has been received or own node exceeded local quota
            if (syncModeNotifiedMap.containsKey(callerContext.getId())) {
                long nextTimeWindowOfSyncMessage = Long.parseLong(syncModeNotifiedMap.get(callerContext.getId()));
                // still within the time window that the sync message was sent by some other GW node or mode switched by own node
                if (nextTimeWindowOfSyncMessage >= requestContext.getRequestTime()) {
                    callerContext.setIsThrottleParamSyncingModeSync(true);
                    if (log.isTraceEnabled()) {
                        log.trace(
                                "Set ThrottleParamSyncingModeSync to true for callerContext: " + callerContext.getId());
                    }
                }
            }
        }
    }

    /**
     * Syncs the throttle window parameters
     *
     * @param isInvocationFlow Whether the flow is just a syncing flow which doesn't increase request counters
     */
    @Override
    public void syncThrottleCounterParams(CallerContext callerContext, boolean isInvocationFlow,
            RequestContext requestContext) {
        if (log.isTraceEnabled()) {
            log.trace("When running syncing throttle counter params: isInvocationFlow = " + isInvocationFlow);
        }
        synchronized (callerContext.getId().intern()) {
            long syncingStartTime = System.currentTimeMillis();
            if (log.isTraceEnabled()) {
                log.trace("When running syncing throttle counter params: next time window = "
                        + callerContext.getNextTimeWindow());
            }

            if (callerContext.getNextTimeWindow() > requestContext.getRequestTime()) {
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle counter params: Running counter sync task");
                }
                String id = callerContext.getId();
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle counter params: Initial Local counter = "
                            + callerContext.getLocalCounter() + " , globalCounter = " + callerContext.getGlobalCounter()
                            + ", distributedCounter = " + SharedParamManager.getDistributedCounter(id));
                }

                if (isInvocationFlow) {
                    callerContext.incrementLocalCounter(); // increment local counter to consider current request
                }
                long localCounter = callerContext.getLocalCounter();
                if (log.isTraceEnabled()) {
                    log.trace(
                            "When running syncing throttle counter params: localCounter increased to " + localCounter);
                }

                callerContext.resetLocalCounter();
                Long distributedCounter = SharedParamManager.addAndGetDistributedCounter(id, localCounter);

                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle counter params: Finally distributedCounter = "
                            + distributedCounter);
                }

                //Update instance's global counter value with distributed counter
                long oldGlobalCounter = callerContext.getGlobalCounter();
                callerContext.setGlobalCounter(distributedCounter);
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle counter params: Finally globalCounter increased from "
                            + oldGlobalCounter + " to " + callerContext.getGlobalCounter());
                    log.trace("When running syncing throttle counter params: finally local counter reset to 0");
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Throttle Counter Sync task skipped");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Latency for running syncing throttle counter params: " + (System.currentTimeMillis()
                        - syncingStartTime) + " ms for callerContext " + callerContext.getId());
            }
        }
    }

    /**
     * Syncs the throttle window related parameters
     */
    @Override
    public void syncThrottleWindowParams(CallerContext callerContext, boolean isInvocationFlow) {
        synchronized (callerContext.getId().intern()) {
            long syncingStartTime = System.currentTimeMillis();
            if (log.isTraceEnabled()) {
                log.trace("When running syncing throttle window params: isInvocationFlow = " + isInvocationFlow);
            }

            String callerId = callerContext.getId();
            long sharedTimestamp = SharedParamManager.getSharedTimestamp(
                    callerContext.getId());  // this will be set 0 if the redis key-value pair is not available
            if (log.isTraceEnabled()) {
                log.trace("Got sharedTimestamp from redis. sharedTimestamp = " + sharedTimestamp);
            }
            long sharedNextWindow = sharedTimestamp + callerContext.getUnitTime();
            long localFirstAccessTime = callerContext.getFirstAccessTime();

            if (log.isTraceEnabled()) {
                log.trace("Initial: sharedTimestamp = " + sharedTimestamp + ", sharedNextWindow = " + sharedNextWindow
                        + ", localFirstAccessTime = " + localFirstAccessTime + ", unit time = "
                        + callerContext.getUnitTime());
            }

            long distributedCounter = SharedParamManager.getDistributedCounter(callerId);
            if (log.isTraceEnabled()) {
                log.trace("When running syncing throttle window params : distributedCounter = " + distributedCounter
                        + ", localCounter = " + callerContext.getLocalCounter() + ", globalCounter = "
                        + callerContext.getGlobalCounter() + ", distributedCounter = " + distributedCounter
                        + ", localHits = " + callerContext.getLocalHits());
            }
            // If this is a new time window. If a sync msg is received from another node, this will be true
            if (localFirstAccessTime < sharedTimestamp) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "When running syncing throttle window params: this is a new time window and a sync msg is received from "
                                    + "another node");
                }
                callerContext.setFirstAccessTime(sharedTimestamp);
                callerContext.setNextTimeWindow(sharedNextWindow);
                callerContext.setGlobalCounter(distributedCounter);
                if (!isInvocationFlow) {
                    // if localCounter was set 0 here, that premature throttling won't happen. But can't set 0 here too
                    // since then already received request that should be counted will be lost.
                    callerContext.setLocalHits(0);
                }
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle window params: Setting time windows of caller context "
                            + callerId + " when window already set at another GW");
                }
            /* If some request comes to a nodes after some node set the shared timestamp then this check whether the
            first access time of local is in between the global time window if so this will set local caller context
            time window to global */
            } else if (localFirstAccessTime == sharedTimestamp) {
                // if this node itself set the shared timestamp or if another node-sent sync msg had triggered setting
                // sharedTimestamp and sharedTimestamp from that other node
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle window params: localFirstAccessTime == sharedTimestamp");
                }
                callerContext.setGlobalCounter(distributedCounter);
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle window params: globalCounter = "
                            + callerContext.getGlobalCounter());
                }
            } else if (localFirstAccessTime > sharedTimestamp && localFirstAccessTime < sharedNextWindow) {
                // if another node had set the shared timestamp, earlier
                callerContext.setFirstAccessTime(sharedTimestamp);
                callerContext.setNextTimeWindow(sharedNextWindow);
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle window params: distributedCounter = " + distributedCounter);
                }
                callerContext.setGlobalCounter(distributedCounter);
                if (log.isTraceEnabled()) {
                    log.trace("When running syncing throttle window params: Global Counter = "
                            + callerContext.getGlobalCounter());
                }
            /* If above conditions are not met, this is the place where node set new window if
             global first access time is 0, then it will be the beginning of the throttle time
             window so present node will set shared timestamp and the distributed counter. Also, if time
             window expired this will be the node who set the next time window starting time */
            } else {
                /* In the flow this is the first time that reaches throttleWindowParamSync method. And then at
                 canAccessIfUnitTimeOver flow, the first call after the sharedTimestamp is removed from redis. */
                if (log.isTraceEnabled()) {
                    log.trace("Setting Shared Timestamp");
                }
                SharedParamManager.setSharedTimestampWithExpiry(callerId, localFirstAccessTime,
                        callerContext.getUnitTime() + localFirstAccessTime);
                if (log.isTraceEnabled()) {
                    log.trace("Setting Distributed Counter With Expiry");
                }
                SharedParamManager.setDistributedCounterWithExpiry(callerId, 0,
                        callerContext.getUnitTime() + localFirstAccessTime);

                if (log.isTraceEnabled()) {
                    log.trace("Finished setting distributed counter. Set value 0. ");
                    log.trace("When running syncing throttle window params: Completed resetting time window of "
                            + callerId);
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("When running syncing throttle window params :" + SharedParamManager.getSharedTimestamp(
                        callerId) + ", sharedNextWindow = " + sharedNextWindow + ", localFirstAccessTime = "
                        + localFirstAccessTime);
            }
            if (log.isDebugEnabled()) {
                log.debug("Latency for running syncing throttle window params: " + (System.currentTimeMillis()
                        - syncingStartTime) + " ms for callerContext " + callerContext.getId());
            }
        }
    }

    /**
     * Calculate and set the local quota to the caller context. This is done for each request since the gateway count
     * can be changed dynamically.
     */
    public void setLocalQuota(CallerContext callerContext, CallerConfiguration configuration) {
        long maxRequests = configuration.getMaximumRequestPerUnitTime();
        int gatewayCount = ServiceReferenceHolder.getInstance().getGatewayCount();

        RedisConfig redisConfig = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getRedisConfig();
        if (gatewayCount < redisConfig.getMinGatewayCount()) {
            gatewayCount = redisConfig.getMinGatewayCount();
            if (log.isTraceEnabled()) {
                log.trace("Set gateway count to " + gatewayCount + " as the calculated gateway count is less than the"
                        + " min_gateway_count configuration");
            }
        }
        short localQuotaBufferPercentage = Short.parseShort(
                ThrottleServiceDataHolder.getInstance().getThrottleProperties().getLocalQuotaBufferPercentage());
        long localQuota = (maxRequests - maxRequests * localQuotaBufferPercentage / 100) / gatewayCount;
        if (log.isTraceEnabled()) {
            log.trace("Set local quota to " + localQuota + " for " + callerContext.getId() + " in hybrid throttling");
        }
        callerContext.setLocalQuota(localQuota);
    }

    @Override
    public String getType() {
        return "hybrid";
    }

    @Override
    public boolean isEnable() {
        return true;
    }
}
