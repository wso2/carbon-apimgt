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
import org.wso2.carbon.apimgt.api.model.subscription.APIPolicy;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.utils.InternalServiceDataUtil;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class ApiPoliciesApiServiceImpl implements ApiPoliciesApiService {

        public Response apiPoliciesGet(String xWSO2Tenant, String policyName, MessageContext messageContext) {

        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();

        xWSO2Tenant = InternalServiceDataUtil.validateTenantDomain(xWSO2Tenant);
        if (InternalServiceDataUtil.isUserAuthorizedToTenant(xWSO2Tenant)) {
            if (StringUtils.isNotEmpty(xWSO2Tenant)) {
                if (StringUtils.isNotEmpty(policyName)) {
                    List<APIPolicy> model = new ArrayList<>();
                    APIPolicy apiPolicy = subscriptionValidationDAO.
                            getApiPolicyByNameForTenant(policyName, xWSO2Tenant);
                    if (apiPolicy != null) {
                        model.add(apiPolicy);
                    }
                    return Response.ok().entity(InternalServiceDataUtil.
                            fromApiPolicyToApiPolicyListDTO(model)).build();

                } else {
                    return Response.ok().entity(InternalServiceDataUtil.
                            fromApiPolicyToApiPolicyListDTO(subscriptionValidationDAO.
                                    getAllApiPolicies(xWSO2Tenant))).build();
                }
            } else {
                if (StringUtils.isNotEmpty(policyName)) {
                    return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                            "X-WSO2-Tenant header is missing.").build();
                }
            }
            return Response.ok().entity(InternalServiceDataUtil.
                    fromApiPolicyToApiPolicyListDTO(subscriptionValidationDAO.
                            getAllApiPolicies())).build();
        }
        return null;
    }
}
