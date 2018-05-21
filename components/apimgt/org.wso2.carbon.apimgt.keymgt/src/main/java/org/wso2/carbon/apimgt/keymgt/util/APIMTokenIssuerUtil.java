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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIMTokenIssuerUtil {

    public static JwtTokenInfoDTO getJwtTokenInfoDTO(OAuthAppDO oAuthAppDO) {

        String tenantDomain = OAuth2Util.getTenantDomainOfOauthApp(oAuthAppDO);
        String userName = oAuthAppDO.getUser().getUserName();

        String tenantedUserName;
        if (StringUtils.isEmpty(tenantDomain) || tenantDomain.equals("carbon.super")) {
            tenantedUserName = userName;
        } else {

            tenantedUserName = userName + "@" + tenantDomain;
        }
        APISubscriptionInfoDTO[] apis = new APISubscriptionInfoDTO[0];
        try {
            apis = ApiMgtDAO.getInstance().getSubscribedAPIsOfUserWithSubscriptionInfo(tenantedUserName);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
        jwtTokenInfoDTO.setSubscriber("sub");
        jwtTokenInfoDTO.setIssuedTime(13123131);
        jwtTokenInfoDTO.setExpirationTime(12312311);
        jwtTokenInfoDTO.setEndUserName(userName);
        jwtTokenInfoDTO.setContentAware(true);
        jwtTokenInfoDTO.setAudience(oAuthAppDO.getAudiences());

        List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
        for (APISubscriptionInfoDTO api : apis)

        {
            SubscribedApiDTO subscribedApiDTO = new SubscribedApiDTO();
            subscribedApiDTO.setName(api.getApiName());
            subscribedApiDTO.setContext(api.getContext());
            subscribedApiDTO.setVersion(api.getVersion());
            subscribedApiDTO.setPublisher("publisher");
            subscribedApiDTO.setSubscriber("subscriber");
            subscribedApiDTO.setSubscriptionTier(api.getSubscriptionTier());
            subscribedApiDTO.setSubscriberTenantDomain(tenantDomain);
            subscribedApiDTOList.add(subscribedApiDTO);
        }
        jwtTokenInfoDTO.setSubscribedApiDTOList(subscribedApiDTOList);

        Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<String, SubscriptionPolicyDTO>();

        SubscriptionPolicyDTO subscriptionPolicyDTO1 = new SubscriptionPolicyDTO();
        subscriptionPolicyDTO1.setSpikeArrestLimit("spike");
        subscriptionPolicyDTO1.setSpikeArrestUnit("unit");
        subscriptionPolicyDTO1.setStopOnQuotaReach(true);

        subscriptionPolicyDTOList.put("Gold", subscriptionPolicyDTO1);
        SubscriptionPolicyDTO subscriptionPolicyDTO2 = new SubscriptionPolicyDTO();
        subscriptionPolicyDTO2.setSpikeArrestLimit("spike");
        subscriptionPolicyDTO2.setSpikeArrestUnit("unit");
        subscriptionPolicyDTO2.setStopOnQuotaReach(true);
        subscriptionPolicyDTOList.put("Bronze", subscriptionPolicyDTO1);

        jwtTokenInfoDTO.setSubscriptionPolicyDTOList(subscriptionPolicyDTOList);
        return jwtTokenInfoDTO;
    }
}
