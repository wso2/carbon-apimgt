package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;

import java.io.InputStream;
import java.util.List;

public interface AdminDAO {

    /**
     * Derives info about monetization usage publish job
     *
     * @return info about the monetization usage publish job
     * @throws APIManagementException
     */
    MonetizationUsagePublishInfo getMonetizationUsagePublishInfo() throws APIManagementException;

    /**
     * Updates info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    void updateUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException;

    /**
     * Add info about monetization usage publish job
     *
     * @throws APIManagementException
     */
    void addMonetizationUsagePublishInfo(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws APIManagementException;

    /**
     * Add a bot detection alert subscription
     *
     * @param email email to be registered for the subscription
     * @throws APIManagementException if an error occurs when adding a bot detection alert subscription
     */
    void addBotDetectionAlertSubscription(String email) throws APIManagementException;

    /**
     * Retrieve all bot detection alert subscriptions
     *
     * @throws APIManagementException if an error occurs when retrieving bot detection alert subscriptions
     */
    List<BotDetectionData> getBotDetectionAlertSubscriptions() throws APIManagementException;

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
     * @return if subscription exist, returns the bot detection alert subscription, else returns a null object
     * @throws APIManagementException if an error occurs when retrieving a bot detection alert subscription
     */
    BotDetectionData getBotDetectionAlertSubscription(String field, String value)
            throws APIManagementException;

    /**
     * Adds an API category
     *
     * @param category      Category
     * @param organization  Organization
     * @return Category
     */
    APICategory addCategory(APICategory category, String organization) throws APIManagementException;

    /**
     * Update API Category
     *
     * @param apiCategory API category object with updated details
     * @throws APIManagementException
     */
    void updateCategory(APICategory apiCategory) throws APIManagementException;

    /**
     * Delete API Category
     *
     * @param categoryID API category ID
     * @throws APIManagementException
     */
    void deleteCategory(String categoryID) throws APIManagementException;

    /**
     * Get all available API categories of the organization
     * @param organization
     * @return
     * @throws APIManagementException
     */
    List<APICategory> getAllCategories(String organization) throws APIManagementException;

    /**
     * Checks whether the given category name is already available under given tenant domain with any UUID other than
     * the given UUID
     *
     * @param categoryName
     * @param uuid
     * @param organization
     * @return
     */
    boolean isAPICategoryNameExists(String categoryName, String uuid, String organization) throws APIManagementException;

    /**
     * Get API category by ID
     * @param apiCategoryID Category ID
     * @return
     * @throws APIManagementException
     */
    APICategory getAPICategoryByID(String apiCategoryID) throws APIManagementException;

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
