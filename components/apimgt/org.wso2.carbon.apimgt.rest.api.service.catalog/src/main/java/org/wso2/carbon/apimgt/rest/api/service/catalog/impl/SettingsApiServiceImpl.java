/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.service.catalog.*;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiService.class);

    public Response getSettings(MessageContext messageContext) {
        try {
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingsToDTO();
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Service Catalog Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
