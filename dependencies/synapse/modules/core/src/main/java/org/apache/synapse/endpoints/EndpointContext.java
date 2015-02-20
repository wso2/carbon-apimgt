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
package org.apache.synapse.endpoints;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.Replicator;

import java.util.Calendar;
import java.util.Date;

/**
 * This class is one of the key classes of the Endpoint management in Synapse. It maintains the
 * runtime state of an endpoint for local and clustered endpoint configurations.
 */
public class EndpointContext {

    private static final Log log = LogFactory.getLog(EndpointContext.class);

    private static final String KEY_PREFIX = "synapse.endpoint.";
    private static final String STATE = ".state";
    private static final String NEXT_RETRY_TIME = ".next_retry_time";
    private static final String REMAINING_RETRIES = ".remaining_retries";
    private static final String LAST_SUSPEND_DURATION = ".last_suspend_duration";

    // The different states an endpoint could exist at any point in time
    /** And active endpoint known to be functioning properly */
    public static final int ST_ACTIVE      = 1;
    /** An endpoint which timed out - but now maybe ready to retry depending on the current time */
    public static final int ST_TIMEOUT     = 2;
    /**
     * An endpoint put into the suspended state by the system.
     * Will retry after an applicable delay
     */
    public static final int ST_SUSPENDED   = 3;
    /**
     * An endpoint manually switched off into maintenance -
     * it will never change state automatically
     */
    public static final int ST_OFF = 4;

    /** The state of the endpoint at present */
    private int  localState = ST_ACTIVE;
    /** The time in ms, until the next retry - depending on a timeout or suspension */
    private long localNextRetryTime = -1;
    /** The number of attempts left for timeout failures, until they make the endpoint suspended */
    private int  localRemainingRetries = -1;
    /** The duration in ms for the last suspension */
    private long localLastSuspendDuration = -1;

    /** Is the environment clustered ? */
    private boolean isClustered = false;
    /** Name of the endpoint - mainly for logging */
    private String endpointName = SynapseConstants.ANONYMOUS_ENDPOINT;
    /** The Axis2 configuration context - to replicate state in a cluster */
    private ConfigurationContext cfgCtx = null;
    /** The endpoint definition that holds static endpoint information */
    private EndpointDefinition definition = null;

    /** Metrics bean to notify the state changes */
    private EndpointView metricsBean = null;

    // for clustered mode operation, keys pre-computed and used for replication
    private final String STATE_KEY;
    private final String NEXT_RETRY_TIME_KEY;
    private final String REMAINING_RETRIES_KEY;
    private final String LAST_SUSPEND_DURATION_KEY;

    /**
     * Create an EndpointContext to hold runtime state of an Endpoint
     * @param endpointName the name of the endpoint (mainly for logging)
     * @param endpointDefinition the definition of the endpoint
     *  (e.g. retry time, suspend duration..)
     * @param clustered is the environment clustered?
     * @param cfgCtx the Axis2 configurationContext for clustering
     */
    public EndpointContext(String endpointName, EndpointDefinition endpointDefinition,
                           boolean clustered, ConfigurationContext cfgCtx, EndpointView metricsBean) {

        if (clustered) {
            if (endpointName == null) {
                if (endpointDefinition != null &&
                    !endpointDefinition.isReplicationDisabled()) {
                    handleException("For proper clustered mode operation, all endpoints should " +
                        "be uniquely named");
                }
            }
            this.isClustered = true;
            this.cfgCtx = cfgCtx;
        }

        this.definition = endpointDefinition;
        if (endpointName != null) {
            this.endpointName = endpointName;
        } else if (endpointDefinition != null) {
            this.endpointName = endpointDefinition.toString();
        }

        this.metricsBean = metricsBean;

        STATE_KEY = KEY_PREFIX + endpointName + STATE;
        NEXT_RETRY_TIME_KEY = KEY_PREFIX + endpointName + NEXT_RETRY_TIME;
        REMAINING_RETRIES_KEY = KEY_PREFIX + endpointName + REMAINING_RETRIES;
        LAST_SUSPEND_DURATION_KEY = KEY_PREFIX + endpointName + LAST_SUSPEND_DURATION;

        if (isClustered && (endpointDefinition == null ||
                !endpointDefinition.isReplicationDisabled())) {
            //In a clustered environment, we need to set the state of an Endpoint when it is created.
            cfgCtx.setNonReplicableProperty(STATE_KEY, ST_ACTIVE);
        }
    }

