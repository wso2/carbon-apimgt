/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.store.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.ThrottlingPoliciesServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.indexing.indexer.DocumentIndexer.log;

public class ThrottlingPoliciesApiServiceImpl implements ThrottlingPoliciesApiService {

    @Override
    public Response throttlingPoliciesPolicyLevelGet(
            String policyLevel, Integer limit, Integer offset, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) {
        String organization = RestApiUtil.getOrganization(messageContext);
        ThrottlingPolicyListDTO tierListDTO = ThrottlingPoliciesServiceImpl.
                throttlingPoliciesPolicyLevelGet(policyLevel, limit, offset, organization);
        return Response.ok().entity(tierListDTO).build();
    }

    @Override
    public Response throttlingPoliciesPolicyLevelPolicyIdGet(String policyId, String policyLevel, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        String organization = RestApiUtil.getOrganization(messageContext);

        if (StringUtils.isBlank(policyLevel)) {
            RestApiUtil.handleBadRequest("policyLevel cannot be empty", log);
        }
        ThrottlingPolicyDTO throttlePoliciesDto = ThrottlingPoliciesServiceImpl.
                throttlingPoliciesPolicyLevelPolicyIdGet(policyId, policyLevel, organization);
        return Response.ok().entity(throttlePoliciesDto).build();
    }
}
