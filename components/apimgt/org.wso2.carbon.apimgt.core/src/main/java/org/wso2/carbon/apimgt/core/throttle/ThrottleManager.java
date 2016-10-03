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

package org.wso2.carbon.apimgt.core.throttle;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.AccessRateController;
import org.apache.synapse.commons.throttle.core.ConcurrentAccessController;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConfiguration;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleException;

public class ThrottleManager {
	
	private static final Log log = LogFactory.getLog(ThrottleManager.class);
	
	/** Access rate controller - limit the remote caller access*/
    private AccessRateController accessController;

    private RoleBasedAccessRateController roleBasedAccessController;
    
    private RoleBasedAccessRateController applicationRoleBasedAccessController;

    private String id;
    
    /** The property key that used when the ConcurrentAccessController
    look up from ConfigurationContext */
    private String key;
    
    public static final String APP_THROTTLE_CONTEXT_PREFIX = "APP_THROTTLE_CONTEXT_";
    
    private static final Object lock = new Object();
    
    public ThrottleManager(String id, String key) {
    	this.id = id;
    	this.key = key;
    	this.accessController = new AccessRateController();
        this.roleBasedAccessController = new RoleBasedAccessRateController();
        this.applicationRoleBasedAccessController = new RoleBasedAccessRateController();
    }
	
	public boolean doThrottleByConcurrency(boolean isResponse, ConcurrentAccessController concurrentAccessController) {
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
                    log.debug("Concurrency Throttle: Access " + (canAccess ? "allowed" : "denied") + " :: " + available
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

    public boolean throttleByAccessRate(String remoteIP, String domainName, ConfigurationContext cc, boolean isClusteringEnable,
                                        ConcurrentAccessController concurrentAccessController, Throttle throttle) {
        String callerId = null;
        boolean canAccess = true;
        //Using remote caller domain name , If there is a throttle configuration for
        // this domain name ,then throttling will occur according to that configuration
        if (domainName != null) {
            // do the domain based throttling
            if (log.isTraceEnabled()) {
                log.trace("The Domain Name of the caller is :" + domainName);
            }
            // loads the DomainBasedThrottleContext
            ThrottleContext context = throttle.getThrottleContext(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
            if (context != null) {
                //loads the DomainBasedThrottleConfiguration
                ThrottleConfiguration config = context.getThrottleConfiguration();
                if (config != null) {
                    //checks the availability of a policy configuration for  this domain name
                    callerId = config.getConfigurationKeyOfCaller(domainName);
                    if (callerId != null) {  // there is configuration for this domain name

                        context.setThrottleId(id);
                        try {
                            //Checks for access state
                            AccessInformation accessInformation
                                    = accessController.canAccess(context, callerId, ThrottleConstants.DOMAIN_BASE);
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
                    ThrottleContext context = throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY);
                    if (context != null) {
                        //Loads the IPBasedThrottleConfiguration
                        ThrottleConfiguration config = context.getThrottleConfiguration();
                        if (config != null) {
                            //Checks the availability of a policy configuration for  this ip
                            callerId = config.getConfigurationKeyOfCaller(remoteIP);
                            if (callerId != null) {   // there is configuration for this ip

                                context.setThrottleId(id);
                                //Checks access state
                                AccessInformation accessInformation = accessController.canAccess(context, callerId,
                                                                                                 ThrottleConstants.IP_BASE);

                                canAccess = accessInformation.isAccessAllowed();
                                if (log.isDebugEnabled()) {
                                    log.debug("Access " + (canAccess ? "allowed" : "denied") + " for IP : " + remoteIP);
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
	
	public boolean doRoleBasedAccessThrottling(boolean isClusteringEnable, ConfigurationContext cc, APIKeyValidationInfoDTO apiKeyValidationInfoDTO, 
			String accessToken, Throttle throttle) {
		boolean canAccess = true;
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
        String roleID;
        String applicationId;
        String applicationTier;
        
        if (apiKeyValidationInfoDTO != null) {
        	roleID = apiKeyValidationInfoDTO.getTier();
        	applicationId = apiKeyValidationInfoDTO.getApplicationId();
        	applicationTier = apiKeyValidationInfoDTO.getApplicationTier();
        	if (accessToken == null || roleID == null) {
        		if(log.isDebugEnabled()) {
        			log.warn("No consumer key or role information found on the request - " +
                        "Throttling not applied");
        		}
                return true;
            } else if (APIConstants.UNLIMITED_TIER.equals(roleID) &&
                       APIConstants.UNLIMITED_TIER.equals(applicationTier)) {
                return true;
            }
        } else {
            log.warn("No authentication context information found on the request - " +
                    "Throttling not applied");
            return true;
        }
         /*Domain name based throttling
        check whether a configuration has been defined for this role name or not
        loads the ThrottleContext */
        ThrottleContext context = throttle.getThrottleContext(
                ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
        if (context == null) {
            log.warn("Unable to load throttle context");
            return true;
        }
        //Loads the ThrottleConfiguration
        ThrottleConfiguration config = context.getThrottleConfiguration();
        if (config != null) {

            String applicationRoleId = null;
            //If an application level tier has been specified and it is not 'Unlimited'
            if(applicationTier != null && !APIConstants.UNLIMITED_TIER.equals(applicationTier)){
                //Get the configuration role of the application
                //applicationRoleId = config.getConfigurationKeyOfCaller(applicationTier);
                applicationRoleId = applicationTier;
            }

            AccessInformation info = null;
            //If application level throttling is applied
            if(applicationRoleId != null){

                ThrottleContext applicationThrottleContext = getApplicationThrottleContext(cc, applicationId, throttle);
                applicationThrottleContext.setThrottleId(id);
                //First throttle by application
                try {
                    info = applicationRoleBasedAccessController.canAccess(applicationThrottleContext, applicationId, applicationRoleId);
                    if(log.isDebugEnabled()){
                        log.debug("Throttle by Application " + applicationId);
                        log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
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
                    return canAccess;
                }
            }

            //If API Level throttling tier is Unlimited
            if (APIConstants.UNLIMITED_TIER.equals(roleID)) {
                return true;
            }

            //check for configuration role of the caller
            String consumerRoleID = config.getConfigurationKeyOfCaller(roleID);
            if (consumerRoleID != null) {
                context.setThrottleId(id);

                try {
                    //If the application has not been subscribed to the Unlimited Tier and
                    //if application level throttling has passed
                    if(!APIConstants.UNLIMITED_TIER.equals(roleID) &&
                       (info == null || info.isAccessAllowed())){
                         //Throttle by access token
                         info = roleBasedAccessController.canAccess(context, accessToken, consumerRoleID);
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
		return canAccess;
	}
	
	
	private static ThrottleContext getApplicationThrottleContext(ConfigurationContext cc, String applicationId, Throttle throttle) {
		synchronized (lock) {
            Object throttleContext = cc.getProperty(APP_THROTTLE_CONTEXT_PREFIX + applicationId);
            if(throttleContext == null){
            	ThrottleContext context = throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                cc.setProperty(APP_THROTTLE_CONTEXT_PREFIX + applicationId, context);
                return context;
            }
            return (ThrottleContext)throttleContext;
        }
	}
		
	private void handleException(String msg, Exception e) {
        log.error(msg, e);
    }

}
