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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.subscription.GlobalPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.GlobalPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.core.Response;


public class GlobalPoliciesApiServiceImpl implements GlobalPoliciesApiService {

    private static final Log log = LogFactory.getLog(GlobalPoliciesApiServiceImpl.class);

    public Response globalPoliciesGet(String xWSO2Tenant, String policyName, MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving global policies. PolicyName: " + policyName + ", Tenant: " + xWSO2Tenant);
        }

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            if (APIConstants.CHAR_ASTERIX.equals(xWSO2Tenant) || APIConstants.ORG_ALL_QUERY_PARAM.equals(xWSO2Tenant)) {
                log.info("Retrieving all global policies across all tenants");
                return Response.ok().entity(SubscriptionValidationDataUtil.
                        fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                                getAllGlobalPolicies())).build();
            } else if (StringUtils.isNotEmpty(policyName)) {
                log.info("Retrieving global policy by name: " + policyName + " for tenant: " + xWSO2Tenant);
                List<GlobalPolicy> model = new ArrayList<>();
                GlobalPolicy globalPolicy = subscriptionValidationDAO.
                        getGlobalPolicyByNameForTenant(policyName, xWSO2Tenant);
                if (globalPolicy != null) {
                    model.add(globalPolicy);
                }
                return Response.ok().entity(SubscriptionValidationDataUtil.
                        fromGlobalPolicyToGlobalPolicyListDTO(model)).build();

            } else {
                log.info("Retrieving all global policies for tenant: " + xWSO2Tenant);
                return Response.ok().entity(SubscriptionValidationDataUtil.
                        fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                                getAllGlobalPolicies(xWSO2Tenant))).build();
            }
        } else {
            if (StringUtils.isNotEmpty(policyName)) {
                log.warn("X-WSO2-Tenant header is missing for global policy name request: " + policyName);
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                        "X-WSo2-Tenant header is missing.").build();
            }
        }
        log.info("Retrieving all global policies for default tenant");
        return Response.ok().entity(SubscriptionValidationDataUtil.
                fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                        getAllGlobalPolicies())).build();
    }
}
