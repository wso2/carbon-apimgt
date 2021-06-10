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

import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
    void deleteLabel(String user, String labelID) throws APIManagementException;

    /**
     * Updates the details of the given Label.
     * @param tenantDomain    tenant domain
     * @param label             content to update
     * @throws APIManagementException if failed to update label
     */
    Label updateLabel(String tenantDomain, Label label) throws APIManagementException;

    Application[] getAllApplicationsOfTenantForMigration(String appTenantDomain) throws APIManagementException;

    /**
     * Get the applications of the given tenantId with pagination.
     *
     * @param tenantId             tenant Id
     * @param start                content to start
     * @param offset               content to limit number of pages
     * @param searchOwner          content to search applications based on owners
     * @param searchApplication    content to search applications based on application
     * @param sortColumn           content to sort column
     * @param sortOrder            content to sort in a order
     * @throws APIManagementException if failed to get applications
     */
    List<Application> getApplicationsByTenantIdWithPagination(int tenantId, int start , int offset, String searchOwner,
                                                              String searchApplication, String sortColumn,
                                                              String sortOrder)
            throws APIManagementException;

    /**
     * Get applications for the tenantId.
     *
     * @param tenantId             tenant Id
     * @param start                content to start
     * @param offset               content to limit number of pages
     * @param searchApplication    content to search applications based on application
     * @param sortColumn           content to sort column
     * @param sortOrder            content to sort in a order
     * @throws APIManagementException if failed to get application
     */
    List<Application> getApplicationsByNameWithPagination(int tenantId, int start, int offset
            , String searchApplication, String sortColumn, String sortOrder)
            throws APIManagementException;

    /**
     * Get count of the applications for the tenantId.
     *
     * @param tenantId             content to get application count based on tenant_id
     * @param searchOwner          content to search applications based on owners
     * @param searchApplication    content to search applications based on application
     * @throws APIManagementException if failed to get application
     */

    public int getApplicationsCount(int tenantId, String searchOwner, String searchApplication)
            throws APIManagementException;

    /**
     * This methods loads the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    Monetization getMonetizationImplClass() throws APIManagementException;

    /**
     * Get the info about the monetization usage publish job
     *
     * @throws APIManagementException if failed to get monetization usage publish info
     */
    MonetizationUsagePublishInfo getMonetizationUsagePublishInfo() throws APIManagementException;

    /**
     * Add the info about the monetization usage publish job
     *
     * @throws APIManagementException if failed to update monetization usage publish info
     */
    void addMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException;

    /**
     * Update the info about the monetization usage publish job
     *
     * @throws APIManagementException if failed to update monetization usage publish info
     */
    void updateMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException;

    /**
     * Add a bot detection alert subscription
     *
     * @param email email to be registered for the subscription
     * @throws APIManagementException if an error occurs when adding a bot detection alert subscription
     */
    void addBotDetectionAlertSubscription(String email) throws APIManagementException;

    /**
     * Delete a bot detection alert subscription
     *
     * @param uuid uuid of the subscription
     * @throws APIManagementException if an error occurs when deleting a bot detection alert subscription
     */
    void deleteBotDetectionAlertSubscription(String uuid) throws APIManagementException;

    /**
     * Retrieve a bot detection alert subscription by querying a particular field (uuid or email)
     *
     * @param field field to be queried to obtain the bot detection alert subscription. Can be uuid or email
     * @param value value corresponding to the field (uuid or email value)
     * @return if a subscription exists, returns the bot detection alert subscription, else returns a null object
     * @throws APIManagementException if an error occurs when retrieving a bot detection alert subscription
     */
    BotDetectionData getBotDetectionAlertSubscription(String field, String value) throws APIManagementException;

    /**
     * Retrieve all bot detection alert subscriptions
     *
     * @throws APIManagementException if an error occurs when retrieving bot detection alert subscriptions
     */
    List<BotDetectionData> getBotDetectionAlertSubscriptions() throws APIManagementException;

    /**
     * Retrieve all bot detected data
     *
     * @return list of bot detected data
     * @throws APIManagementException
     */
    List<BotDetectionData> retrieveBotDetectionData() throws APIManagementException;

    /**
     * Adds a new category for the tenant
     *
     * @param userName    logged in user name
     * @param category        category to add
     * @throws APIManagementException if failed add category
     */
    APICategory addCategory(APICategory category, String userName) throws APIManagementException;

    /**
     * Updates an API Category
     *
     * @param   apiCategory
     * @throws APIManagementException
     */
    void updateCategory(APICategory apiCategory) throws APIManagementException;

    /**
     * Delete an API Category
     *
     * @param categoryID
     * @param username
     * @throws APIManagementException
     */
    void deleteCategory(String categoryID, String username) throws APIManagementException;

    /**
     * Checks whether an api category exists by the given name
     *
     * 1. in case uuid is null : checks whether the categoryName is already taken in the tenantDomain (this
     *                           flow is used when adding a new api category)
     * 2. in case uuid is not null: checks whether the categoryName is already taken by any category other than the one
     *                              defined by the passed uuid in the given tenant
     *
     * @param categoryName
     * @param tenantID
     * @return true if an api category exists by the given category name
     * @throws APIManagementException
     */
    boolean isCategoryNameExists(String categoryName, String uuid, int tenantID) throws APIManagementException;

    /**
     * Returns all api categories of the tenant
     *
     * @param tenantID
     * @return
     * @throws APIManagementException
     */
    List<APICategory> getAllAPICategoriesOfTenant(int tenantID) throws APIManagementException;

    /**
     * Returns all api categories of the tenant with number of APIs for each category
     *
     * @param tenantID
     * @return
     * @throws APIManagementException
     */
    List<APICategory> getAPICategoriesOfTenant(int tenantID) throws APIManagementException;

    /**
     * Returns all api categories of the tenant along with the count of attached APIs
     *
     * @param username
     * @return
     * @throws APIManagementException
     */
    List<APICategory> getAllAPICategoriesOfTenantForAdminListing(String username) throws APIManagementException;

    /**
     * Get API Category identified by the given uuid
     *
     * @param apiCategoryId api category UUID
     * @return
     * @throws APIManagementException
     */
    APICategory getAPICategoryByID(String apiCategoryId) throws APIManagementException;

    /**
     * The method converts the date into timestamp
     *
     * @param date
     * @return Timestamp in long format
     */
    long getTimestamp(String date);

    /**
     * This method used to retrieve key manager configurations for tenant
     * @param tenantDomain tenant Domain
     * @return KeyManagerConfigurationDTO list
     * @throws APIManagementException if error occurred
     */
    List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByTenant(String tenantDomain) throws APIManagementException;

    /**
     * This method returns all the key managers registered in all the tenants
     * @return
     * @throws APIManagementException
     */
    Map<String, List<KeyManagerConfigurationDTO>> getAllKeyManagerConfigurations() throws APIManagementException;

    /**
     * This method used to retrieve key manager with Id in respective tenant
     * @param tenantDomain tenant domain requested
     * @param id uuid of key manager
     * @return KeyManagerConfigurationDTO for retrieved data
     * @throws APIManagementException
     */
    KeyManagerConfigurationDTO getKeyManagerConfigurationById(String tenantDomain, String id)
            throws APIManagementException;
    /**
     * This method used to check existence of key manager with Id in respective tenant
     * @param tenantDomain tenant domain requested
     * @param id uuid of key manager
     * @return existence
     * @throws APIManagementException
     */
    boolean isKeyManagerConfigurationExistById(String tenantDomain, String id) throws APIManagementException;

    /**
     * This method used to create key Manager
     * @param keyManagerConfigurationDTO key manager data
     * @return created key manager
     * @throws APIManagementException
     */
    KeyManagerConfigurationDTO addKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException;
    /**
     * This method used to update key Manager
     * @param keyManagerConfigurationDTO key manager data
     * @return updated key manager
     * @throws APIManagementException
     */
    KeyManagerConfigurationDTO updateKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException;

    /**
     * This method used to delete key manager
     * @param tenantDomain tenant domain requested
     * @param id uuid of key manager
     * @throws APIManagementException
     */
    void deleteKeyManagerConfigurationById(String tenantDomain,String id) throws APIManagementException;

    /**
     * This method used to retrieve key manager from name
     * @param tenantDomain tenant domain requested
     * @param name name requested
     * @return keyManager data
     * @throws APIManagementException
     */
    KeyManagerConfigurationDTO getKeyManagerConfigurationByName(String tenantDomain, String name)
            throws APIManagementException;

    /**
     * The method get all the pending workflow requests
     *
     * @param workflowType
     * @param status
     * @param tenantDomain
     * @return Workflow[]
     * @throws APIManagementException
     */
    Workflow[] getworkflows(String workflowType, String status, String tenantDomain) throws APIManagementException;

    /**
     * The method get all the pending workflow requests
     *
     * @param externelWorkflowRef
     * @param status
     * @param tenantDomain
     * @return Workflow
     * @throws APIManagementException
     */
    Workflow getworkflowReferenceByExternalWorkflowReferenceID(String externelWorkflowRef, String status, String tenantDomain)
            throws APIManagementException;

    /**
     * This method used to check the existence of the scope name for the particular user
     * @param username user to be validated
     * @param scopeName scope name to be checked
     * @return true if a scope exists by the given username
     * @throws APIManagementException
     */
    boolean isScopeExistsForUser(String username, String scopeName)
            throws APIManagementException;

    /**
     * This method used to check the existence of the scope name
     * @param username logged in username to get the tenantDomain
     * @param scopeName scope name to be checked
     * @return true if a scope exists
     * @throws APIManagementException
     */
    boolean isScopeExists(String username, String scopeName)
            throws APIManagementException;

    /**
     * Adds a tenant theme to the database
     *
     * @param tenantId     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when adding a tenant theme to the database
     */
    void addTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException;

    /**
     * Updates an existing tenant theme in the database
     *
     * @param tenantId     tenant ID of user
     * @param themeContent content of the tenant theme
     * @throws APIManagementException if an error occurs when updating an existing tenant theme in the database
     */
    void updateTenantTheme(int tenantId, InputStream themeContent) throws APIManagementException;

    /**
     * Retrieves a tenant theme from the database
     *
     * @param tenantId tenant ID of user
     * @return content of the tenant theme
     * @throws APIManagementException if an error occurs when retrieving a tenant theme from the database
     */
    InputStream getTenantTheme(int tenantId) throws APIManagementException;

    /**
     * Checks whether a tenant theme exist for a particular tenant
     *
     * @param tenantId tenant ID of user
     * @return true if a tenant theme exist for a particular tenant ID, false otherwise
     * @throws APIManagementException if an error occurs when determining whether a tenant theme exists for a given
     *                                tenant ID
     */
    boolean isTenantThemeExist(int tenantId) throws APIManagementException;

    /**
     * Deletes a tenant theme from the database
     *
     * @param tenantId tenant ID of user
     * @throws APIManagementException if an error occurs when deleting a tenant theme from the database
     */
    void deleteTenantTheme(int tenantId) throws APIManagementException;
}
