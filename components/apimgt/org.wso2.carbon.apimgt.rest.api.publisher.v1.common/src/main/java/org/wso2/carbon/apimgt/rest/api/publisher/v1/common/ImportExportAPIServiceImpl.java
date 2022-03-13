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

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
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
    public File exportAPI(String apiId, String name, String version, String revisionNum, String providerName,
            boolean preserveStatus, ExportFormat format, boolean preserveDocs, boolean preserveCredentials,
            boolean exportLatestRevision, String originalDevPortalUrl, String organization)
            throws APIManagementException, APIImportExportException {

        APIIdentifier apiIdentifier;
        APIDTO apiDtoToReturn;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        API api;
        String exportAPIUUID;

        // apiId == null means the path from the API Controller
        if (apiId == null) {
            // Validate API name, version and provider before exporting
            String provider = ExportUtils.validateExportParams(name, version, providerName);
            apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(provider), name, version);
            apiId = APIUtil.getUUIDFromIdentifier(apiIdentifier, organization);
            if (apiId == null) {
                throw new APIImportExportException("API Id not found for the provided details");
            }
        }

        if (exportLatestRevision) {
            //if a latest revision flag used, latest revision's api object is used
            exportAPIUUID = apiProvider.getLatestRevisionUUID(apiId);
        } else if (StringUtils.isNotBlank(revisionNum)) {
            //if a revision number provided, revision api object is used
            exportAPIUUID = apiProvider.getAPIRevisionUUID(revisionNum, apiId);
        } else {
            //if a revision number is not provided, working copy's id is used
            exportAPIUUID = apiId;
        }

        // If an incorrect revision num provided (revision does not exist)
        if (StringUtils.isBlank(exportAPIUUID)) {
            throw new APIMgtResourceNotFoundException("Incorrect revision number provided: " + revisionNum,
                    ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, revisionNum));
        }

        api = apiProvider.getAPIbyUUID(exportAPIUUID, organization);
        apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, preserveCredentials, apiProvider);
        apiIdentifier = api.getId();
        apiIdentifier.setUuid(exportAPIUUID);
        return ExportUtils.exportApi(apiProvider, apiIdentifier, apiDtoToReturn, api, userName, format, preserveStatus,
                preserveDocs, originalDevPortalUrl, organization);
    }

    @Override
    public File exportAPI(String apiId, String revisionUUID, boolean preserveStatus, ExportFormat format,
                          boolean preserveDocs, boolean preserveCredentials, String organization)
            throws APIManagementException, APIImportExportException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        API api = apiProvider.getAPIbyUUID(revisionUUID, organization);
        api.setUuid(apiId);
        apiIdentifier.setUuid(apiId);
        APIDTO apiDtoToReturn = APIMappingUtil.fromAPItoDTO(api, preserveCredentials, apiProvider);
        return ExportUtils.exportApi(apiProvider, apiIdentifier, apiDtoToReturn, api, userName, format, preserveStatus,
                preserveDocs, StringUtils.EMPTY, organization);

    }

    @Override
    public File exportAPIProduct(String apiId, String revisionUUID, boolean preserveStatus, ExportFormat format,
                                 boolean preserveDocs, boolean preserveCredentials, String organization)
            throws APIManagementException, APIImportExportException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIProductIdentifier apiProductIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiId);
        APIProduct product = apiProvider.getAPIProductbyUUID(revisionUUID, organization);
        APIProductDTO apiProductDtoToReturn = APIMappingUtil.fromAPIProducttoDTO(product);
        return ExportUtils.exportApiProduct(apiProvider, apiProductIdentifier, apiProductDtoToReturn, userName,
                format, preserveStatus, preserveDocs, preserveCredentials, organization);
    }

    @Override
    public File exportAPIProduct(String apiId, String name, String version, String providerName, String revisionNum,
                                 ExportFormat format, boolean preserveStatus, boolean preserveDocs,
                                 boolean preserveCredentials, boolean exportLatestRevision, String organization)
            throws APIManagementException, APIImportExportException {

        APIProductIdentifier apiProductIdentifier;
        APIProductDTO apiProductDtoToReturn;
        APIProduct apiProduct;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(userName));
        String exportAPIProductUUID;

        if (apiId != null) {
            apiProductIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiId, tenantDomain);
        } else {
            // Validate API name, version and provider before exporting
            String provider = ExportUtils.validateExportParams(name, version, providerName);
            apiProductIdentifier = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider), name, version);
            apiId = APIUtil.getUUIDFromIdentifier(apiProductIdentifier, organization);
        }

        if (exportLatestRevision) {
            //if a latest revision flag used, latest revision's api product object is used
            exportAPIProductUUID = apiProvider.getLatestRevisionUUID(apiId);
        } else if (revisionNum != null) {
            //if a revision number provided, revision api product object is used
            exportAPIProductUUID = apiProvider.getAPIRevisionUUID(revisionNum, apiId);
        } else {
            //if a revision number is not provided, working copy's id is used
            exportAPIProductUUID = apiId;
        }

        // If an incorrect revision num provided (revision does not exist)
        if (StringUtils.isBlank(exportAPIProductUUID)) {
            throw new APIMgtResourceNotFoundException("Incorrect revision number provided: " + revisionNum,
                    ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, revisionNum));
        }

        apiProduct = apiProvider.getAPIProductbyUUID(exportAPIProductUUID, tenantDomain);
        apiProductIdentifier.setUUID(exportAPIProductUUID);
        if (apiProduct != null) {
            apiProductDtoToReturn = APIMappingUtil.fromAPIProducttoDTO(apiProduct);
            return ExportUtils
                    .exportApiProduct(apiProvider, apiProductIdentifier, apiProductDtoToReturn, userName, format,
                            preserveStatus, preserveDocs, preserveCredentials, organization);
        }
        return null;

    }

    @Override public API importAPI(InputStream fileInputStream, Boolean preserveProvider, Boolean rotateRevision,
            Boolean overwrite, String[] tokenScopes, String organization) throws APIManagementException {

        String extractedFolderPath;
        try {
            extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(fileInputStream);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
        return ImportUtils.importApi(extractedFolderPath, null, preserveProvider, rotateRevision,
                overwrite, false, tokenScopes, null, organization);
    }

    @Override
    public APIProduct importAPIProduct(InputStream fileInputStream, Boolean preserveProvider, Boolean rotateRevision,
            Boolean overwriteAPIProduct, Boolean overwriteAPIs, Boolean importAPIs, String[] tokenScopes,
            String organization)
            throws APIManagementException {

        String extractedFolderPath;
        try {
            extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(fileInputStream);
        } catch (APIImportExportException e) {
            throw new APIManagementException(e);
        }
        return ImportUtils.importApiProduct(extractedFolderPath, preserveProvider, rotateRevision, overwriteAPIProduct,
                overwriteAPIs, importAPIs, tokenScopes, organization);
    }
}
