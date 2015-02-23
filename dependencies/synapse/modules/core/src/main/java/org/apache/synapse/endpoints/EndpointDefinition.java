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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfigurable;
import org.apache.synapse.aspects.AspectConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * An Endpoint definition contains the information about an endpoint. It is used by leaf
 * level endpoints to store this information (e.g. AddressEndpoint and WSDLEndpoint).
 */
public class EndpointDefinition implements AspectConfigurable {

    /** Who is the leaf level Endpoint which uses me? */
    private Endpoint leafEndpoint = null;
    /**
     * The simple address this endpoint resolves to - if explicitly specified
     */
    private String address = null;
    /**
     * Should messages be sent in an WS-RM Sequence ?
     */
    private boolean reliableMessagingOn = false;
    /**
     * Should messages be sent using WS-A?
     */
    private boolean addressingOn = false;
    /**
     * The addressing namespace version
     */
    private String addressingVersion = null;
    /**
     * Should messages be sent using WS-Security?
     */
    private boolean securityOn = false;
    /**
     * The "key" for any WS-RM Policy overrides to be used
     */
    private String wsRMPolicyKey = null;
    /**
     * The "key" for any Rampart Security Policy to be used
     */
    private String wsSecPolicyKey = null;
    /**
     * The "key" for any Rampart Security Policy to be used for inbound messages
     */
    private String inboundWsSecPolicyKey = null;
    /**
     * The "key" for any Rampart Security Policy to be used for outbound messages
     */
    private String outboundWsSecPolicyKey = null;
    /**
     * use a separate listener - implies addressing is on *
     */
    private boolean useSeparateListener = false;
    /**
     * force REST (POST) on *
     */
    private boolean forcePOX = false;
    /**
     * force REST (GET) on *
     */
    private boolean forceGET = false;
    /**
     * force SOAP11 on *
     */
    private boolean forceSOAP11 = false;
    /**
     * force SOAP11 on *
     */
    private boolean forceSOAP12 = false;
    /**
     * force REST on ?
     */
    private boolean forceREST = false;
    /**
     *  HTTP Endpoint
     */
    private boolean isHTTPEndpoint = false;
    /**
     * use MTOM *
     */
    private boolean useMTOM = false;
    /**
     * use SWA *
     */
    private boolean useSwa = false;
    /**
     * Endpoint message format. pox/soap11/soap12
     */
    private String format = null;

    /**
     * The charset encoding for messages sent to the endpoint.
     */
    private String charSetEncoding;

    /**
     * Whether endpoint state replication should be disabled or not (only valid in clustered setups)
     */
    private boolean replicationDisabled = false;

    /**
     * timeout duration for waiting for a response in ms. if the user has set some timeout action
     * and the timeout duration is not set, default is set to 0. note that if the user has
     * not set any timeout configuration, default timeout action is set to NONE, which won't do
     * anything for timeouts.
     */
    private long timeoutDuration = 0;

    /**
     * action to perform when a timeout occurs (NONE | DISCARD | DISCARD_AND_FAULT) *
     */
    private int timeoutAction = SynapseConstants.NONE;

    /** The initial suspend duration when an endpoint is marked inactive */
    private long initialSuspendDuration = -1;
    /** The suspend duration ratio for the next duration - this is the geometric series multipler */
    private float suspendProgressionFactor = 1;
    /** This is the maximum duration for which a node will be suspended */
    private long suspendMaximumDuration = Long.MAX_VALUE;
    /** A list of error codes, which directly puts an endpoint into suspend mode */
    private final List<Integer> suspendErrorCodes = new ArrayList<Integer>();

    /** No of retries to attempt on timeout, before an endpoint is makred inactive */
    private int retriesOnTimeoutBeforeSuspend = 0;
    /** The delay between retries for a timeout out endpoint */
    private int retryDurationOnTimeout = 0;
    /** A list of error codes which puts the endpoint into timeout mode */
    private final List<Integer> timeoutErrorCodes = new ArrayList<Integer>();

