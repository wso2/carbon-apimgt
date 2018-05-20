/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.keymgt.token.APIMJWTGenerator;
import org.wso2.carbon.apimgt.keymgt.token.JWTGenerator;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OauthTokenIssuerImpl;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIMTokenIssuer extends OauthTokenIssuerImpl {

    private static final Log log = LogFactory.getLog(APIMTokenIssuer.class);

    @Override public String accessToken(OAuthTokenReqMessageContext tokReqMsgCtx) throws OAuthSystemException {

        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        try {
            OAuthAppDO oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
            String tenantDomain = OAuth2Util.getTenantDomainOfOauthApp(oAuthAppDO);
            String userName = oAuthAppDO.getUser().getUserName();
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            ServiceProvider serviceProvider = applicationManagementService
                    .getServiceProvider(oAuthAppDO.getApplicationName(), tenantDomain);
            ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
            if (spProperties.length > 1) {
                ServiceProviderProperty property = spProperties[1];
                if (property.getName().equals(APIConstants.APP_TOKEN_TYPE) && property.getValue().equals("JWT")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Generating the JWT from API Manager");
                    }
                    String tenantedUserName = userName + "@" + tenantDomain;
                    APISubscriptionInfoDTO[] apis = ApiMgtDAO.getInstance()
                            .getSubscribedAPIsOfUserWithSubscriptionInfo(tenantedUserName);

                    JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
                    jwtTokenInfoDTO.setSubscriber("sub");
                    jwtTokenInfoDTO.setIssuedTime(13123131);
                    jwtTokenInfoDTO.setExpirationTime(12312311);
                    jwtTokenInfoDTO.setEndUserName(userName);
                    jwtTokenInfoDTO.setContentAware(true);

                    List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
                    for (APISubscriptionInfoDTO api : apis) {
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
                    APIMJWTGenerator apimjwtGenerator = new APIMJWTGenerator();
                    return apimjwtGenerator.generateJWT(jwtTokenInfoDTO);
                }
            }

        } catch (IdentityOAuth2Exception e) {
            e.printStackTrace();
        } catch (InvalidOAuthClientException e) {
            e.printStackTrace();
        } catch (IdentityApplicationManagementException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return super.accessToken(tokReqMsgCtx);
    }
}
