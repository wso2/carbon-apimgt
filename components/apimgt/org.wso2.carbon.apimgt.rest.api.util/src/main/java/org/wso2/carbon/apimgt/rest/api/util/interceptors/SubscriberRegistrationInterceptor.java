/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class SubscriberRegistrationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(SubscriberRegistrationInterceptor.class);
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";

    public SubscriberRegistrationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    /**
     * Handles the incoming message after post authentication. Only used in Store REST API, to register a newly 
     * signed up store user who hasn't logged in to Store for the first time either via REST API or Store UI. 
     * This method will register the user as a subscriber 
     * (register in AM_SUBSCRIBER table, add the default application for subscriber etc.).
     *
     * @param message cxf message
     */
    @Override
    public void handleMessage(Message message) {
        String username = RestApiUtil.getLoggedInUsername();
        String groupId = RestApiUtil.getLoggedInUserGroupId();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            //takes a consumer object using the user set in thread local carbon context
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            Subscriber subscriber = apiConsumer.getSubscriber(username);
            if (subscriber == null) {
                try {
                    APIUtil.checkPermission(username, APIConstants.Permissions.API_SUBSCRIBE);
                } catch (APIManagementException e) {
                    // When user does not have subscribe permission we will log it and continue flow.
                    // This happens when user tries to access anonymous apis although he does not have subscribe 
                    // permission. It should be allowed.
                    if (logger.isDebugEnabled()) {
                        logger.debug("User " + username + " does not have subscribe permission", e);
                    }
                    return;
                }
                if (!SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
                    loadTenantRegistry();
                }
                apiConsumer.addSubscriber(username, groupId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Subscriber " + username + " added to AM_SUBSCRIBER database");
                }
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Unable to add the subscriber " + username, e, logger);
        }
    }

    private void loadTenantRegistry() throws APIManagementException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIUtil.loadTenantRegistry(tenantId);
            APIUtil.loadTenantAPIPolicy(tenantDomain, tenantId);
            APIUtil.loadTenantExternalStoreConfig(tenantId);
            APIUtil.loadTenantWorkFlowExtensions(tenantId);
            APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
            APIUtil.loadTenantConf(tenantId);

        } catch (RegistryException e) {
            throw new APIManagementException("Error occured while loading registry for tenant '" + tenantDomain + "'");
        }
    }

}
