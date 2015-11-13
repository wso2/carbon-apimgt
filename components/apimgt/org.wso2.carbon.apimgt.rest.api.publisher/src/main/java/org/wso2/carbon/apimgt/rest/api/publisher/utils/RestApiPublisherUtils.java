/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

/**
 *  This class contains REST API Publisher related utility operations
 */
public class RestApiPublisherUtils {

    /**
     * check whether the specified API exists and the current logged in user has access to it
     *
     * @param apiId API identifier
     * @throws APIManagementException
     */
    public static void checkUserAccessAllowedForAPI(String apiId) throws APIManagementException {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //this is just to check whether the user has access to the api or the api exists. When it tries to retrieve 
        // the resource from the registry, it will fail with AuthorizationFailedException if user does not have enough
        // privileges.
        APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
    }
}
