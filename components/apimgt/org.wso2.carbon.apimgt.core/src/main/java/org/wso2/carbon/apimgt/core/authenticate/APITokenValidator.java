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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;


public class APITokenValidator {

    private static final Log log = LogFactory.getLog(APITokenValidator.class);

    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,
                                               String requiredAuthenticationLevel) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating API key for context: " + context + ", version: " + version);
        }
        
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            log.info("API key validation initiated for context: " + context);
            return null;
        } catch (Exception e) {
            log.error("Error occurred while validating API key for context: " + context, e);
            throw new APIManagementException("API key validation failed", e);
        }
    }
}