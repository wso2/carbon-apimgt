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

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.AlertsMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.AlertTypesListDTO;

import java.util.List;

public class AlertTypesCommonImpl {

    private AlertTypesCommonImpl() {
    }

    /**
     * Get available alert types for admin
     *
     * @return List of alert types
     * @throws APIManagementException   When an internal error occurs
     * @throws AlertManagementException When an alert related error occurs
     */
    public static AlertTypesListDTO getAdminAlertTypes() throws APIManagementException, AlertManagementException {
        AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
        List<AlertTypeDTO> alertTypes = adminAlertConfigurator
                .getSupportedAlertTypes();
        return AlertsMappingUtil.fromAlertTypesListToAlertTypeListDTO(alertTypes);
    }
}
