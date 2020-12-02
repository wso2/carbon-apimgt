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


package org.wso2.carbon.apimgt.impl.importexport;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;

import java.io.File;
import java.io.InputStream;

/**
 * Import Export OSGI interface.
 */
public interface ImportExportAPI {

    /**
     * Used to export API artifact
     *
     * @param apiId          UUID of API.
     * @param name           name of API.
     * @param version        version of API.
     * @param providerName   provider of API.
     * @param preserveStatus Preserve API status on export
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveDocs   Preserve documentation on Export.
     * @return API artifact.
     * @throws APIManagementException
     * @throws APIImportExportException
     */
    public File exportAPI(String apiId, String name, String version, String providerName, boolean preserveStatus,
            ExportFormat format, boolean preserveDocs) throws APIManagementException, APIImportExportException;

    /**
     * Used to export API product artifact.
     *
     * @param apiId          UUID of API.
     * @param name           name of API.
     * @param version        version of API.
     * @param providerName   provider of API.
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @param preserveDocs   Preserve documentation on Export.
     * @return APIProduct Artifact.
     * @throws APIManagementException
     * @throws APIImportExportException
     */
    public File exportAPIProduct(String apiId, String name, String version, String providerName, ExportFormat format,
            boolean preserveStatus, boolean preserveDocs) throws APIManagementException, APIImportExportException;

    /**
     * Used to import an API artifact.
     *
     * @param fileInputStream  Input stream from the REST request
     *                         (This will not be null when importing dependent APIs with API Products)
     * @param preserveProvider Decision to keep or replace the provider
     * @param overwrite        Whether to update the API or not
     * @throws APIImportExportException If there is an error in importing an API
     * @@return Imported API
     */
    public API importAPI(InputStream fileInputStream, Boolean preserveProvider, Boolean overwrite, String[] tokenScopes)
            throws APIManagementException;

    /**
     * Used to import an API Product artifact.
     *
     * @param fileInputStream     Input stream from the REST request
     *                            (This will not be null when importing dependent APIs with API Products)
     * @param preserveProvider    User choice to keep or replace the API Product provider
     * @param importAPIs          Whether to import the dependent APIs or not.
     * @param overwriteAPIProduct Whether to update the API Product or not. This is used when updating already existing API Products.
     * @param overwriteAPIs       Whether to update the dependent APIs or not. This is used when updating already existing dependent APIs of an API Product.
     * @@return Imported API Product
     */
    public APIProduct importAPIProduct(InputStream fileInputStream, Boolean preserveProvider,
            Boolean overwriteAPIProduct, Boolean overwriteAPIs, Boolean importAPIs, String[] tokenScopes)
            throws APIManagementException;
}
