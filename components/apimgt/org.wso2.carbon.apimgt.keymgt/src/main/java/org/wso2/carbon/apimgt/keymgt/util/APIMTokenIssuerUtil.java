/*
 *Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class APIMTokenIssuerUtil {

    public static JwtTokenInfoDTO getJwtTokenInfoDTO(Application application, OAuthTokenReqMessageContext tokReqMsgCtx)
            throws APIManagementException {

        String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
        String userName = tokReqMsgCtx.getAuthorizedUser().toFullQualifiedUsername();
        String applicationName = application.getName();

        String appOwner = application.getOwner();
        APISubscriptionInfoDTO[] apis = ApiMgtDAO.getInstance()
                .getSubscribedAPIsForAnApp(appOwner, applicationName);

        JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
        jwtTokenInfoDTO.setSubscriber("sub");
        jwtTokenInfoDTO.setEndUserName(userName);
        jwtTokenInfoDTO.setContentAware(true);

        Set<String> subscriptionTiers = new HashSet<>();
        List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
        for (APISubscriptionInfoDTO api : apis) {
            subscriptionTiers.add(api.getSubscriptionTier());

            SubscribedApiDTO subscribedApiDTO = new SubscribedApiDTO();
            subscribedApiDTO.setName(api.getApiName());
            subscribedApiDTO.setContext(api.getContext());
            subscribedApiDTO.setVersion(api.getVersion());
            subscribedApiDTO.setPublisher(api.getProviderId());
            subscribedApiDTO.setSubscriptionTier(api.getSubscriptionTier());
            subscribedApiDTO.setSubscriberTenantDomain(tenantDomain);
            subscribedApiDTOList.add(subscribedApiDTO);
        }
        jwtTokenInfoDTO.setSubscribedApiDTOList(subscribedApiDTOList);

        SubscriptionPolicy[] subscriptionPolicies = ApiMgtDAO.getInstance()
                .getSubscriptionPolicies(subscriptionTiers.toArray(new String[0]), APIUtil.getTenantId(appOwner));

        Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<>();
        for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies) {
            SubscriptionPolicyDTO subscriptionPolicyDTO = new SubscriptionPolicyDTO();
            subscriptionPolicyDTO.setSpikeArrestLimit(subscriptionPolicy.getRateLimitCount());
            subscriptionPolicyDTO.setSpikeArrestUnit(subscriptionPolicy.getRateLimitTimeUnit());
            subscriptionPolicyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
            subscriptionPolicyDTOList.put(subscriptionPolicy.getPolicyName(), subscriptionPolicyDTO);
        }
        jwtTokenInfoDTO.setSubscriptionPolicyDTOList(subscriptionPolicyDTOList);

        return jwtTokenInfoDTO;
    }
}