    private AspectConfiguration aspectConfiguration;
    /**
     * The variable that indicate tracing on or off for the current mediator
     */
    private int traceState = SynapseConstants.TRACING_UNSET;

    /** A list of error codes which permit the retries */
    private final List<Integer> retryDisabledErrorCodes = new ArrayList<Integer>();

    /** A list of error codes which permit the retries for Enabled error Codes */
    private final List<Integer> retryEnabledErrorCodes = new ArrayList<Integer>();
    /**
     * This should return the absolute EPR address referenced by the named endpoint. This may be
     * possibly computed.
     *
     * @return an absolute address to be used to reference the named endpoint
     */
    public String getAddress() {
        return address;
    }


    /**
     * This should return the absolute EPR address referenced by the named endpoint. This may be
     * possibly computed if the ${} properties specified in the URL.
     *
     * @param messageContext the current message context against the address is computed
     * @return an absolute address to be used to reference the named endpoint
     */
    public String getAddress(MessageContext messageContext) {
        if (address == null) {
            return null;
        }

        boolean matches = false;
        int s = 0;
        Pattern pattern = Pattern.compile("\\$\\{.*?\\}");

        StringBuffer computedAddress = new StringBuffer();

        Matcher matcher = pattern.matcher(address);
        while (matcher.find()) {


            Object property = messageContext.getProperty(
                    address.substring(matcher.start() + 2, matcher.end() - 1));
            if (property != null) {
                computedAddress.append(address.substring(s, matcher.start()));
                computedAddress.append(property.toString());
                s = matcher.end();
                matches = true;
            }
        }

        if (!matches) {
            return address;
        } else {
            computedAddress.append(address.substring(s, address.length()));
            return computedAddress.toString();
        }
    }

    /**
     * Set an absolute URL as the address for this named endpoint
     *
     * @param address the absolute address to be used
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Is RM turned on on this endpoint?
     *
     * @return true if on
     */
    public boolean isReliableMessagingOn() {
        return reliableMessagingOn;
    }

    /**
     * Request that RM be turned on/off on this endpoint
     *
     * @param reliableMessagingOn a boolean flag indicating RM is on or not
     */
    public void setReliableMessagingOn(boolean reliableMessagingOn) {
        this.reliableMessagingOn = reliableMessagingOn;
    }

    /**
     * Is WS-A turned on on this endpoint?
     *
     * @return true if on
     */
    public boolean isAddressingOn() {
        return addressingOn;
    }

    /**
     * Request that WS-A be turned on/off on this endpoint
     *
     * @param addressingOn  a boolean flag indicating addressing is on or not
     */
    public void setAddressingOn(boolean addressingOn) {
        this.addressingOn = addressingOn;
    }

    /**
     * Get the addressing namespace version
     *
     * @return the adressing version
     */
    public String getAddressingVersion() {
        return addressingVersion;
    }

    /**
     * Set the addressing namespace version
     *
     * @param addressingVersion Version of the addressing spec to use
     */
    public void setAddressingVersion(String addressingVersion) {
        this.addressingVersion = addressingVersion;
    }

    /**
     * Is WS-Security turned on on this endpoint?
     *
     * @return true if on
     */
    public boolean isSecurityOn() {
        return securityOn;
    }

    /**
     * Request that WS-Sec be turned on/off on this endpoint
     *
     * @param securityOn  a boolean flag indicating security is on or not
     */
    public void setSecurityOn(boolean securityOn) {
        this.securityOn = securityOn;
    }

    /**
     * Return the Rampart Security configuration policys' 'key' to be used (See Rampart)
     *
     * @return the Rampart Security configuration policys' 'key' to be used (See Rampart)
     */
    public String getWsSecPolicyKey() {
        return wsSecPolicyKey;
    }

    /**
     * Set the Rampart Security configuration policys' 'key' to be used (See Rampart)
     *
     * @param wsSecPolicyKey the Rampart Security configuration policys' 'key' to be used
     */
    public void setWsSecPolicyKey(String wsSecPolicyKey) {
        this.wsSecPolicyKey = wsSecPolicyKey;
    }

