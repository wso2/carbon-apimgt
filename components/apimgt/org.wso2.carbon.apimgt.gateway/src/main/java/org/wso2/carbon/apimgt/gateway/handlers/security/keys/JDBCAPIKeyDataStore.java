/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

/**
 * A JDBC interface for the API key data store. This implementation directly
 * interacts with the API Manager database to validate and authenticate API
 * keys.
 */
public class JDBCAPIKeyDataStore implements APIKeyDataStore {

    private ApiMgtDAO dao;

    public JDBCAPIKeyDataStore() throws APISecurityException {
        dao = new ApiMgtDAO();
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String clientDomain) throws APISecurityException {
        try {
            return dao.validateKey(context, apiVersion, apiKey, APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        } catch (APIManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while looking up API key data in the database", e);
        }
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey,String requiredAuthenticationLevel, String clientDomain) throws APISecurityException {
        try {
            return dao.validateKey(context, apiVersion, apiKey,requiredAuthenticationLevel);
        } catch (APIManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while looking up API key data in the database", e);
        }
    }
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException {
        try {
            return ApiMgtDAO.getAllURITemplates(context, apiVersion);
        } catch (APIManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while looking up API resource URI templates in the database", e);
        }
    }

    public void cleanup() {

    }
}
