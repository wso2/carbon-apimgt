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

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.apache.commons.io.FileUtils;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.RestApiAdminUtils;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TenantThemeCommonImpl {

    private static final String TENANT_THEMES_EXPORT_DIR_PREFIX = "exported-tenant-themes";

    private TenantThemeCommonImpl() {
    }

    public static void importTenantTheme(InputStream fileInputStream) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
            String errorMessage = "Super Tenant " + APIConstants.SUPER_TENANT_DOMAIN +
                    " is not allowed to import a tenant theme";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_IMPORT_NOT_ALLOWED,
                            APIConstants.SUPER_TENANT_DOMAIN));
        }
        try {
            RestApiAdminUtils.importTenantTheme(fileInputStream, tenantDomain);
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), e,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_IMPORT_FAILED, tenantDomain, e.getMessage()));
        }
    }

    public static File exportTenantTheme() throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APIAdminImpl apiAdmin = new APIAdminImpl();
        if (!apiAdmin.isTenantThemeExist(tenantId)) {
            throw new APIManagementException("Tenant Theme for tenant " + tenantDomain + " does not exist.",
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_NOT_FOUND, tenantDomain));

        }
        InputStream tenantTheme = apiAdmin.getTenantTheme(tenantId);
        String tempPath =
                System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + TENANT_THEMES_EXPORT_DIR_PREFIX;
        String tempFile = tenantDomain + APIConstants.ZIP_FILE_EXTENSION;
        File tenantThemeArchive = new File(tempPath, tempFile);

        try {
            FileUtils.copyInputStreamToFile(tenantTheme, tenantThemeArchive);
            return tenantThemeArchive;
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), e,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_EXPORT_FAILED, tenantDomain, e.getMessage()));
        }
    }

}
