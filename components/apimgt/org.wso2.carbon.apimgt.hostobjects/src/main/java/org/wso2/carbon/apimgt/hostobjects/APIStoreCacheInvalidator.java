/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.hostobjects;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.CacheInvalidationHolder;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Runs periodically notifying the Gateway about changed API Subscriptions & Applications.
 * CacheInvalidationTask will create a list of {@code APIKeyMapping} elements,
 * corresponding to the APIs that were changed. Once sent to the Gateway,
 * Gateway will clear the cache entries associated with those APIs from it's cache.
 */
public class APIStoreCacheInvalidator {
    private static final Log log = LogFactory.getLog(APIStoreCacheInvalidator.class);
    private static final int MAXIMUM_MAPPINGS_TO_INVALIDATE = 1000;

    public APIStoreCacheInvalidator() {

        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(2,
                                                 new ThreadFactory() {
                                                     @Override
                                                     public Thread newThread(Runnable r) {
                                                         Thread t = new Thread(r);
                                                         t.setName("CacheInvalidationTask" +
                                                                   " - Store");
                                                         return t;
                                                     }
                                                 });


        // Scheduling the task to trigger with 500ms time gap.
        executor.scheduleAtFixedRate(new CacheInvalidationTask(), 500,
                                     500, TimeUnit.MILLISECONDS);

    }


    /**
     * CacheInvalidator task will read the List of changed APIs from {@code CacheInvalidationHolder},
     * then create APIKeyMappings corresponding to the changed APIs and then call the Gateway to invalidate cache
     * entries.
     */
    private class CacheInvalidationTask implements Runnable {

        @Override
        public void run() {
            Set<APIIdentifier> identifiers = CacheInvalidationHolder.getInstance().getApiKeyMappings();

            // Task will continue only if any APIs have been changed from the last time.
            if (identifiers.size() > 0) {
                log.debug("Remaining " + identifiers.size() + " elements to invalidate.");
                int mappingsAdded = 0;
                Set<APIIdentifier> clonedIdentifiers = new HashSet<APIIdentifier>();

                // Creating local copy of the Changed APIs.
                for (APIIdentifier identifier : identifiers) {
                    clonedIdentifiers.add(identifier);
                    mappingsAdded++;
                    if (mappingsAdded > MAXIMUM_MAPPINGS_TO_INVALIDATE) {
                        break;
                    }

                }

                // Removing cloned Identifiers from the main set.
                identifiers.removeAll(clonedIdentifiers);
                APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService();

                List<APIKeyMapping> mappings = new ArrayList<APIKeyMapping>(clonedIdentifiers.size());

                // Creating corresponding APIKeyMapping elements using APIIdentifiers.
                for(APIIdentifier identifier: clonedIdentifiers){
                    API api = getAPI(identifier);
                    if(api != null){
                        APIKeyMapping mapping = new APIKeyMapping();
                        mapping.setContext(api.getContext());
                        mapping.setApiVersion(identifier.getVersion());
                        if(identifier.getApplicationId() != null) {
                            mapping.setApplicationId(identifier.getApplicationId());
                        }
                        mapping.setDomain(MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier
                                                                                                                  .getProviderName()))) ;
                        mappings.add(mapping);
                    }
                }

                if (configurationService != null) {
                    Map<String, Environment> gatewayEnvs = configurationService.getAPIManagerConfiguration()
                            .getApiGatewayEnvironments();

                    // Read All the Gateway Environments available.
                    for (Environment environment : gatewayEnvs.values()) {
                        try {
                            APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);

                            // Invalidate caches on each different Environment.
                            client.invalidateKeys(mappings);

                        }
                        // We are simply logging the Exception, because we have to continue pushing changes to other
                        // Gateways.
                        catch (AxisFault axisFault) {
                            log.error("Error occurred while invalidating cache at Environment :" + environment.getName());
                        }
                    }
                }

            }
        }


        /**
         * Helper method for obtaining API when given the APIIdentifier. Since this directly obtains UserRegistry
         * of the API Provider, had to start Tenantflow and sent tenant, username details in the {@code
         * CarbonContext}. For these reasons it might be best if this is not used as a common Util method.
         * user regist
         * @param identifier ID of the API that needs to be fetched.
         * @return API object.
         */
        private API getAPI(APIIdentifier identifier){
            String apiPath = APIUtil.getAPIPath(identifier);
            String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));

                String tenantUserName = MultitenantUtils.getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);


                // Mimicking as the Provider of the API.
                PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantUserName);


                Registry registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                    APIConstants.API_KEY);
                Resource apiResource = registry.get(apiPath);
                String artifactId = apiResource.getUUID();
                if (artifactId == null) {
                    log.debug("Registry Artifact for API "+ identifier.getApiName() +" is null");
                    return null;
                }
                GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
                return APIUtil.getAPIForPublishing(apiArtifact, registry);

            }
            // Exceptions aren't thrown, because it will break the loop from continuing. We'll simply log the
            // exception and continue with the work.
            catch (RegistryException e) {
                log.error("Error occurred while reading Artifact for "+identifier.getApiName(),e);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error("Error occurred while getting Tenant ID for "+ identifier.getProviderName(),e);
            } catch (APIManagementException e) {
                log.error("Error occurred while fetching API "+identifier.getApiName(),e);
            }finally {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(baseUser);
            }
            return null;
        }

    }
}