    private void recordStatistics(int state) {
        if (metricsBean == null) {
            return;
        }

        switch (state) {
            case ST_ACTIVE:
                metricsBean.resetConsecutiveSuspensions();
                metricsBean.resetConsecutiveTimeouts();
                metricsBean.setSuspendedAt(null);
                metricsBean.setTimedoutAt(null);
                break;

            case ST_TIMEOUT:
                metricsBean.resetConsecutiveSuspensions();
                metricsBean.incrementTimeouts();
                if (localState != ST_TIMEOUT) {
                    metricsBean.setTimedoutAt(Calendar.getInstance().getTime());
                    metricsBean.setSuspendedAt(null);
                }
                break;

            case ST_SUSPENDED:
                metricsBean.resetConsecutiveTimeouts();
                metricsBean.incrementSuspensions();
                if (localState != ST_SUSPENDED) {
                    metricsBean.setSuspendedAt(Calendar.getInstance().getTime());
                    metricsBean.setTimedoutAt(null);
                }
                break;
        }
    }

    /**
     * Update the internal state of the endpoint
     *
     * @param state the new state of the endpoint
     */
    private void setState(int state) {

        recordStatistics(state);

        if (isClustered) {
            Replicator.setAndReplicateState(STATE_KEY, state, cfgCtx);
            if (definition == null) return;
            switch (state) {
                case ST_ACTIVE: {
                    Replicator.setAndReplicateState(REMAINING_RETRIES_KEY,
                            definition.getRetriesOnTimeoutBeforeSuspend(), cfgCtx);
                    Replicator.setAndReplicateState(LAST_SUSPEND_DURATION_KEY, null, cfgCtx);
                    break;
                }
                case ST_TIMEOUT: {
                    Integer retries
                            = (Integer) cfgCtx.getPropertyNonReplicable(REMAINING_RETRIES_KEY);
                    if (retries == null) {
                        retries = definition.getRetriesOnTimeoutBeforeSuspend();
                    }

                    if (retries <= 0) {
                        log.info("Endpoint : " + endpointName + " has been marked for SUSPENSION," +
                                " but no further retries remain. Thus it will be SUSPENDED.");

                        setState(ST_SUSPENDED);

                    } else {
                        Replicator.setAndReplicateState(
                                REMAINING_RETRIES_KEY, (retries - 1), cfgCtx);
                        long nextRetry = System.currentTimeMillis()
                                + definition.getRetryDurationOnTimeout();
                        Replicator.setAndReplicateState(NEXT_RETRY_TIME_KEY, nextRetry, cfgCtx);

                        log.warn("Endpoint : " + endpointName + " is marked as TIMEOUT and " +
                                "will be retried : " + (retries - 1) + " more time/s after : " +
                                new Date(nextRetry) + " until its marked SUSPENDED for failure");
                    }
                    break;
                }
                case ST_SUSPENDED: {
                    computeNextRetryTimeForSuspended();
                    break;
                }
                case ST_OFF: {
                    // mark as in maintenence, and reset all other information
                    Replicator.setAndReplicateState(REMAINING_RETRIES_KEY,
                            definition == null ? -1 :
                                    definition.getRetriesOnTimeoutBeforeSuspend(), cfgCtx);
                    Replicator.setAndReplicateState(LAST_SUSPEND_DURATION_KEY, null, cfgCtx);
                    break;
                }
            }

        } else {

            localState = state;
            if (definition == null) return;
            switch (state) {
                case ST_ACTIVE: {
                    localRemainingRetries = definition.getRetriesOnTimeoutBeforeSuspend();
                    localLastSuspendDuration = -1;
                    break;
                }
                case ST_TIMEOUT: {
                    int retries = localRemainingRetries;
                    if (retries == -1) {
                        retries = definition.getRetriesOnTimeoutBeforeSuspend();
                    }

                    if (retries <= 0) {
                        log.info("Endpoint : " + endpointName + " has been marked for SUSPENSION, "
                                + "but no further retries remain. Thus it will be SUSPENDED.");

                        setState(ST_SUSPENDED);

                    } else {
                        localRemainingRetries = retries - 1;
                        localNextRetryTime =
                                System.currentTimeMillis() + definition.getRetryDurationOnTimeout();

                        log.warn("Endpoint : " + endpointName + " is marked as TIMEOUT and " +
                                "will be retried : " + localRemainingRetries + " more time/s " +
                                "after : " + new Date(localNextRetryTime)
                                + " until its marked SUSPENDED for failure");
                    }
                    break;
                }
                case ST_SUSPENDED: {
                    computeNextRetryTimeForSuspended();
                    break;
                }
                case ST_OFF: {
                    // mark as in maintenence, and reset all other information
                    localRemainingRetries = definition == null ?
                            -1 : definition.getRetriesOnTimeoutBeforeSuspend();
                    localLastSuspendDuration = -1;
                    break;
                }
            }
        }
    }

