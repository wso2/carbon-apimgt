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
package org.wso2.carbon.apimgt.impl.config;

import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 * This Interface provide functionality to APIM config related operations.
 */
public interface APIMConfigService {

    /**
     * Add external-store configuration into the store.
     *
     * @param organization        organization of the user.
     * @param externalStoreConfig configuration to add.
     * @throws APIManagementException throw if external-store configuration couldn't store.
     */
    public void addExternalStoreConfig(String organization, String externalStoreConfig) throws APIManagementException;

    /**
     * Update external-store configuration into the storage.
     *
     * @param organization        organization of the user.
     * @param externalStoreConfig configuration to update.
     * @throws APIManagementException throw if external-store configuration couldn't store.
     */
    public void updateExternalStoreConfig(String organization, String externalStoreConfig)
            throws APIManagementException;

    /**
     * Retrieve external-store config for an organization.
     *
     * @param organization organization of the user.
     * @return external-store configuration.
     * @throws APIManagementException throw if external-store configuration couldn't retrieve from store.
     */
    public String getExternalStoreConfig(String organization) throws APIManagementException;

    /**
     * store tenant-config relevant to organization.
     *
     * @param organization organization of the user.
     * @param tenantConfig tenant-config of organization.
     * @throws APIManagementException throw if tenant-config couldn't store.
     */
    public void addTenantConfig(String organization, String tenantConfig) throws APIManagementException;

    /**
     * Retrieve tenant-config relevant to organization.
     *
     * @param organization organization of the user.
     * @throws APIManagementException throw if tenant-config couldn't retrieve.
     */
    public String getTenantConfig(String organization) throws APIManagementException;

    /**
     * Update tenant-config relevant to organization.
     *
     * @param tenantConfig tenant-config of organization.
     * @param organization organization of the user.
     * @throws APIManagementException throw if tenant-config couldn't retrieve.
     */
    public void updateTenantConfig(String organization, String tenantConfig) throws APIManagementException;

    /**
     * Retrieve workflow configuration relevant to organization.
     *
     * @param organization organization of the user.
     * @return workflow configuration.
     * @throws APIManagementException throw if workflow-config couldn't retrieve.
     */
    public Object getWorkFlowConfig(String organization) throws APIManagementException;

    /**
     * Update workflow configuration relevant to to organization.
     *
     * @param organization   organization of the user.
     * @param workflowConfig workflow configuration.
     * @throws APIManagementException throw if workflow-config couldn't update.
     */
    public void updateWorkflowConfig(String organization, String workflowConfig) throws APIManagementException;

    /**
     * store workflow configuration relevant to to organization.
     *
     * @param organization   organization of the user.
     * @param workflowConfig workflow configuration.
     * @throws APIManagementException throw if workflow-config couldn't add.
     */
    public void addWorkflowConfig(String organization, String workflowConfig) throws APIManagementException;

    /**
     * Retrieve google-analytics configuration relevant to to organization.
     *
     * @param organization organization of the user.
     * @return workflow configuration.
     * @throws APIManagementException throw if google-analytics configuration couldn't retrieve.
     */
    public String getGAConfig(String organization) throws APIManagementException;

    /**
     * Update google-analytics configuration relevant to to organization.
     *
     * @param organization organization of the user.
     * @param gaConfig     google-analytics configuration.
     * @throws APIManagementException throw if google-analytics configuration couldn't update.
     */
    public void updateGAConfig(String organization, String gaConfig) throws APIManagementException;

    /**
     * add google-analytics configuration relevant to to organization.
     *
     * @param organization organization of the user.
     * @param gaConfig     google-analytics configuration.
     * @throws APIManagementException throw if google-analytics configuration couldn't add.
     */
    public void addGAConfig(String organization, String gaConfig) throws APIManagementException;

    /**
     * retrieve self-signUp configuration relevant to to organization.
     *
     * @param organization organization of the user.
     * @throws APIManagementException throw if self-signUp configuration couldn't retrieve.
     */
    public Object getSelfSighupConfig(String organization) throws APIManagementException;

    /**
     * update self-signUp configuration relevant to to organization.
     *
     * @param organization     organization of the user.
     * @param selfSignUpConfig self-signUp configuration.
     * @throws APIManagementException throw if self-signUp configuration couldn't update.
     */
    public void updateSelfSighupConfig(String organization, String selfSignUpConfig) throws APIManagementException;

    /**
     * add self-signUp configuration relevant to to organization.
     *
     * @param organization     organization of the user.
     * @param selfSignUpConfig self-signUp configuration.
     * @throws APIManagementException throw if self-signUp configuration couldn't add.
     */
    public void addSelfSighupConfig(String organization, String selfSignUpConfig) throws APIManagementException;

}
