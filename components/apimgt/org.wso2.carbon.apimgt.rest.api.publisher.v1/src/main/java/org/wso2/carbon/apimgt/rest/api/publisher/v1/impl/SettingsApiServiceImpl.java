/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;

import org.wso2.carbon.apimgt.impl.restapi.publisher.SettingsApiServiceImplUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response getSettings(MessageContext messageContext){

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingstoDTO(isUserAvailable, organization);
            settingsDTO.setScopes(getScopeList());
            settingsDTO.setGatewayTypes(APIUtil.getGatewayTypes());
            settingsDTO.setCustomProperties(APIUtil.getCustomProperties(username));
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException | IOException e) {
            String errorMessage = "Error while retrieving Publisher Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
    /**
     * This method returns the scope list from the publisher-api.yaml
     * @return  List<String> scope list
     * @throws APIManagementException
     */
    private List<String> getScopeList() throws APIManagementException {

        String definition = null;
        try {
            definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/publisher-api.yaml"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error while reading the swagger definition", e);
        }
        return SettingsApiServiceImplUtil.getScopeListForSwaggerDefinition(definition);
    }
}
