package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.sql.Connection;

public interface PolicyDAO {

    /**
     * Add API Policy
     *
     * @param policy policy object to add
     * @throws APIManagementException
     */
    APIPolicy addAPIPolicy(APIPolicy policy) throws APIManagementException;

    /**
     * Retrieves {@link APIPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     * <p>This will retrieve complete details about the APIPolicy with all pipelins and conditions.</p>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link APIPolicy}
     * @throws APIManagementException
     */
    APIPolicy getAPIPolicy(String policyName, int tenantId) throws APIManagementException;

    /**
     * Add a Application level throttling policy to database
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    void addApplicationPolicy(ApplicationPolicy policy) throws APIManagementException;

    /**
     * Retrieves {@link ApplicationPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link ApplicationPolicy}
     * @throws APIManagementException
     */
    ApplicationPolicy getApplicationPolicy(String policyName, int tenantId) throws APIManagementException;

    /**
     * Add a Subscription level throttling policy to database
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    void addSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException;

    /**
     * Retrieves {@link SubscriptionPolicy} with name <code>policyName</code> and tenant Id <code>tenantNId</code>
     *
     * @param policyName name of the policy to retrieve from the database
     * @param tenantId   tenantId of the policy
     * @return {@link SubscriptionPolicy}
     * @throws APIManagementException
     */
    SubscriptionPolicy getSubscriptionPolicy(String policyName, int tenantId) throws APIManagementException;

    /**
     * Add a Global level throttling policy to database
     *
     * @param policy Global Policy
     * @throws APIManagementException
     */
    void addGlobalPolicy(GlobalPolicy policy) throws APIManagementException;

    /**
     * Get a particular Global level policy.
     *
     * @param policyName name of the global polixy
     * @return {@link GlobalPolicy}
     * @throws APIManagementException
     */
    GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException;

    /**
     * Update a API level throttling policy to database.
     * <p>
     * If condition group already exists for the policy, that condition Group will be deleted and condition Group will
     * be inserted to the database with old POLICY_ID.
     * </p>
     *
     * @param policy policy object defining the throttle policy
     * @throws APIManagementException
     */
    APIPolicy updateAPIPolicy(APIPolicy policy) throws APIManagementException;

    /**
     * Updates Application level policy.
     * <p>policy name and tenant id should be specified in <code>policy</code></p>
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    void updateApplicationPolicy(ApplicationPolicy policy) throws APIManagementException;

    /**
     * Updates Subscription level policy.
     * <p>policy name and tenant id should be specified in <code>policy</code></p>
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIManagementException;

    /**
     * Updates global throttle policy in database
     *
     * @param policy updated policy object
     * @throws APIManagementException
     */
    void updateGlobalPolicy(GlobalPolicy policy) throws APIManagementException;

    /**
     * Retrieves list of available policy names under <code>policyLevel</code>
     * and user <code>username</code>'s tenant
     *
     * @param policyLevel policY level to filter policies
     * @param username    username will be used to get the tenant
     * @return array of policy names
     * @throws APIManagementException
     */
    String[] getPolicyNames(String policyLevel, String username) throws APIManagementException;

    /**
     * Removes a throttling policy from the database
     *
     * @param policyLevel level of the policy to be deleted
     * @param policyName  name of the policy
     * @param tenantId    used to get the tenant id
     * @throws APIManagementException
     */
    void removeThrottlePolicy(String policyLevel, String policyName, int tenantId)
            throws APIManagementException;

    /**
     * Returns true if the key template exist in DB
     *
     * @param policy Global Policy
     * @return true if key template already exists
     * @throws APIManagementException
     */
    boolean isKeyTemplatesExist(GlobalPolicy policy) throws APIManagementException;

    /**
     * Returns true if Application Policy is attached to Application
     *
     * @param policyName Policy Name
     * @param organization Organization
     * @return true if key template already exists
     * @throws APIManagementException
     */
    boolean hasApplicationPolicyAttachedToApplication(String policyName, String organization) throws APIManagementException;

    /**
     * Returns true if Subscription Policy is attached
     *
     * @param policyName Policy Name
     * @param organization Organization
     * @return true if key template already exists
     * @throws APIManagementException
     */
    boolean hasSubscriptionPolicyAttached(String policyName, String organization) throws APIManagementException;

    /**
     * Returns true if API Policy is attached
     *
     * @param policyName Policy Name
     * @param organization Organization
     * @return true if key template already exists
     * @throws APIManagementException
     */
    boolean hasAPIPolicyAttached(String policyName, String organization) throws APIManagementException;

    /**
     * Retrieves {@link APIPolicy} with name <code>uuid</code>
     * <p>This will retrieve complete details about the APIPolicy with all pipelines and conditions.</p>
     *
     * @param uuid uuid of the policy to retrieve from the database
     * @return {@link APIPolicy}
     * @throws APIManagementException
     */
    APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Retrieves {@link ApplicationPolicy} with name <code>uuid</code>
     *
     * @param uuid uuid of the policy to retrieve from the database
     * @return {@link ApplicationPolicy}
     * @throws APIManagementException
     */
    ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Retrieves {@link SubscriptionPolicy} with name <code>uuid</code>
     *
     * @param uuid name of the policy to retrieve from the database
     * @return {@link SubscriptionPolicy}
     * @throws APIManagementException
     */
    SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Get a particular Global level policy given UUID.
     *
     * @param uuid name of the global polixy
     * @return {@link GlobalPolicy}
     * @throws APIManagementException
     */
    GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Get API level policies. Result only contains basic details of the policy,
     * it doesn't contain pipeline information.
     *
     * @param tenantID policies are selected using tenantID
     * @return APIPolicy ArrayList
     * @throws APIManagementException
     */
    APIPolicy[] getAPIPolicies(int tenantID) throws APIManagementException;

    /**
     * Get application level polices
     *
     * @param tenantID polices are selected only belong to specific tenantID
     * @return AppilicationPolicy array list
     */
    ApplicationPolicy[] getApplicationPolicies(int tenantID) throws APIManagementException;

    /**
     * Get all subscription level policies belongs to specific tenant
     *
     * @param tenantID tenantID filters the polices belongs to specific tenant
     * @return subscriptionPolicy array list
     */
    SubscriptionPolicy[] getSubscriptionPolicies(int tenantID) throws APIManagementException;

    /**
     * Get all Global level policeis belongs to specific tenant
     *
     * @param tenantID
     * @return
     * @throws APIManagementException
     */
    GlobalPolicy[] getGlobalPolicies(int tenantID) throws APIManagementException;


}
