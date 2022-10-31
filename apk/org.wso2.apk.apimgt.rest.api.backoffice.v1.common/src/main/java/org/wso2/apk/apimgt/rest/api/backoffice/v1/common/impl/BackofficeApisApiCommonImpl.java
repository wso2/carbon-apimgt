/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.APIMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

/**
 * Util class for BackofficeApisApiService related operations
 */
public class BackofficeApisApiCommonImpl {

    public static final String MESSAGE = "message";
    public static final String ERROR_WHILE_UPDATING_API = "Error while updating API : ";

    private BackofficeApisApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(BackofficeApisApiCommonImpl.class);
    private static final String HTTP_STATUS_LOG = "HTTP status ";
    private static final String AUDIT_ERROR = "Error while parsing the audit response";


    public static APIDTO getBackOfficeAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return getBackOfficeAPIByID(apiId, apiProvider, organization);
    }

    private static APIDTO getBackOfficeAPIByID(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return APIMappingUtil.fromAPItoDTO(api, apiProvider);
    }

}
