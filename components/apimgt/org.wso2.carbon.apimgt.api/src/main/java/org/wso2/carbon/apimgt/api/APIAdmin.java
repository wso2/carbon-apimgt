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
package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Label;

import java.util.List;

/**
 * APIAdmin responsible for providing helper functionality
 */
public interface APIAdmin  {
    /**
     * Returns labels of a given tenant
     *
     * @param tenantDomain    tenant domain
     * @return A List of labels related to the given tenant
     */
    List<Label> getAllLabels(String tenantDomain) throws APIManagementException;

    /**
     * Creates a new label for the tenant
     *
     * @param tenantDomain    tenant domain
     * @param label           content to add
     * @throws APIManagementException if failed add Label
     */
    Label addLabel(String tenantDomain, Label label) throws APIManagementException;

    /**
     * Delete existing label
     *
     * @param labelID  Label identifier
     * @throws APIManagementException If failed to delete label
     */
    void deleteLabel(String labelID) throws APIManagementException;

    /**
     * Updates the details of the given Label.
     *
     * @param label             content to update
     * @throws APIManagementException if failed to update label
     */
    Label updateLabel(Label label) throws APIManagementException;

    Application[] getAllApplicationsOfTenant(String appTenantDomain) throws APIManagementException;
}
