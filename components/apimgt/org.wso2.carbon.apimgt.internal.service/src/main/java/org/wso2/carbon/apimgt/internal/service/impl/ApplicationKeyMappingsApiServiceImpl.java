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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApplicationKeyMappingsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApplicationKeyMappingsApiServiceImpl implements ApplicationKeyMappingsApiService {

    @Override
    public Response applicationKeyMappingsGet(String xWSO2Tenant, String consumerKey, String keymanager,
                                              MessageContext messageContext) throws APIManagementException {

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String organization = RestApiUtil.getOrganization(messageContext);

        if (StringUtils.isNotEmpty(consumerKey)) {
            ApplicationKeyMapping keyMapping = subscriptionValidationDAO.getApplicationKeyMapping(consumerKey,
                    keymanager, xWSO2Tenant);
            List<ApplicationKeyMapping> applicationKeyMappings = new ArrayList<>();
            if (keyMapping != null) {
                applicationKeyMappings.add(keyMapping);
            }
            return Response.ok().entity(SubscriptionValidationDataUtil.
                    fromApplicationKeyMappingToApplicationKeyMappingListDTO(applicationKeyMappings)).build();
        }
        if (StringUtils.isNotEmpty(organization) && !organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM))   {
            return Response.ok().entity(SubscriptionValidationDataUtil.
                    fromApplicationKeyMappingToApplicationKeyMappingListDTO(subscriptionValidationDAO.
                            getAllApplicationKeyMappingsByOrganization(organization))).build();
        } else if (StringUtils.isNotEmpty(organization) && organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM) &&
                xWSO2Tenant.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return Response.ok().entity(SubscriptionValidationDataUtil.
                    fromApplicationKeyMappingToApplicationKeyMappingListDTO(subscriptionValidationDAO.
                            getAllApplicationKeyMappings())).build();
        } else if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDataUtil.
                    fromApplicationKeyMappingToApplicationKeyMappingListDTO(subscriptionValidationDAO.
                            getAllApplicationKeyMappings(xWSO2Tenant))).build();

        }
        return null;
    }
}
