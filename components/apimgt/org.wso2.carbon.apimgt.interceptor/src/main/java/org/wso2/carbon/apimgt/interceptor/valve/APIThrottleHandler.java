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

package org.wso2.carbon.apimgt.interceptor.valve;

import javax.servlet.http.HttpServletRequest;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.interceptor.utils.ThrottleUtils;
import org.wso2.carbon.apimgt.interceptor.valve.internal.DataHolder;
import org.wso2.carbon.apimgt.core.throttle.ThrottleManager;
import org.wso2.carbon.apimgt.core.APIManagerConstants;
import org.apache.synapse.commons.throttle.core.ConcurrentAccessController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;

public class APIThrottleHandler {
	
	private static final Log log = LogFactory.getLog(APIThrottleHandler.class);
	
	/** The Throttle object - holds all runtime and configuration data */
    private volatile Throttle throttle;
	
	/** Does this env. support clustering*/
    private boolean isClusteringEnable = false;
    
    /** ConcurrentAccessController - limit the remote callers concurrent access */
    private ConcurrentAccessController concurrentAccessController = null;
    
   /** The concurrent access control group id */
   private static final String id = "B";
   
   /** The property key that used when the ConcurrentAccessController
   look up from ConfigurationContext */
   private static final String key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + id + ThrottleConstants.CAC_SUFFIX;
   
   public boolean doThrottle(HttpServletRequest request, APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String accessToken) {
	   ThrottleManager throttleManager = new ThrottleManager(id, key);
		ConfigurationContext cc = DataHolder.getServerConfigContext();
		ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
       if (clusteringAgent != null && clusteringAgent.getStateManager() != null) {
           isClusteringEnable = true;
       }
        //check the availability of the ConcurrentAccessController
       //if this is a clustered environment
       if (isClusteringEnable) {
           concurrentAccessController = (ConcurrentAccessController) cc.getProperty(key);
       }
       initThrottle(cc);
       
       // perform concurrency throttling
       boolean canAccess = throttleManager.doThrottleByConcurrency(false, concurrentAccessController);
       
       if (canAccess) {
       	String remoteIP = request.getRemoteAddr();
       	canAccess = throttleManager.throttleByAccessRate(remoteIP, remoteIP, cc, isClusteringEnable, concurrentAccessController, throttle)
       			&& throttleManager.doRoleBasedAccessThrottling(isClusteringEnable, cc, apiKeyValidationInfoDTO, accessToken, throttle);
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
           return false;
       }
       return true;
   }
   
   private void handleException(String msg, ClusteringFault e) {
	   log.error(msg, e);
	}

   private void initThrottle(ConfigurationContext cc) {
		Object entryValue = null;
		if (throttle == null) {
			entryValue = ThrottleUtils.lookup(APIManagerConstants.APPLICATION_THROTTLE_POLICY_KEY);
		}
		if (isClusteringEnable && concurrentAccessController != null && throttle != null) {
           concurrentAccessController = null; // set null ,
           // because need to reload
       }
		if (throttle == null) {
			try {
				throttle = ThrottleFactory.createMediatorThrottle(
				        PolicyEngine.getPolicy((OMElement) entryValue));
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
				log.error("Error while initializing throttle handler", e);
			}
		}
		
	}
   
   public Throttle getThrottle() {
		return throttle;
	}

	public void setThrottle(Throttle throttle) {
		this.throttle = throttle;
	}

}