    /**
     * Endpoint has processed a message successfully
     */
    public void onSuccess() {
        if (isClustered) {
            Integer state = (Integer) cfgCtx.getPropertyNonReplicable(STATE_KEY);

            if ((state != null) && ((state != ST_ACTIVE) && (state != ST_OFF))) {
                log.info("Endpoint : " + endpointName + " currently " + getStateAsString() +
                        " will now be marked active since it processed its last message");
                setState(ST_ACTIVE);
            }
        } else {
            if (localState != ST_ACTIVE && localState != ST_OFF) {
                log.info("Endpoint : " + endpointName + " currently " + getStateAsString() +
                        " will now be marked active since it processed its last message");
                setState(ST_ACTIVE);
            }
        }
    }

    /**
     * Endpoint failed processing a message
     */
    public void onFault() {
        log.warn("Endpoint : " + endpointName + " will be marked SUSPENDED as it failed");
        setState(ST_SUSPENDED);
    }

    /**
     * Endpoint timeout processing a message
     */
    public void onTimeout() {
        if (log.isDebugEnabled()) {
            log.debug("Endpoint : " + endpointName + " will be marked for " +
                    "SUSPENSION due to the occurrence of one of the configured errors");
        }
        setState(ST_TIMEOUT);
    }

    /**
     * Compute the suspension duration according to the geometric series parameters defined
     */
    private void computeNextRetryTimeForSuspended() {
        boolean notYetSuspended = true;
        long lastSuspendDuration = definition.getInitialSuspendDuration();
        if (isClustered) {
            Long lastDuration = (Long) cfgCtx.getPropertyNonReplicable(LAST_SUSPEND_DURATION_KEY);
            if (lastDuration != null) {
                lastSuspendDuration = lastDuration;
                notYetSuspended = false;
            }
        } else if (localLastSuspendDuration > 0) {
            lastSuspendDuration = localLastSuspendDuration;
            notYetSuspended = false;
        }

        long nextSuspendDuration = (notYetSuspended ?
                definition.getInitialSuspendDuration() :
                (long) (lastSuspendDuration * definition.getSuspendProgressionFactor()));

        if (nextSuspendDuration > definition.getSuspendMaximumDuration()) {
            nextSuspendDuration = definition.getSuspendMaximumDuration();
        } else if (nextSuspendDuration < 0) {
            nextSuspendDuration = SynapseConstants.DEFAULT_ENDPOINT_SUSPEND_TIME;
        }

        long nextRetryTime = System.currentTimeMillis() + nextSuspendDuration;

        if (isClustered) {
            Replicator.setAndReplicateState(LAST_SUSPEND_DURATION_KEY, nextSuspendDuration, cfgCtx);
            Replicator.setAndReplicateState(NEXT_RETRY_TIME_KEY, nextRetryTime, cfgCtx);
        } else {
            localLastSuspendDuration = nextSuspendDuration;
            localNextRetryTime = nextRetryTime;
        }

        log.warn("Suspending endpoint : " + endpointName +
                (notYetSuspended ? " -" :
                        " - last suspend duration was : " + lastSuspendDuration + "ms and") +
                " current suspend duration is : " + nextSuspendDuration + "ms - " +
                "Next retry after : " + new Date(nextRetryTime));
    }

