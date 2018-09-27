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
package org.wso2.carbon.apimgt.impl;


import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.List;

/**
 * This class provides the core API admin functionality.
 */
public class APIAdminImpl implements APIAdmin {

    ApiMgtDAO apiMgtDAO= ApiMgtDAO.getInstance();
    /**
     * Returns all labels associated with given tenant domain.
     *
     * @param tenantDomain tenant domain
     * @return List<Label>  List of label of given tenant domain.
     * @throws APIManagementException
     */
    public List<Label> getAllLabels(String tenantDomain) throws APIManagementException {
        return apiMgtDAO.getAllLabels(tenantDomain);
    }

    /**
     * Creates a new label for the tenant
     *
     * @param tenantDomain    tenant domain
     * @param label           content to add
     * @throws APIManagementException if failed add Label
     */
    public Label addLabel(String tenantDomain, Label label) throws APIManagementException{
        return apiMgtDAO.addLabel(tenantDomain, label);
    }

    /**
     * Delete an existing label
     *
     * @param labelId Label identifier
     * @throws APIManagementException If failed to delete label
     */
    public void deleteLabel(String labelId) throws APIManagementException{
        apiMgtDAO.deleteLabel(labelId);
    }

    /**
     * Updates the details of the given Label.
     *
     * @param label             content to update
     * @throws APIManagementException if failed to update label
     */
    public Label updateLabel(Label label) throws APIManagementException{
        return apiMgtDAO.updateLabel(label);
    }

    @Override
    public Application[] getAllApplicationsOfTenant(String appTenantDomain) throws APIManagementException{
        return apiMgtDAO.getLightWeightApplicationsOfTenant(appTenantDomain);
    }
}
