/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

import java.io.File;

/**
 * This class represents the API Manager utility for governance
 */
public class APIMUtil {

    public boolean isAPIExist(String apiId) throws GovernanceException {
        try {
            ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(apiId);
        } catch (APIManagementException e) {
            throw new GovernanceException("Failed to find API with ID: " + apiId, e);
        }
        return true;
    }

//    public File getAPIProject(String apiId, String organization) throws GovernanceException {
//
//        try {
//            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
//            API api = apiProvider.getAPIbyUUID(apiId, organization);
//            api.setUuid(apiId);
//            apiIdentifier.setUuid(apiId);
//            APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, true, apiProvider);
//            File apiProject = ExportUtils.exportApi(apiProvider, apiIdentifier, apiDtoToReturn, api, userName,
//                    ExportFormat.YAML,
//                    true, true, StringUtils.EMPTY, organization);
//        } catch (APIManagementException e) {
//            throw new RuntimeException(e);
//        } catch (APIImportExportException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
