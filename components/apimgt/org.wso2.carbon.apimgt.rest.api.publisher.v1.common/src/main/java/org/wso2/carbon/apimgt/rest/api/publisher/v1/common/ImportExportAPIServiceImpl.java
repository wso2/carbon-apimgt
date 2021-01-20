/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.InputStream;

/**
 * Osgi Service implementation for import export API.
 */
@Component(
        name = "import.export.service.component",
        immediate = true,
        service = ImportExportAPI.class
)
public class ImportExportAPIServiceImpl implements ImportExportAPI {

    @Override
    public File exportAPI(String apiId, String name, String version, String providerName, boolean preserveStatus,
                          ExportFormat format, boolean preserveDocs, boolean preserveCredentials)
            throws APIManagementException,
            APIImportExportException {

        APIIdentifier apiIdentifier;
        APIDTO apiDtoToReturn;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        API api;

        // apiId == null means the path from the API Controller
        if (apiId == null) {
            // Validate API name, version and provider before exporting
            String provider = ExportUtils.validateExportParams(name, version, providerName);
            apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), name, version);
            api = apiProvider.getAPI(apiIdentifier);
            apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, preserveCredentials, null);
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api);
        }
        if (api != null) {
            return ExportUtils.exportApi(apiProvider, apiIdentifier, apiDtoToReturn, userName, format, preserveStatus,
                    preserveDocs);
        }
        return null;
    }

    @Override
    public File exportAPIProduct(String apiId, String name, String version, String providerName,
                                 ExportFormat format, boolean preserveStatus, boolean preserveDocs,
                                 boolean preserveCredentials)
            throws APIManagementException, APIImportExportException {

        APIProductIdentifier apiProductIdentifier;
        APIProductDTO apiProductDtoToReturn;
        APIProduct apiProduct;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(userName));

        if (apiId != null) {
            apiProductIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiId, tenantDomain);
            apiProduct = apiProvider.getAPIProductbyUUID(apiId, tenantDomain);
        } else {
            // Validate API name, version and provider before exporting
            String provider = ExportUtils.validateExportParams(name, version, providerName);
            apiProductIdentifier = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider), name, version);
            apiProduct = apiProvider.getAPIProduct(apiProductIdentifier);

        }
        if (apiProduct != null) {
            apiProductDtoToReturn = APIMappingUtil.fromAPIProducttoDTO(apiProduct);
            return ExportUtils
                    .exportApiProduct(apiProvider, apiProductIdentifier, apiProductDtoToReturn, userName, format,
                            preserveStatus, preserveDocs, preserveCredentials);
        }
        return null;

    }

    @Override
    public API importAPI(InputStream fileInputStream, Boolean preserveProvider, Boolean overwrite,
            String[] tokenScopes) throws APIManagementException {
        String extractedFolderPath;
        try {
            extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(fileInputStream);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
        return ImportUtils.importApi(extractedFolderPath, null, preserveProvider, overwrite, tokenScopes);
    }

    @Override public APIProduct importAPIProduct(InputStream fileInputStream, Boolean preserveProvider,
            Boolean overwriteAPIProduct, Boolean overwriteAPIs, Boolean importAPIs, String[] tokenScopes)
            throws APIManagementException {
        String extractedFolderPath;
        try {
            extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(fileInputStream);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
        return ImportUtils.importApiProduct(extractedFolderPath, preserveProvider, overwriteAPIProduct, overwriteAPIs,
                importAPIs, tokenScopes);
    }
}