    /**
     * Return the Rampart Security configuration policys' 'key' to be used for inbound messages
     * (See Rampart)
     *
     * @return the Rampart Security configuration policys' 'key' to be used for inbound messages
     */
    public String getInboundWsSecPolicyKey() {
        return inboundWsSecPolicyKey;
    }

    /**
     * Set the Rampart Security configuration policys' 'key' to be used for inbound messages
     * (See Rampart)
     *
     * @param inboundWsSecPolicyKey the Rampart Security configuration policys' 'key' to be used
     */
    public void setInboundWsSecPolicyKey(String inboundWsSecPolicyKey) {
        this.inboundWsSecPolicyKey = inboundWsSecPolicyKey;
    }

    /**
     * Return the Rampart Security configuration policys' 'key' to be used for outbound messages
     * (See Rampart)
     *
     * @return the ORampart Security configuration policys' 'key' to be used for outbound messages
     */
    public String getOutboundWsSecPolicyKey() {
        return outboundWsSecPolicyKey;
    }

    /**
     * Set the Rampart Security configuration policys' 'key' to be used (See Rampart)
     *
     * @param outboundWsSecPolicyKey the Rampart Security configuration policys' 'key' to be used
     */
    public void setOutboundWsSecPolicyKey(String outboundWsSecPolicyKey) {
        this.outboundWsSecPolicyKey = outboundWsSecPolicyKey;
    }

    /**
     * Get the WS-RM configuration policys' 'key' to be used
     *
     * @return the WS-RM configuration policys' 'key' to be used
     */
    public String getWsRMPolicyKey() {
        return wsRMPolicyKey;
    }

    /**
     * Set the WS-RM configuration policys' 'key' to be used
     *
     * @param wsRMPolicyKey the WS-RM configuration policys' 'key' to be used
     */
    public void setWsRMPolicyKey(String wsRMPolicyKey) {
        this.wsRMPolicyKey = wsRMPolicyKey;
    }

    public void setUseSeparateListener(boolean b) {
        this.useSeparateListener = b;
    }

    public boolean isUseSeparateListener() {
        return useSeparateListener;
    }

    public void setForcePOX(boolean forcePOX) {
        this.forcePOX = forcePOX;
    }

    public boolean isForcePOX() {
        return forcePOX;
    }

    public boolean isForceGET() {
        return forceGET;
    }

    public void setForceGET(boolean forceGET) {
        this.forceGET = forceGET;
    }

    public void setForceSOAP11(boolean forceSOAP11) {
        this.forceSOAP11 = forceSOAP11;
    }

    public boolean isForceSOAP11() {
        return forceSOAP11;
    }

    public void setForceSOAP12(boolean forceSOAP12) {
        this.forceSOAP12 = forceSOAP12;
    }

    public boolean isForceSOAP12() {
        return forceSOAP12;
    }

    public boolean isForceREST() {
        return forceREST;
    }

    public void setForceREST(boolean forceREST) {
        this.forceREST = forceREST;
    }

    public boolean isUseMTOM() {
        return useMTOM;
    }

    public void setUseMTOM(boolean useMTOM) {
        this.useMTOM = useMTOM;
    }

    public boolean isUseSwa() {
        return useSwa;
    }

