/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApplicationKeyMappingsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.utils.InternalServiceDataUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApplicationKeyMappingsApiServiceImpl implements ApplicationKeyMappingsApiService {

    @Override
    public Response applicationKeyMappingsGet(String xWSO2Tenant, String consumerKey, MessageContext messageContext) {

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        String validatedTenantDomain = InternalServiceDataUtil.validateTenantDomain(xWSO2Tenant);

        if (StringUtils.isNotEmpty(consumerKey)) {
            if (InternalServiceDataUtil.isUserAuthorizedToTenant(validatedTenantDomain)) {
                ApplicationKeyMapping keyMapping = subscriptionValidationDAO.getApplicationKeyMapping(consumerKey);
                List<ApplicationKeyMapping> applicationKeyMappings = new ArrayList<>();
                if (keyMapping != null) {
                    applicationKeyMappings.add(keyMapping);
                }
                return Response.ok().entity(InternalServiceDataUtil.
                        fromApplicationKeyMappingToApplicationKeyMappingListDTO(applicationKeyMappings)).build();
            }
        }
        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            if (InternalServiceDataUtil.isUserAuthorizedToTenant(validatedTenantDomain)) {

                return Response.ok().entity(InternalServiceDataUtil.
                        fromApplicationKeyMappingToApplicationKeyMappingListDTO(subscriptionValidationDAO.
                                getAllApplicationKeyMappings(xWSO2Tenant))).build();
            }

        } else {
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(validatedTenantDomain) &&
                    InternalServiceDataUtil.isUserAuthorizedToTenant(validatedTenantDomain)) {
                return Response.ok().entity(InternalServiceDataUtil.
                        fromApplicationKeyMappingToApplicationKeyMappingListDTO(
                                subscriptionValidationDAO.getAllApplicationKeyMappings())).build();
            } else {
                InternalServiceDataUtil.handleUnauthorizedError();

            }
        }
        return null;
    }
}
