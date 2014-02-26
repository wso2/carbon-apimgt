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
package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.ArrayList;

public class ThriftAPIDataStore implements APIKeyDataStore{

    private static final ThriftKeyValidatorClientPool clientPool =
            ThriftKeyValidatorClientPool.getInstance();
    /**
     * Validate the given API key for the specified API context and version.
     *
     * @param context    Context of an API
     * @param apiVersion A valid version of the API
     * @param apiKey     An API key string - Not necessarily a valid key
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException
     *          on error
     */
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey, String clientDomain)
            throws APISecurityException {
        ThriftKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAPIKeyData(context, apiVersion, apiKey, APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN, clientDomain);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Validate the given API key for the specified API context and version.
     *
     * @param context    Context of an API
     * @param apiVersion A valid version of the API
     * @param apiKey     An API key string - Not necessarily a valid key
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException
     *          on error
     */
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
                                                 String requiredAuthenticationLevel, String clientDomain) throws APISecurityException {
        ThriftKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAPIKeyData(context, apiVersion, apiKey,requiredAuthenticationLevel, clientDomain);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion)
            throws APISecurityException {
        ThriftKeyValidatorClient client = null;
        try {
            client = clientPool.get();
            return client.getAllURITemplates(context, apiVersion);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.release(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Clean up any resources allocated to this API key data store instance.
     */
    public void cleanup() {
        
    }
}