    public void setUseSwa(boolean useSwa) {
        this.useSwa = useSwa;
    }

    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    /**
     * Set the timeout duration.
     *
     * @param timeoutDuration a duration in milliseconds
     */
    public void setTimeoutDuration(long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    public int getTimeoutAction() {
        return timeoutAction;
    }

    public void setTimeoutAction(int timeoutAction) {
        this.timeoutAction = timeoutAction;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Get the charset encoding for messages sent to the endpoint.
     *
     * @return charSetEncoding
     */
    public String getCharSetEncoding() {
        return charSetEncoding;
    }

    /**
     * Set the charset encoding for messages sent to the endpoint.
     *
     * @param charSetEncoding the charset encoding or <code>null</code>
     */
    public void setCharSetEncoding(String charSetEncoding) {
        this.charSetEncoding = charSetEncoding;
    }

    /**
     * Get the suspend on fail duration.
     *
     * @return suspendOnFailDuration
     */
    public long getInitialSuspendDuration() {
        return initialSuspendDuration;
    }

    /**
     * Set the suspend on fail duration.
     *
     * @param initialSuspendDuration a duration in milliseconds
     */
    public void setInitialSuspendDuration(long initialSuspendDuration) {
        this.initialSuspendDuration = initialSuspendDuration;
    }

    public int getTraceState() {
        return traceState;
    }

    public void setTraceState(int traceState) {
        this.traceState = traceState;
    }

    public float getSuspendProgressionFactor() {
        return suspendProgressionFactor;
    }

    public void setSuspendProgressionFactor(float suspendProgressionFactor) {
        this.suspendProgressionFactor = suspendProgressionFactor;
    }

    public long getSuspendMaximumDuration() {
        return suspendMaximumDuration;
    }

    public void setSuspendMaximumDuration(long suspendMaximumDuration) {
        this.suspendMaximumDuration = suspendMaximumDuration;
    }

    public int getRetriesOnTimeoutBeforeSuspend() {
        return retriesOnTimeoutBeforeSuspend;
    }

    public void setRetriesOnTimeoutBeforeSuspend(int retriesOnTimeoutBeforeSuspend) {
        this.retriesOnTimeoutBeforeSuspend = retriesOnTimeoutBeforeSuspend;
    }

    public int getRetryDurationOnTimeout() {
        return retryDurationOnTimeout;
    }

    public void setRetryDurationOnTimeout(int retryDurationOnTimeout) {
        this.retryDurationOnTimeout = retryDurationOnTimeout;
    }

    public List<Integer> getSuspendErrorCodes() {
        return suspendErrorCodes;
    }

    public List<Integer> getTimeoutErrorCodes() {
        return timeoutErrorCodes;
    }

    public List<Integer> getRetryDisabledErrorCodes() {
        return retryDisabledErrorCodes;
    }

    public List<Integer> getRetryEnableErrorCodes() {
        return retryEnabledErrorCodes;
    }

    public boolean isReplicationDisabled() {
        return replicationDisabled;
    }

    public void setReplicationDisabled(boolean replicationDisabled) {
        this.replicationDisabled = replicationDisabled;
    }

    public void addSuspendErrorCode(int code) {
        suspendErrorCodes.add(code);
    }

    public void addTimeoutErrorCode(int code) {
        timeoutErrorCodes.add(code);
    }

    public void addRetryDisabledErrorCode(int code) {
        retryDisabledErrorCodes.add(code);
    }
    public void addRetryEnabledErrorCode(int code) {
        retryEnabledErrorCodes.add(code);
    }

    public boolean isHTTPEndpoint() {
        return isHTTPEndpoint;
    }

    public void setHTTPEndpoint(boolean HTTPEndpoint) {
        isHTTPEndpoint = HTTPEndpoint;
    }

    public String toString() {
        if (leafEndpoint != null) {
            return leafEndpoint.toString();
        } else if (address != null) {
            return "Address [" + address + "]";
        }
        return "[unknown endpoint]";
    }

    public void setLeafEndpoint(Endpoint leafEndpoint) {
        this.leafEndpoint = leafEndpoint;
    }

    public boolean isStatisticsEnable() {
        return this.aspectConfiguration != null && this.aspectConfiguration.isStatisticsEnable();
    }

    public void disableStatistics() {
        if (this.aspectConfiguration != null) {
            this.aspectConfiguration.disableStatistics();
        }
    }

    public void enableStatistics() {
        if (this.aspectConfiguration != null) {
            this.aspectConfiguration.enableStatistics();
        }
    }

    public void configure(AspectConfiguration aspectConfiguration) {
        this.aspectConfiguration = aspectConfiguration;
    }

    public AspectConfiguration getAspectConfiguration() {
        return this.aspectConfiguration;
    }


}