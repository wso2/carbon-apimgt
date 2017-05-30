package org.wso2.carbon.apimgt.core.impl;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.core.api.ThrottlePolicyDeploymentManager;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.HttpResponse;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.template.ThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.ThrottleConstants;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * This class deploy the generated policy configuration to DAS using it's rest API
 */
public class ThrottlePolicyDeploymentManagerImpl implements ThrottlePolicyDeploymentManager {
    private static final Log log = LogFactory.getLog(ThrottlePolicyDeploymentManagerImpl.class);
    private ThrottlePolicyTemplateBuilder templateBuilder;

    public ThrottlePolicyDeploymentManagerImpl() {
        templateBuilder = new ThrottlePolicyTemplateBuilder();
    }

    @Override
    public boolean deployPolicy(String policyLevel, Policy policy) throws APIManagementException {

        String config = null;
        try {
            if (policy instanceof APIPolicy) {
                config = templateBuilder.getThrottlePolicyForAPILevelDefault((APIPolicy) policy);
            } else if (policy instanceof ApplicationPolicy) {
                config = templateBuilder.getThrottlePolicyForAppLevel((ApplicationPolicy) policy);
            } else if (policy instanceof SubscriptionPolicy) {
                config = templateBuilder.getThrottlePolicyForSubscriptionLevel((SubscriptionPolicy) policy);
            } else if (policy instanceof GlobalPolicy) {
                config = templateBuilder.getThrottlePolicyForGlobalLevel((GlobalPolicy) policy);
            }

            return this.deployPolicy(config);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error occurred while deploying throttle policies", e,
                    ExceptionCodes.THROTTLE_TEMPLATE_EXCEPTION);
        }
    }

    @Override
    public boolean unDeployPolicy(String policyLevel, Policy apiPolicy) throws APIManagementException {
        //todo implement the content
        return unDeployPolicy(null);
    }

    @Override
    public boolean updatePolicy(String policyLevel, Policy apiPolicy) throws APIManagementException {
        //todo implement the content
        return updatePolicy(null);
    }

    /**
     * Deploy policy using DAS rest API
     *
     * @param policy policy config
     * @return status of the deployment
     */
    private boolean deployPolicy(String policy) throws APIManagementException, URISyntaxException {
        String getDasRestApiUrl = APIMConfigurationService.getInstance().getApimConfigurations()
                .getAnalyticsConfigurations().getDasServerURL()
                + ThrottleConstants.DAS_REST_API_PATH_ARTIFACT_DEPLOY;
        URI uri = new URI(getDasRestApiUrl);
        HttpResponse response = new RestCallUtilImpl()
                .postRequest(uri, null, null, Entity.text(policy), MediaType.TEXT_PLAIN_TYPE);
        boolean isSuccess = (response.getResponseCode() == 200);
        if (isSuccess) {
            JSONObject g = new JSONObject(response.getResults());
            int statusCode = g.getInt("code");
            if (statusCode == 4 || statusCode == 1) {
                return true;
            } else {
                log.error("Unknown error occurred while deploying policies with msg '" + g.toString() + "'");
                return false;
            }
        } else {
            log.error("Error occurred while deploying policies with HTTP code :" + response.getResponseCode());
            return false;
        }
    }

    /**
     * UnDeploy policy using DAS rest API
     *
     * @param policy policy config
     * @return status of the UnDeployment
     */
    private boolean unDeployPolicy(String policy) {
        //todo implement the content
        return false;
    }

    /**
     * Update policy using DAS rest API
     *
     * @param policy policy config
     * @return status of the update
     */
    private boolean updatePolicy(String policy) {
        //todo implement the content
        return false;
    }
}
