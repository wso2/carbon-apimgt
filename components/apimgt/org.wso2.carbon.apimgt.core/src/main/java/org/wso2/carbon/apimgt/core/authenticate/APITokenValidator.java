/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.core.authenticate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;


public class APITokenValidator {

    private static final Log log = LogFactory.getLog(APITokenValidator.class);

    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken, String requiredAuthenticationLevel,
                                               String clientDomain) throws APIManagementException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        // first check whether client domain is authorized
        if (accessToken != null && ApiMgtDAO.findConsumerKeyFromAccessToken(accessToken) != null &&
                ApiMgtDAO.isDomainRestricted(accessToken, clientDomain)) {
            String authorizedDomains = ApiMgtDAO.getAuthorizedDomains(accessToken);
            log.error("Unauthorized client domain :" + clientDomain +
                    ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
            throw new APIManagementException("Unauthorized client domain :" + clientDomain +
                    ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
        }
        return apiMgtDAO.validateKey(context, version, accessToken, requiredAuthenticationLevel);
    }

    public static String getAPIManagerClientDomainHeader() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        return config.getFirstProperty(APIConstants.API_GATEWAY_CLIENT_DOMAIN_HEADER);
    }
}