    /**
     * Checks if the endpoint is in the state ST_ACTIVE. In a clustered environment, the non
     * availability of a clustered STATE_KEY implies that this endpoint is active
     *
     * @return Returns true if the endpoint should be considered as active
     */
    public boolean readyToSend() {

        if (log.isDebugEnabled()) {
            log.debug("Checking if endpoint : " + endpointName + " currently at state " +
                    getStateAsString() + " can be used now?");
        }

        if (isClustered) {

            // gets the value from configuration context (The shared state across all instances)
            Integer state = (Integer) cfgCtx.getPropertyNonReplicable(STATE_KEY);
            Integer remainingRetries
                    = (Integer) cfgCtx.getPropertyNonReplicable(REMAINING_RETRIES_KEY);
            Long nextRetryTime = (Long) cfgCtx.getPropertyNonReplicable(NEXT_RETRY_TIME_KEY);

            if (state == null) {
                // state has not yet been replicated..
                // first replication occurs on first timeout or fault
                return true;

            } else {
                if (state == ST_ACTIVE) {
                    return true;

                } else if (state == ST_OFF) {
                    return false;

                } else if (System.currentTimeMillis() >= nextRetryTime) {
                    // if we are not active, but reached the next retry time, return true but do not
                    // make a state change. We will make the state change on a successful send
                    // if we are in the ST_TIMEOUT state, reduce a remaining retry
                    if (state == ST_TIMEOUT) {
                        remainingRetries--;
                        Replicator.setAndReplicateState(
                                REMAINING_RETRIES_KEY, remainingRetries, cfgCtx);

                        if (log.isDebugEnabled()) {
                            log.debug("Endpoint : " + endpointName + " which is currently in " +
                                    "timeout state is ready to be retried. Remaining retries " +
                                    "before suspension : " + remainingRetries);
                        }

                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Endpoint : " + endpointName + " which is currently " +
                                    "SUSPENDED, is ready to be retried now");
                        }
                    }
                    return true;
                }
            }

        } else {

            if (localState == ST_ACTIVE) {
                return true;

            } else if (localState == ST_OFF) {
                return false;

            } else if (System.currentTimeMillis() >= localNextRetryTime) {

                // if we are not active, but reached the next retry time, return true but do not
                // make a state change. We will make the state change on a successful send
                // if we are in the ST_TIMEOUT state, reduce a remaining retry
                if (localState == ST_TIMEOUT) {

                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint : " + endpointName + " which is currently in timeout " +
                                "state is ready to be retried. Remaining retries before " +
                                "suspension : " + localRemainingRetries);
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint : " + endpointName + " which is currently SUSPENDED," +
                                " is ready to be retried now");
                    }
                }
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Endpoint : " + endpointName + " not ready and is currently : "
                    + getStateAsString() + ". Next retry will be after : "
                    + new Date(localNextRetryTime));
        }

        return false;
    }

    /**
     * Manually turn off this endpoint (e.g. for maintenence)
     */
    public void switchOff() {
        log.info("Manually switching off endpoint : " + endpointName);
        setState(ST_OFF);
    }

    /**
     * Activate this endpoint manually (i.e. from an automatic suspend or manual switch off)
     */
    public void switchOn() {
        log.info("Manually activating endpoint : " + endpointName);
        setState(ST_ACTIVE);
    }

    public boolean isState(int s) {
        if (isClustered) {
            Integer state = (Integer) cfgCtx.getPropertyNonReplicable(STATE_KEY);
            // state has not yet been replicated..
            // first replication occurs on first timeout or fault
            boolean isState = false;
            if (state == null) {
                if (s == ST_ACTIVE) {
                    isState = true;
                }
            } else {
                isState = state == s;
            }
            return isState;
        } else {
            return localState == s;
        }
    }


    /**
     * Private method to return the current state as a loggable string
     *
     * @return the current state as a string
     */
    private String getStateAsString() {
        Integer state = localState;
        if (isClustered) {
            state = (Integer) cfgCtx.getPropertyNonReplicable(STATE_KEY);
            if (state == null) {
                return "ACTIVE";
            }
        }
        switch (state) {
            case ST_ACTIVE : return "ACTIVE";
            case ST_TIMEOUT : return "TIMEOUT";
            case ST_SUSPENDED : return "SUSPENDED";
            case ST_OFF: return "MAINTNENCE";
            default: return "UNKNOWN";
        }
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     */
    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ Name : ").append(endpointName).
                append(" ] [ State : ").append(getStateAsString()).append(" ]");
        return sb.toString();
    }
}
