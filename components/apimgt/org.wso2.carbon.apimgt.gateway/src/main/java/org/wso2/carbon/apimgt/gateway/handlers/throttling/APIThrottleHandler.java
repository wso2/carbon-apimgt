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
import org.apache.axis2.clustering.ClusteringAgent;
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
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.throttle.core.*;
import org.wso2.carbon.throttle.core.factory.ThrottleContextFactory;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

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

    /**
     * The property key that used when the ConcurrentAccessController
     * look up from ConfigurationContext
     */
    private String key;
    /**
     * The key for getting the throttling policy - key refers to a/an [registry] entry
     */
    private String policyKey = null;
    /**
     * The concurrent access control group id
     */
    private String id;
    /**
     * Version number of the throttle policy
     */
    private long version;

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
        return doThrottle(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        boolean isResponse = messageContext.isResponse();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();

        ThrottleDataHolder dataHolder =
                (ThrottleDataHolder) cc.getProperty(ThrottleConstants.THROTTLE_INFO_KEY);

        if (dataHolder == null) {
            log.debug("Data holder not present...");

            synchronized (cc) {
                dataHolder =
                        (ThrottleDataHolder) cc.getProperty(ThrottleConstants.THROTTLE_INFO_KEY);
                if (dataHolder == null) {
                    dataHolder = new ThrottleDataHolder();
                    cc.setNonReplicableProperty(ThrottleConstants.THROTTLE_INFO_KEY, dataHolder);
                }
            }
        }

        synchronized (this) {
            if ((throttle == null && !isResponse) || (isResponse && concurrentAccessController == null)) {
                ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
                if (clusteringAgent != null) {
                    isClusteringEnable = true;
                }
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
            if (cc != null) {
                try {
                    Replicator.replicate(cc);
                } catch (ClusteringFault clusteringFault) {
                    handleException("Error during the replicating  states ", clusteringFault);
                }
            }
        }

        if (!canAccess) {
            handleThrottleOut(messageContext);
            return false;
        }
        return true;
    }

    private void handleThrottleOut(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, 900800);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Message throttled out");

        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        // By default we send a 503 response back
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload());
        } else {
            Utils.setSOAPFault(messageContext, "Server", "Message Throttled Out",
                               "You have exceeded your quota");
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        if (Utils.isCORSEnabled()) {
            /* For CORS support adding required headers to the fault response */
            Map<String, String> headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Utils.getAllowedOrigin((String) headers.get("Origin")));
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        Utils.sendFault(messageContext, HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    private OMElement getFaultPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                                               APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(APIThrottleConstants.THROTTLE_OUT_ERROR_CODE));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("Message Throttled Out");
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText("You have exceeded your quota");

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
        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String callerId = null;
        boolean canAccess = true;
        //remote ip of the caller
        String remoteIP = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("X-Forwarded-For");
        if (remoteIP != null) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) axisMC.getProperty("REMOTE_ADDR");
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
                            AccessInformation accessInformation = accessController.canAccess(context,
                                                                                             callerId, ThrottleConstants.DOMAIN_BASE);
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
                                AccessInformation accessInformation = accessController.canAccess(
                                        context,
                                        callerId,
                                        ThrottleConstants.IP_BASE);

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
            return canAccess;
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
            String roleID;
            String applicationId;
            String applicationTier;

            if (authContext != null) {
                //Although the method says getApiKey, what is actually returned is the Bearer header (accessToken)
                accessToken = authContext.getApiKey();
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

                    ThrottleContext applicationThrottleContext =
                            ApplicationThrottleController.getApplicationThrottleContext(synCtx, dataHolder, applicationId);
                    if (isClusteringEnable) {
                        applicationThrottleContext.setConfigurationContext(cc);
                        applicationThrottleContext.setThrottleId(id);
                    }
                    //First throttle by application
                    try {
                        info = applicationRoleBasedAccessController.canAccess(applicationThrottleContext, applicationId, applicationRoleId);
                        if (log.isDebugEnabled()) {
                            log.debug("Throttle by Application " + applicationId);
                            log.debug("Allowed = " + info != null ? info.isAccessAllowed() : "false");
                        }
                    } catch (ThrottleException e) {
                        log.warn("Exception occurred while performing role " +
                                 "based throttling", e);
                        canAccess = false;
                    }

                    //check for the permission for access
                    if (info != null && !info.isAccessAllowed()) {

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
                        canAccess = false;
                        return canAccess;
                    }
                }

                //---------------End of application level throttling------------
                //==============================Start of Resource level throttling=========================================

                //get throttling information for given request with resource path and http verb
                APIKeyValidator validator = new APIKeyValidator(ServiceReferenceHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration());
                VerbInfoDTO verbInfoDTO = null;
                try {
                    //verbInfoDTO = validator.getVerbInfoDTOFromAPIData(apiContext, apiVersion, requestPath, httpMethod);
                    verbInfoDTO = validator.findMatchingVerb(synCtx);
                } catch (APISecurityException e) {
                    log.error("API Security Exception " + e.getMessage());
                    e.printStackTrace();
                } catch (ResourceNotFoundException e) {
                    log.error("Could not find matching resource " + e.getMessage());
                    e.printStackTrace();
                }
                String resourceLevelRoleId = null;
                //no data related to verb information data
                if (verbInfoDTO == null) {
                    canAccess = false;
                    log.warn("Error while getting throttling information for resource and http verb");
                    return canAccess;
                } else {
                    //Not only we can proceed
                    String resourceAndHTTPVerbThrottlingTier = verbInfoDTO.getThrottling();
                    //If there no any tier then we need to set it as unlimited
                    if (resourceAndHTTPVerbThrottlingTier == null) {
                        log.warn("Unable to find throttling information for resource and http verb. Throttling will not apply");
                    } else {
                        resourceLevelRoleId = resourceAndHTTPVerbThrottlingTier;
                    }
                    String resourceAndHTTPVerbKey = verbInfoDTO.getRequestKey() + "-" + accessToken;
                    //resourceLevelTier should get from auth context or request synapse context
                    // getResourceAuthenticationScheme(apiContext, apiVersion, requestPath, httpMethod);
                    //api + resource+http verb combination as verb_resource_api_combined_key
                    //if request not null then only we proceed
                    if (resourceLevelRoleId != null) {
                        try {
                            //If the application has not been subscribed to the Unlimited Tier and
                            //if application level throttling has passed
                            if (!APIConstants.UNLIMITED_TIER.equals(resourceLevelRoleId) &&
                                (info == null || info.isAccessAllowed())) {
                                //Throttle by resource and http verb
                                // If this is a clustered env.
                                if (isClusteringEnable) {
                                    resourceContext.setConfigurationContext(cc);
                                    resourceContext.setThrottleId(id + "resource");
                                }
                                info = roleBasedAccessController.canAccess(resourceContext, resourceAndHTTPVerbKey, resourceAndHTTPVerbThrottlingTier);
                            }
                        } catch (ThrottleException e) {
                            log.warn("Exception occurred while performing resource" +
                                     "based throttling", e);
                            canAccess = false;
                            return canAccess;
                        }

                        //check for the permission for access
                        if (info != null && !info.isAccessAllowed()) {

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
                            canAccess = false;
                            return canAccess;
                        }
                    } else {
                        log.warn("Unable to find the throttle policy for role.");
                    }
                }
                //==============================End of Resource level throttling=========================================


                //---------------Start of API level throttling------------------

                // Domain name based throttling
                //check whether a configuration has been defined for this role name or not
                //loads the ThrottleContext
                ThrottleContext context = throttle.getThrottleContext(
                        ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                if (context == null) {
                    log.warn("Unable to load throttle context");
                    return true;
                }

                if (APIConstants.UNLIMITED_TIER.equals(roleID)) {
                    return canAccess;
                }

                //check for configuration role of the caller
                String consumerRoleID = config.getConfigurationKeyOfCaller(roleID);
                if (consumerRoleID != null) {
                    // If this is a clustered env.
                    if (isClusteringEnable) {
                        context.setConfigurationContext(cc);
                        context.setThrottleId(id);
                    }
                    try {

                        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
                        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

                        apiContext = apiContext != null ? apiContext : "";
                        apiVersion = apiVersion != null ? apiVersion : "";
                        String apiKey = apiContext + ":" + apiVersion + ":" + accessToken;
                        //If the application has not been subscribed to the Unlimited Tier and
                        //if application level throttling has passed
                        if (!APIConstants.UNLIMITED_TIER.equals(roleID) &&
                            (info == null || info.isAccessAllowed())) {
                            //Throttle by access token
                            info = roleBasedAccessController.canAccess(context, apiKey, consumerRoleID);
                        }
                    } catch (ThrottleException e) {
                        log.warn("Exception occurred while performing role " +
                                 "based throttling", e);
                        canAccess = false;
                    }

                    //check for the permission for access
                    if (info != null && !info.isAccessAllowed()) {

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
                        canAccess = false;
                    }
                } else {
                    log.warn("Unable to find the throttle policy for role: " + roleID);
                }
            }
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
            return;
        }
        Object entryValue = null;
        boolean reCreate = false;

        if (entry.isDynamic()) {
            if ((!entry.isCached()) || (entry.isExpired()) || throttle == null) {
                entryValue = synCtx.getEntry(this.policyKey);
                if (this.version != entry.getVersion()) {
                    reCreate = true;
                }
            }
        } else if (this.throttle == null) {
            entryValue = synCtx.getEntry(this.policyKey);
        }

        if (reCreate || throttle == null) {
            if (entryValue == null || !(entryValue instanceof OMElement)) {
                handleException("Unable to load throttling policy using key: " + policyKey);
                return;
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
                throttle = ThrottleFactory.createMediatorThrottle(
                        PolicyEngine.getPolicy((OMElement) entryValue));

                ThrottleContext throttleContext = throttle.getThrottleContext(
                        ThrottleConstants.ROLE_BASED_THROTTLE_KEY);

                if (throttleContext != null) {
                    ThrottleConfiguration throttleConfiguration = throttleContext.getThrottleConfiguration();
                    ThrottleContext resourceContext =
                            ThrottleContextFactory.createThrottleContext(ThrottleConstants.ROLE_BASE, throttleConfiguration);
                    throttle.addThrottleContext(RESOURCE_THROTTLE_KEY, resourceContext);
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

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private void logMessageDetails(MessageContext messageContext) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.gateway.handlers
        String applicationName = (String) messageContext.getProperty("APPLICATION_NAME");
        String endUserName = (String) messageContext.getProperty("END_USER_NAME");

        //Do not change this log format since its using by some external apps
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String logMessage = "";
        if (applicationName != null) {
            logMessage = " belonging to appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " userName=" + endUserName;
        }
        String logID = axisMC.getOptions().getMessageId();
        if (logID != null) {
            logMessage = logMessage + " transactionId=" + logID;
        }
        try {
            String userAgent = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("User-Agent");
            if (userAgent != null) {
                logMessage = logMessage + " with userAgent=" + userAgent;
            }
        } catch (Exception e) {
            log.debug("Error while getting User Agent for request");
        }

        long reqIncomingTimestamp = Long.parseLong((String) ((Axis2MessageContext) messageContext).
                getAxis2MessageContext().getProperty("wso2statistics.request.received.time"));
        Date incomingReqTime = new Date(reqIncomingTimestamp);
        if (incomingReqTime != null) {
            logMessage = logMessage + " at requestTime=" + incomingReqTime;
        }
        //If gateway is fronted by hardware load balancer client ip should retrieve from x forward for header
        String remoteIP = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("X-Forwarded-For");
        if (remoteIP == null) {
            remoteIP = (String) axisMC.getProperty("REMOTE_ADDR");
        }
        //null check before add it to log message
        if (remoteIP != null) {
            logMessage = logMessage + " from clientIP=" + remoteIP;
        }
        log.debug("Message throttled out Details:" + logMessage);
    }

}
