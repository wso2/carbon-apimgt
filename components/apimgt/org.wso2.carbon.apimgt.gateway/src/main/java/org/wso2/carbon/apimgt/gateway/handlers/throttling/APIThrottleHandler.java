/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.AccessRateController;
import org.apache.synapse.commons.throttle.core.ConcurrentAccessController;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConfiguration;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleDataHolder;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;
import org.apache.synapse.commons.throttle.core.factory.ThrottleContextFactory;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIDescriptionGenUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This API handler is responsible for evaluating authenticated user requests against their
 * corresponding access tiers (SLAs) and deciding whether the requests should be accepted
 * or not. Note that this implementation assumes that all the requests are already authenticated
 * and have associated AuthenticationContext information. Otherwise it will assume that the request
 * should not be throttled in which case it will simply log a warning and accept the request.
 * When this handler decides to throttle a request out, it looks for a custom sequence named
 * ThrottleConstants.API_THROTTLE_OUT_HANDLER and executes it. Following that it will send
 * a HTTP 503 response to the API consumer.
 */
public class APIThrottleHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIThrottleHandler.class);

    /**
     * The Throttle object - holds all runtime and configuration data
     */
    private volatile Throttle throttle;
    /**
     * ConcurrentAccessController - limit the remote callers concurrent access
     */
    private ConcurrentAccessController concurrentAccessController = null;
    /**
     * Access rate controller - limit the remote caller access
     */
    private AccessRateController accessController;

    private RoleBasedAccessRateController roleBasedAccessController;

    private RoleBasedAccessRateController applicationRoleBasedAccessController;

    public static final String RESOURCE_THROTTLE_KEY = "resource_throttle_context";

    private Map<String, Boolean> continueOnLimitReachedMap;

    /**
     * The property key that used when the ConcurrentAccessController
     * look up from ConfigurationContext
     */
    private String key;
    /**
     * The key for getting the throttling policy - key refers to a/an [registry] Api entry
     */
    private String policyKey = null;

    /**
     * The key for getting the throttling policy - key refers to a/an [registry] Application entry
     */
    private String policyKeyApplication = null;

    /**
     * The key for getting the throttling policy - key refers to a/an [registry] Resource entry
     */
    private String policyKeyResource = null;

    /**
     * The concurrent access control group id
     */
    private String id;
    /**
     * Version number of the throttle policy
     */
    private long version;

    private String sandboxUnitTime = "1000";

    private String productionUnitTime = "1000";

    private String sandboxMaxCount;

    private String productionMaxCount;


    /**
     * Does this env. support clustering
     */
    private boolean isClusteringEnable = false;

    public APIThrottleHandler() {
        this.accessController = new AccessRateController();
        this.roleBasedAccessController = new RoleBasedAccessRateController();
        this.applicationRoleBasedAccessController = new RoleBasedAccessRateController();
    }

    public boolean handleRequest(MessageContext messageContext) {
        Timer timer = getTimer();
        Timer.Context context = timer.start();
        long executionStartTime = System.nanoTime();
        try {
            return doThrottle(messageContext);
        } finally {
            messageContext.setProperty(APIMgtGatewayConstants.THROTTLING_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionStartTime));
            context.stop();
        }
    }

    public boolean handleResponse(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        boolean isResponse = messageContext.isResponse();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();

        ThrottleDataHolder dataHolder = null;
        if (cc == null) {
            handleException("Error while retrieving ConfigurationContext from messageContext");
        }

        synchronized (cc) {
            dataHolder = (ThrottleDataHolder) cc.getProperty(ThrottleConstants.THROTTLE_INFO_KEY);
            if (dataHolder == null) {
                dataHolder = new ThrottleDataHolder();
                cc.setNonReplicableProperty(ThrottleConstants.THROTTLE_INFO_KEY, dataHolder);
            }
        }

        if ((throttle == null && !isResponse) || (isResponse && concurrentAccessController == null)) {
            isClusteringEnable = isClusteringEnabled();
        }

        if (!isResponse) {
            //check the availability of the ConcurrentAccessController
            //if this is a clustered environment
            if (isClusteringEnable) {
                concurrentAccessController = (ConcurrentAccessController) cc.getProperty(key);
            }
            initThrottle(messageContext, cc);
        } else {
            // if the message flow path is OUT , then must lookup from ConfigurationContext -
            // never create ,just get the existing one
            concurrentAccessController = (ConcurrentAccessController) cc.getProperty(key);
        }


        // perform concurrency throttling
        boolean canAccess = doThrottleByConcurrency(isResponse);
        // if the access is success through concurrency throttle and if this is a request message
        // then do access rate based throttling
        if (canAccess && !isResponse && throttle != null) {
            canAccess = throttleByAccessRate(axis2MC, cc) &&
                    doRoleBasedAccessThrottling(messageContext, cc);
        }

        // All the replication functionality of the access rate based throttling handled by itself
        // Just replicate the current state of ConcurrentAccessController
        if (isClusteringEnable && concurrentAccessController != null) {

            try {
                Replicator.replicate(cc);
            } catch (ClusteringFault clusteringFault) {
                handleException("Error during the replicating  states ", clusteringFault);
            }

        }

        if (!canAccess) {
            handleThrottleOut(messageContext);
            return false;
        }
        return true;
    }

    private void handleThrottleOut(MessageContext messageContext) {

        String errorMessage = null;
        String errorDescription = null;
        int errorCode = -1;
        int httpErrorCode = -1;

        if (APIThrottleConstants.HARD_LIMIT_EXCEEDED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE;
            errorMessage = "API Limit Reached";
            errorDescription = "API not accepting requests";
            // It it's a hard limit exceeding, we tell it as service not being available.
            httpErrorCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
        } else if (APIThrottleConstants.API_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else if (APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.RESOURCE_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else {
            errorCode = APIThrottleConstants.APPLICATION_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        }

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription));
        } else {
            setSOAPFault(messageContext, errorMessage, errorDescription);
        }

        sendFault(messageContext, httpErrorCode);
    }

    private OMElement getFaultPayload(int throttleErrorCode, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                                               APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(throttleErrorCode));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private boolean doThrottleByConcurrency(boolean isResponse) {
        boolean canAccess = true;
        if (concurrentAccessController != null) {
            // do the concurrency throttling
            int concurrentLimit = concurrentAccessController.getLimit();
            if (log.isDebugEnabled()) {
                log.debug("Concurrent access controller for ID: " + id +
                          " allows: " + concurrentLimit + " concurrent accesses");
            }
            int available;
            if (!isResponse) {
                available = concurrentAccessController.getAndDecrement();
                canAccess = available > 0;
                if (log.isDebugEnabled()) {
                    log.debug("Concurrency Throttle: Access " +
                              (canAccess ? "allowed" : "denied") + " :: " + available
                              + " of available of " + concurrentLimit + " connections");
                }
            } else {
                available = concurrentAccessController.incrementAndGet();
                if (log.isDebugEnabled()) {
                    log.debug("Concurrency Throttle : Connection returned" + " :: " +
                              available + " of available of " + concurrentLimit + " connections");
                }
            }
        }
        return canAccess;
    }

    private boolean throttleByAccessRate(org.apache.axis2.context.MessageContext axisMC,
                                         ConfigurationContext cc) {
        resolveTenantId();
        String callerId = null;
        boolean canAccess = true;
        //remote ip of the caller
        String remoteIP = (String) ((TreeMap) axisMC.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        if (remoteIP != null) {
            if (remoteIP.indexOf(',') > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(','));
            }
        } else {
            remoteIP = (String) axisMC.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        } //domain name of the caller
        String domainName = (String) axisMC.getPropertyNonReplicable(NhttpConstants.REMOTE_HOST);

        //Using remote caller domain name , If there is a throttle configuration for
        // this domain name ,then throttling will occur according to that configuration
        if (domainName != null) {
            // do the domain based throttling
            if (log.isTraceEnabled()) {
                log.trace("The Domain Name of the caller is :" + domainName);
            }
            // loads the DomainBasedThrottleContext
            ThrottleContext context
                    = throttle.getThrottleContext(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
            if (context != null) {
                //loads the DomainBasedThrottleConfiguration
                ThrottleConfiguration config = context.getThrottleConfiguration();
                if (config != null) {
                    //checks the availability of a policy configuration for  this domain name
                    callerId = config.getConfigurationKeyOfCaller(domainName);
                    if (callerId != null) {  // there is configuration for this domain name
                        //If this is a clustered env.
                        if (isClusteringEnable) {
                            context.setConfigurationContext(cc);
                            context.setThrottleId(id);
                        }
                        try {
                            //Checks for access state
                            AccessInformation accessInformation =
                                    accessController.canAccess(context, callerId, ThrottleConstants.DOMAIN_BASE);
                            canAccess = accessInformation.isAccessAllowed();
                            if (log.isDebugEnabled()) {
                                log.debug("Access " + (canAccess ? "allowed" : "denied")
                                          + " for Domain Name : " + domainName);
                            }
                            //In the case of both of concurrency throttling and
                            //rate based throttling have enabled ,
                            //if the access rate less than maximum concurrent access ,
                            //then it is possible to occur death situation.To avoid that reset,
                            //if the access has denied by rate based throttling
                            if (!canAccess && concurrentAccessController != null) {
                                concurrentAccessController.incrementAndGet();
                                if (isClusteringEnable) {
                                    cc.setProperty(key, concurrentAccessController);
                                }
                            }
                        } catch (ThrottleException e) {
                            handleException("Error occurred during throttling", e);
                        }
                    }
                }
            }
        } else {
            log.debug("The Domain name of the caller cannot be found");
        }

        //At this point , any configuration for the remote caller hasn't found ,
        //therefore trying to find a configuration policy based on remote caller ip
        if (callerId == null) {
            //do the IP-based throttling
            if (remoteIP == null) {
                if (log.isDebugEnabled()) {
                    log.debug("The IP address of the caller cannot be found");
                }
                canAccess = true;

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The IP Address of the caller is :" + remoteIP);
                }
                try {
                    // Loads the IPBasedThrottleContext
                    ThrottleContext context =
                            throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY);
                    if (context != null) {
                        //Loads the IPBasedThrottleConfiguration
                        ThrottleConfiguration config = context.getThrottleConfiguration();
                        if (config != null) {
                            //Checks the availability of a policy configuration for  this ip
                            callerId = config.getConfigurationKeyOfCaller(remoteIP);
                            if (callerId != null) {   // there is configuration for this ip
                                //For clustered env.
                                if (isClusteringEnable) {
                                    context.setConfigurationContext(cc);
                                    context.setThrottleId(id);
                                }
                                //Checks access state
                                AccessInformation accessInformation =
                                        accessController.canAccess(context, callerId, ThrottleConstants.IP_BASE);

                                canAccess = accessInformation.isAccessAllowed();
                                if (log.isDebugEnabled()) {
                                    log.debug("Access " + (canAccess ? "allowed" : "denied")
                                              + " for IP : " + remoteIP);
                                }
                                //In the case of both of concurrency throttling and
                                //rate based throttling have enabled ,
                                //if the access rate less than maximum concurrent access ,
                                //then it is possible to occur death situation.To avoid that reset,
                                //if the access has denied by rate based throttling
                                if (!canAccess && concurrentAccessController != null) {
                                    concurrentAccessController.incrementAndGet();
                                    if (isClusteringEnable) {
                                        cc.setProperty(key, concurrentAccessController);
                                    }
                                }
                            }
                        }
                    }
                } catch (ThrottleException e) {
                    handleException("Error occurred during throttling", e);
                }
            }
        }
        return canAccess;
    }

    private boolean doRoleBasedAccessThrottling(MessageContext synCtx, ConfigurationContext cc) {

        boolean canAccess = true;
        ThrottleDataHolder dataHolder = (ThrottleDataHolder)
                cc.getPropertyNonReplicable(ThrottleConstants.THROTTLE_INFO_KEY);

        if (throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY) == null) {
            //there is no throttle configuration for RoleBase Throttling
            //skip role base throttling
            return true;
        }

        ConcurrentAccessController cac = null;
        if (isClusteringEnable) {
            // for clustered  env.,gets it from axis configuration context
            cac = (ConcurrentAccessController) cc.getProperty(key);
        }

        if (!synCtx.isResponse()) {
            // gets the remote caller role name
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
            String accessToken;
            String consumerKey;
            String authorizedUser;
            String roleID;
            String applicationId;
            String applicationTier;

            if (authContext != null) {
                //Although the method says getApiKey, what is actually returned is the Bearer header (accessToken)
                accessToken = authContext.getApiKey();
                consumerKey = authContext.getConsumerKey();
                authorizedUser = authContext.getUsername();
                roleID = authContext.getTier();
                applicationTier = authContext.getApplicationTier();
                applicationId = authContext.getApplicationId();

                if (accessToken == null || roleID == null) {
                    log.warn("No consumer key or role information found on the request - " +
                             "Throttling not applied");
                    return true;
                }
            } else {
                log.warn("No authentication context information found on the request - " +
                         "Throttling not applied");
                return true;
            }

            // Domain name based throttling
            //check whether a configuration has been defined for this role name or not
            //loads the ThrottleContext
            ThrottleContext resourceContext = throttle.getThrottleContext(RESOURCE_THROTTLE_KEY);
            if (resourceContext == null) {
                log.warn("Unable to load throttle context");
                return true;
            }
            //Loads the ThrottleConfiguration
            ThrottleConfiguration config = resourceContext.getThrottleConfiguration();
            if (config != null) {
                String applicationRoleId = null;
                //If an application level tier has been specified and it is not 'Unlimited'
                if (applicationTier != null && !APIConstants.UNLIMITED_TIER.equals(applicationTier)) {
                    //Get the configuration role of the application
                    //applicationRoleId = config.getConfigurationKeyOfCaller(applicationTier);
                    applicationRoleId = applicationTier;
                }

                AccessInformation info = null;
                //If application level throttling is applied
                if (applicationRoleId != null) {
                    ThrottleContext applicationThrottleContext = getApplicationThrottleContext(synCtx, dataHolder, applicationId);
                    if (isClusteringEnable) {
                        applicationThrottleContext.setConfigurationContext(cc);
                        applicationThrottleContext.setThrottleId(id);
                    }
                    //First throttle by application
                    try {
                        info = applicationRoleBasedAccessController.canAccess(applicationThrottleContext,
                                                                              applicationId, applicationRoleId);
                        if (log.isDebugEnabled()) {
                            log.debug("Throttle by Application " + applicationId);
                            log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
                        }
                    } catch (ThrottleException e) {
                        log.warn("Exception occurred while performing role " + "based throttling", e);
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                        return false;
                    }

                    //check for the permission for access
                    if (info != null && !info.isAccessAllowed()) {
                        log.info("Exceeded the allocated quota in Application level.");
                        //In the case of both of concurrency throttling and
                        //rate based throttling have enabled ,
                        //if the access rate less than maximum concurrent access ,
                        //then it is possible to occur death situation.To avoid that reset,
                        //if the access has denied by rate based throttling
                        if (cac != null) {
                            cac.incrementAndGet();
                            // set back if this is a clustered env
                            if (isClusteringEnable) {
                                cc.setProperty(key, cac);
                                resourceContext.setConfigurationContext(cc);
                                //replicate the current state of ConcurrentAccessController
                                try {
                                    Replicator.replicate(cc, new String[]{key});
                                } catch (ClusteringFault clusteringFault) {
                                    log.error("Error during replicating states", clusteringFault);
                                }
                            }
                        }
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                        return false;
                    }
                }

                //---------------End of application level throttling------------
                //==============================Start of Resource level throttling======================================

                //get throttling information for given request with resource path and http verb
//                VerbInfoDTO verbInfoDTO = null;

                //verbInfoDTO = validator.getVerbInfoDTOFromAPIData(apiContext, apiVersion, requestPath, httpMethod);
                VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);

                String resourceLevelRoleId = null;
                //no data related to verb information data
                if (verbInfoDTO == null) {
                    log.warn("Error while getting throttling information for resource and http verb");
                    return false;
                } else {
                    //Not only we can proceed
                    String resourceAndHTTPVerbThrottlingTier = verbInfoDTO.getThrottling();
                    //If there no any tier then we need to set it as unlimited
                    if (resourceAndHTTPVerbThrottlingTier == null) {
                        log.warn("Unable to find throttling information for resource and http verb. Throttling will " +
                                 "not apply");
                    } else {
                        resourceLevelRoleId = resourceAndHTTPVerbThrottlingTier;
                    }
                    //adding consumerKey and authz_user combination instead of access token to resourceAndHTTPVerbKey
                    //This avoids sending more than the permitted number of requests in a unit time by
                    // regenerating the access token
                    String resourceAndHTTPVerbKey = verbInfoDTO.getRequestKey() + '-' + consumerKey + ':' +
                                                    authorizedUser;
                    //resourceLevelTier should get from auth context or request synapse context
                    // getResourceAuthenticationScheme(apiContext, apiVersion, requestPath, httpMethod);
                    //api + resource+http verb combination as verb_resource_api_combined_key
                    //if request not null then only we proceed
                    if (resourceLevelRoleId != null) {
                        try {
                            //If the application has not been subscribed to the Unlimited Tier and
                            //if application level throttling has passed
                            if (!APIConstants.UNLIMITED_TIER.equals(resourceLevelRoleId) && (info == null || info
                                    .isAccessAllowed())) {
                                //Throttle by resource and http verb
                                // If this is a clustered env.
                                if (isClusteringEnable) {
                                    resourceContext.setConfigurationContext(cc);
                                    resourceContext.setThrottleId(id + "resource");
                                }
                                info = roleBasedAccessController.canAccess(resourceContext, resourceAndHTTPVerbKey,
                                                                           resourceAndHTTPVerbThrottlingTier);
                            }
                        } catch (ThrottleException e) {
                            log.warn("Exception occurred while performing resource" + "based throttling", e);
                            synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
                            return false;
                        }

                        //check for the permission for access
                        if (info != null && !info.isAccessAllowed()) {
                            log.info("Exceeded the allocated quota in Resource level.");
                            //In the case of both of concurrency throttling and
                            //rate based throttling have enabled ,
                            //if the access rate less than maximum concurrent access ,
                            //then it is possible to occur death situation.To avoid that reset,
                            //if the access has denied by rate based throttling
                            if (cac != null) {
                                cac.incrementAndGet();
                                // set back if this is a clustered env
                                if (isClusteringEnable) {
                                    cc.setProperty(key, cac);
                                    //replicate the current state of ConcurrentAccessController
                                    try {
                                        Replicator.replicate(cc, new String[]{key});
                                    } catch (ClusteringFault clusteringFault) {
                                        log.error("Error during replicating states", clusteringFault);
                                    }
                                }
                            }
                            if (isContinueOnThrottleReached(resourceAndHTTPVerbThrottlingTier)) {
                                // This means that we are allowing the requests to continue even after the throttling
                                // limit has reached.
                                if (synCtx.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY) == null) {
                                    synCtx.setProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY, Boolean.TRUE);
                                }
                            } else {
                                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
                                return false;
                            }
                        }
                    } else {
                        log.warn("Unable to find the throttle policy for role.");
                    }
                }
                //==============================End of Resource level throttling=======================================

                //---------------Start of API level throttling------------------

                // Domain name based throttling
                //check whether a configuration has been defined for this role name or not
                //loads the ThrottleContext
                ThrottleContext context = throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                String apiKey;
                if (context == null) {
                    log.warn("Unable to load throttle context");
                    return true;
                }
                // If this is a clustered env.
                //check for configuration role of the caller
                config = context.getThrottleConfiguration();
                String consumerRoleID = config.getConfigurationKeyOfCaller(roleID);
                if (isClusteringEnable) {
                    context.setConfigurationContext(cc);
                    context.setThrottleId(id);
                }
                try {

                    String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
                    String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

                    apiContext = apiContext != null ? apiContext : "";
                    apiVersion = apiVersion != null ? apiVersion : "";
                    //adding consumerKey and authz_user combination instead of access token to apiKey
                    //This avoids sending more than the permitted number of requests in a unit time by
                    // regenerating the access token
                    apiKey = apiContext + ':' + apiVersion + ':' + consumerKey + ':' + authorizedUser;
                    //If the application has not been subscribed to the Unlimited Tier and
                    //if application level throttling has passed
                    if (!APIConstants.UNLIMITED_TIER.equals(roleID) && (info == null || info.isAccessAllowed())) {
                        //Throttle by access token
                        info = roleBasedAccessController.canAccess(context, apiKey, consumerRoleID);
                    }
                } catch (ThrottleException e) {
                    log.warn("Exception occurred while performing role " + "based throttling", e);
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.API_LIMIT_EXCEEDED);
                    return false;
                }

                //check for the permission for access
                if (info != null && !info.isAccessAllowed()) {
                    log.info("Exceeded the allocated quota in API level.");
                    //In the case of both of concurrency throttling and
                    //rate based throttling have enabled ,
                    //if the access rate less than maximum concurrent access ,
                    //then it is possible to occur death situation.To avoid that reset,
                    //if the access has denied by rate based throttling
                    if (cac != null) {
                        cac.incrementAndGet();
                        // set back if this is a clustered env
                        if (isClusteringEnable) {
                            cc.setProperty(key, cac);
                            //replicate the current state of ConcurrentAccessController
                            try {
                                Replicator.replicate(cc, new String[]{key});
                            } catch (ClusteringFault clusteringFault) {
                                log.error("Error during replicating states", clusteringFault);
                            }
                        }
                    }
                    if (isContinueOnThrottleReached(consumerRoleID)) {
                        // This means that we are allowing the requests to continue even after the throttling
                        // limit has reached.
                        if (synCtx.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY) == null) {
                            synCtx.setProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY, Boolean.TRUE);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Request throttled at API level for throttle key" + apiKey
                                    + ". But role " + consumerRoleID + "allows to continue to serve requests");
                        }
                    } else {
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.API_LIMIT_EXCEEDED);
                        return false;
                    }
                }
            }
        }

        //---------------End of API level throttling------------------

        //---------------Start of Hard throttling------------------

        ThrottleContext hardThrottleContext = throttle.getThrottleContext(
                APIThrottleConstants.HARD_THROTTLING_CONFIGURATION);

        try {
            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

            apiContext = apiContext != null ? apiContext : "";
            apiVersion = apiVersion != null ? apiVersion : "";
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);

            if (hardThrottleContext != null && authContext.getKeyType() != null) {
                String throttleKey = apiContext + ':' + apiVersion + ':' + authContext.getKeyType();
                AccessInformation info = null;
                if (isClusteringEnable) {
                    hardThrottleContext.setConfigurationContext(cc);
                }

                if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(authContext.getKeyType())) {
                    hardThrottleContext.setThrottleId(id + APIThrottleConstants.PRODUCTION_HARD_LIMIT);
                    info = roleBasedAccessController.canAccess(hardThrottleContext, throttleKey,
                            APIThrottleConstants.PRODUCTION_HARD_LIMIT);
                } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(authContext.getKeyType())) {
                    hardThrottleContext.setThrottleId(id + APIThrottleConstants.SANDBOX_HARD_LIMIT);
                    info = roleBasedAccessController.canAccess(hardThrottleContext, throttleKey,
                            APIThrottleConstants.SANDBOX_HARD_LIMIT);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Throttle by hard limit " + throttleKey);
                    log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
                }

                if (info != null && !info.isAccessAllowed()) {
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
                    log.info("Hard Throttling limit exceeded.");
                    return false;
                }
            }

        } catch (ThrottleException e) {
            log.warn("Exception occurred while performing role based throttling", e);
            synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
            return false;
        }
        return canAccess;
    }

    private void initThrottle(MessageContext synCtx, ConfigurationContext cc) {
        if (policyKey == null) {
            throw new SynapseException("Throttle policy unspecified for the API");
        }

        Entry entry = synCtx.getConfiguration().getEntryDefinition(policyKey);
        if (entry == null) {
            handleException("Cannot find throttling policy using key: " + policyKey);
        }
        Object entryValue = null;
        boolean reCreate = false;

        if (entry.isDynamic()) {
            if ((!entry.isCached()) || (entry.isExpired()) || throttle == null) {
                entryValue = synCtx.getEntry(this.policyKey);
                if (this.version != entry.getVersion()) {
                    reCreate = true;// if there is a change, it will recreate
                }
            }
        } else if (this.throttle == null) {
            entryValue = synCtx.getEntry(this.policyKey);
        }

        if (reCreate || throttle == null) {
            if (entryValue == null || !(entryValue instanceof OMElement)) {
                handleException("Unable to load throttling policy using key: " + policyKey);
            }
            version = entry.getVersion();

            // Check for reload in a cluster environment
            // For clustered environments, if the concurrent access controller
            // is not null and throttle is not null , then must reload.
            if (isClusteringEnable && concurrentAccessController != null && throttle != null) {
                concurrentAccessController = null; // set null ,
                // because need to reload
            }

            try {
                // Creates the throttle from the policy
                synchronized (this) {
                    if (throttle == null || reCreate) {
                        OMElement policyOMElement = (OMElement) entryValue;
                        throttle = ThrottleFactory.createMediatorThrottle(
                                PolicyEngine.getPolicy(policyOMElement));

                        //load the resource level tiers
                        Object resEntryValue = synCtx.getEntry(this.policyKeyResource);
                        if (resEntryValue == null || !(resEntryValue instanceof OMElement)) {
                            handleException("Unable to load throttling policy using key: " + this.policyKeyResource);
                        }

                        //create throttle for the resource level
                        Throttle resThrottle = ThrottleFactory
                                .createMediatorThrottle(PolicyEngine.getPolicy((OMElement) resEntryValue));
                        //get the throttle Context for the resource level
                        ThrottleContext throttleContext = resThrottle
                                .getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);

                        if (throttleContext != null) {
                            ThrottleConfiguration throttleConfiguration = throttleContext.getThrottleConfiguration();
                            ThrottleContext resourceContext =
                                    createThrottleContext(throttleConfiguration);
                            throttle.addThrottleContext(RESOURCE_THROTTLE_KEY, resourceContext);
                        }


                        OMElement hardThrottlingPolicy = createHardThrottlingPolicy();
                        if (hardThrottlingPolicy != null) {
                            Throttle tempThrottle = ThrottleFactory.createMediatorThrottle(
                                    PolicyEngine.getPolicy(hardThrottlingPolicy));
                            ThrottleConfiguration newThrottleConfig = tempThrottle.getThrottleConfiguration(ThrottleConstants
                                    .ROLE_BASED_THROTTLE_KEY);
                            ThrottleContext hardThrottling = createThrottleContext(newThrottleConfig);
                            throttle.addThrottleContext(APIThrottleConstants.HARD_THROTTLING_CONFIGURATION, hardThrottling);
                        }

                        // We check to what tiers allows to continue on quota reached.
                        OMElement assertionElement = policyOMElement.getFirstChildWithName(
                                APIConstants.ASSERTION_ELEMENT);
                        Iterator tierElementIterator = assertionElement.getChildrenWithName(
                                APIConstants.POLICY_ELEMENT);

                        if (continueOnLimitReachedMap == null) {
                            continueOnLimitReachedMap = new HashMap<String, Boolean>();
                        } else if (!continueOnLimitReachedMap.isEmpty()) {
                            continueOnLimitReachedMap.clear();
                        }
                        while (tierElementIterator.hasNext()) {
                            OMElement tierElement = (OMElement) tierElementIterator.next();
                            String tierName = tierElement.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT)
                                    .getText();
                            try {
                                Map<String, Object> tierAttributes = APIDescriptionGenUtil.getTierAttributes
                                        (tierElement);
                                for (Map.Entry<String, Object> tierEntry : tierAttributes.entrySet()) {
                                    if (APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE.equalsIgnoreCase(
                                            tierEntry.getKey())) {
                                        // We are putting the inverse value of the attribute to the map.
                                        // The reason is that we have the value whether to stop when quota reached.
                                        // The map contains the inverse of this, whether to continue when quota reached.
                                        continueOnLimitReachedMap.put(
                                                tierName, !Boolean.parseBoolean((String) tierEntry.getValue()));
                                        break;
                                    }
                                }
                            } catch (APIManagementException e) {
                                // We do not throw the exception. If there is any exception, then the others can
                                // still function without any issue.
                                log.warn("Unable to get the action for quota reached of tier : " + tierName);
                            }
                        }
                    }
                }
                //For non-clustered  environment , must re-initiates
                //For  clustered  environment,
                //concurrent access controller is null ,
                //then must re-initiates
                if (throttle != null && (concurrentAccessController == null || !isClusteringEnable)) {
                    concurrentAccessController = throttle.getConcurrentAccessController();
                    if (concurrentAccessController != null) {
                        cc.setProperty(key, concurrentAccessController);
                    } else {
                        cc.removeProperty(key);
                    }
                }

            } catch (ThrottleException e) {
                handleException("Error processing the throttling policy", e);
            }
        }
    }

    public void setId(String id) {
        this.id = id;
        this.key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + id + ThrottleConstants.CAC_SUFFIX;
    }

    public String getId() {
        return id;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public String gePolicyKey() {
        return policyKey;
    }

    public void setPolicyKeyApplication(String policyKeyApplication) {
        this.policyKeyApplication = policyKeyApplication;
    }

    public String gePolicyKeyApplication() {
        return policyKeyApplication;
    }

    public void setPolicyKeyResource(String policyKeyResource) {
        this.policyKeyResource = policyKeyResource;
    }

    public String gePolicyKeyResource() {
        return policyKeyResource;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }


    public String getSandboxUnitTime() {
        return sandboxUnitTime;
    }

    public void setSandboxUnitTime(String sandboxUnitTime) {
        this.sandboxUnitTime = sandboxUnitTime;
    }

    public String getSandboxMaxCount() {
        return sandboxMaxCount;
    }

    public void setSandboxMaxCount(String sandboxMaxCount) {
        this.sandboxMaxCount = sandboxMaxCount;
    }

    private OMElement createHardThrottlingPolicy() {

        if (productionMaxCount == null &&
                sandboxMaxCount == null) {
            return null;
        }

        OMElement parsedPolicy = null;

        StringBuilder policy = new StringBuilder("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" " +
                "xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
                "    <throttle:MediatorThrottleAssertion>\n");

        if (productionMaxCount != null && productionUnitTime != null) {
            policy.append(createPolicyForRole(APIThrottleConstants.PRODUCTION_HARD_LIMIT, productionUnitTime, productionMaxCount));
        }

        if (sandboxMaxCount != null && sandboxUnitTime != null) {
            policy.append(createPolicyForRole(APIThrottleConstants.SANDBOX_HARD_LIMIT, sandboxUnitTime, sandboxMaxCount));
        }

        policy.append("    </throttle:MediatorThrottleAssertion>\n" +
                "</wsp:Policy>");
        try {
            parsedPolicy = AXIOMUtil.stringToOM(policy.toString());
        } catch (XMLStreamException e) {
            log.error("Error occurred while creating policy file for Hard Throttling.", e);
        }
        return parsedPolicy;
    }

    private String createPolicyForRole(String roleId, String unitTime, String maxCount) {
        return "<wsp:Policy>\n" +
                "     <throttle:ID throttle:type=\"ROLE\">" + roleId + "</throttle:ID>\n" +
                "            <wsp:Policy>\n" +
                "                <throttle:Control>\n" +
                "                    <wsp:Policy>\n" +
                "                        <throttle:MaximumCount>" + maxCount + "</throttle:MaximumCount>\n" +
                "                        <throttle:UnitTime>" + unitTime + "</throttle:UnitTime>\n" +
                "                    </wsp:Policy>\n" +
                "                </throttle:Control>\n" +
                "            </wsp:Policy>\n" +
                " </wsp:Policy>\n";
    }

    public String getProductionUnitTime() {
        return productionUnitTime;
    }

    public void setProductionUnitTime(String productionUnitTime) {
        this.productionUnitTime = productionUnitTime;
    }

    public String getProductionMaxCount() {
        return productionMaxCount;
    }

    public void setProductionMaxCount(String productionMaxCount) {
        this.productionMaxCount = productionMaxCount;
    }

    private synchronized boolean isContinueOnThrottleReached(String tier) {
        if (continueOnLimitReachedMap.isEmpty()) {
            // This means that there are no tiers that has the attribute defined. Hence we should not allow to continue.
            return false;
        }
        // This means that the tier is not there. Then we have should not allow to continue.
        return continueOnLimitReachedMap.containsKey(tier) && continueOnLimitReachedMap.get(tier);
    }

    protected Timer getTimer() {
        return MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
    }

    protected boolean isClusteringEnabled() {
        return GatewayUtils.isClusteringEnabled();
    }

    protected org.apache.axis2.context.MessageContext getAxis2MessageContext(Axis2MessageContext messageContext) {
        return messageContext.
                getAxis2MessageContext();
    }

    protected void sendFault(MessageContext messageContext, int httpErrorCode) {
        Utils.sendFault(messageContext, httpErrorCode);
    }

    protected void setSOAPFault(MessageContext messageContext, String errorMessage, String errorDescription) {
        Utils.setSOAPFault(messageContext, "Server", errorMessage, errorDescription);
    }

    protected int resolveTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
    }

    protected ThrottleContext getApplicationThrottleContext(MessageContext synCtx, ThrottleDataHolder dataHolder, String
            applicationId) {
        return ApplicationThrottleController
                .getApplicationThrottleContext(synCtx, dataHolder, applicationId, policyKeyApplication);
    }

    protected ThrottleContext createThrottleContext(ThrottleConfiguration throttleConfiguration) throws ThrottleException {
        return ThrottleContextFactory.createThrottleContext(ThrottleConstants.ROLE_BASE, throttleConfiguration);
    }

}
