/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIClientGenerationManager;
import org.wso2.carbon.apimgt.rest.api.store.v1.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

public class SdkGenApiServiceImpl implements SdkGenApiService {

    private static final Log log = LogFactory.getLog(SdkGenApiServiceImpl.class);

    /**
     * Rest API implementation to get the supported sdk languages
    */
    @Override
    public Response sdkGenLanguagesGet(String organizationId, MessageContext messageContext) {

        APIClientGenerationManager apiClientGenerationManager = new APIClientGenerationManager();
        String supportedLanguages = apiClientGenerationManager.getSupportedSDKLanguages();

        if (StringUtils.isNotEmpty(supportedLanguages)) {
            // Split the string with ',' and add them to a list.
            List<String> lanuagesList = Arrays.stream(supportedLanguages.split(",")).collect(Collectors.toList());
            return Response.ok().entity(lanuagesList).build();
        }
        String message = "Could not find the supported sdk languages";
        RestApiUtil.handleInternalServerError(message, log);
        return null;
    }
}
