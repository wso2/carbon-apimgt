/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.ThrottlingPoliciesApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyListDTO;

import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for Publisher throttling policies related operations
 */
public class ThrottlingPoliciesApiServiceImpl implements ThrottlingPoliciesApiService {

    /**
     * Retrieves all the Tiers
     *
     * @param policyLevel tier level (api/application or resource)
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */
    @Override
    public Response getAllThrottlingPolicies(String policyLevel, Integer limit, Integer offset,
                                             String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        ThrottlingPolicyListDTO policyListDTO = ThrottlingPoliciesApiCommonImpl.getAllThrottlingPolicies(policyLevel,
                limit, offset);
        return Response.ok().entity(policyListDTO).build();
    }

    /**
     * Retrieves all the Tiers
     *
     * @param limit       max number of objects returns
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return Response object containing resulted tiers
     */

    @Override
    public Response getSubscriptionThrottlingPolicies(Integer limit, Integer offset, String ifNoneMatch,
                                                      MessageContext messageContext) throws APIManagementException {

        SubscriptionPolicyListDTO subscriptionPolicyListDTO = ThrottlingPoliciesApiCommonImpl.
                getSubscriptionThrottlingPolicies(limit, offset);
        return Response.ok().entity(subscriptionPolicyListDTO).build();
    }

    /**
     * Returns the matched throttling policy to the given policy name
     *
     * @param policyName  name of the throttling policy
     * @param policyLevel throttling policy level (subscription or api)
     * @param ifNoneMatch If-None-Match header value
     * @return ThrottlingPolicyDTO matched to the given throttling policy name
     */
    @Override
    public Response getThrottlingPolicyByName(String policyName, String policyLevel, String ifNoneMatch,
                                              MessageContext messageContext) throws APIManagementException {

        ThrottlingPolicyDTO throttlingPolicy = ThrottlingPoliciesApiCommonImpl.getThrottlingPolicyByName(policyName,
                policyLevel);
        return Response.ok().entity(throttlingPolicy).build();

    }

}
