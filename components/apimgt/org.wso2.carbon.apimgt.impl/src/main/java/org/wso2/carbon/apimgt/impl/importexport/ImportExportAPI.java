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

import java.io.File;

/**
 * Import Export OSGI interface.
 */
public interface ImportExportAPI {

    /**
     * Used to export API artifact
     *
     * @param apiId UUID of API.
     * @param name name of API.
     * @param version version of API.
     * @param providerName provider of API.
     * @param preserveStatus Preserve API status on export
     * @param format Format of output documents. Can be YAML or JSON
     * @param preserveDocs Preserve documentation on Export.
     * @return API artifact.
     * @throws APIManagementException
     * @throws APIImportExportException
     */
    public File exportAPI(String apiId, String name, String version, String providerName, boolean preserveStatus,
                          ExportFormat format, boolean preserveDocs)
            throws APIManagementException, APIImportExportException;

    /**
     * Used to export API product artifact.
     *
     * @param apiId UUID of API.
     * @param name name of API.
     * @param version version of API.
     * @param providerName provider of API.
     * @param format Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @param preserveDocs Preserve documentation on Export.
     * @return APIProduct Artifact.
     * @throws APIManagementException
     * @throws APIImportExportException
     */
    public File exportApiProduct(String apiId, String name, String version, String providerName, ExportFormat format,
                                 boolean preserveStatus, boolean preserveDocs)
            throws APIManagementException, APIImportExportException;
}
