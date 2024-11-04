package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Abstract class for monetization, implements Monetization interface
 */

public abstract class AbstractMonetization implements Monetization{

    /**
     * Create billing plan for a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    public abstract boolean createBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Update billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    public abstract boolean updateBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Delete a billing plan of a policy
     *
     * @param subPolicy subscription policy
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    public abstract boolean deleteBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException;

    /**
     * Enable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    public abstract boolean enableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties) throws MonetizationException;

    /**
     * Disable monetization for a API
     *
     * @param tenantDomain           tenant domain
     * @param api                    API
     * @param monetizationProperties monetization properties map
     * @return true if successful, false otherwise
     * @throws MonetizationException if the action failed
     */
    public abstract boolean disableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties) throws MonetizationException;

    /**
     * Get mapping of tiers and billing engine plans
     *
     * @param api API
     * @return tier to billing plan mapping
     * @throws MonetizationException if failed to get tier to billing plan mapping
     */
    public abstract Map<String, String> getMonetizedPoliciesToPlanMapping(API api) throws MonetizationException;

    /**
     * Get current usage for a subscription
     *
     * @param subscriptionUUID subscription UUID
     * @param apiProvider      API provider
     * @return current usage for a subscription
     * @throws MonetizationException if failed to get current usage for a subscription
     */
    public abstract Map<String, String> getCurrentUsageForSubscription(String subscriptionUUID, APIProvider apiProvider) throws MonetizationException;

    /**
     * Get total revenue for a given API from all subscriptions
     *
     * @param api API
     * @param apiProvider API provider
     * @return total revenue data for a given API from all subscriptions
     * @throws MonetizationException if failed to get total revenue data for a given API
     */
    public abstract Map<String, String> getTotalRevenue(API api, APIProvider apiProvider) throws MonetizationException;

    @Override
    public boolean publishMonetizationUsageRecords(MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws MonetizationException {
        Object usageData = monetizationAnalyticsDataProvider(monetizationUsagePublishInfo);
        return publishUsageData(usageData, monetizationUsagePublishInfo);
    }

    /**
     * Publish the usageData to the billing engine
     *
     * @return true if the job is successful, and false otherwise
     * @throws MonetizationException if failed to publish usageData
     */
    public abstract boolean publishUsageData(Object usageData, MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws MonetizationException;

    /**
     * Gets Usage Data from Analytics Provider
     *
     * @param monetizationUsagePublishInfo monetization publish info
     * @return usage data from analytics provider
     * @throws AnalyticsException if the action failed
     */
    public Object monetizationAnalyticsDataProvider(MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws MonetizationException{
        String className = "org.wso2.apim.analytics.impl.ChoreoAnalyticsforMonetizationImpl"; // todo the hard-coded FQN of the implementation class, linking will be done later
        String message;
        try {
            Class<?> callingClass = Class.forName(className); //The class is gotten using the FQN
            AnalyticsforMonetization analyticsClass = (AnalyticsforMonetization) callingClass.getDeclaredConstructor().newInstance(); //The class is called and instantiated as a child of the interface
            return analyticsClass.getUsageData(monetizationUsagePublishInfo); //the usageData is returned from the implementation class
        }  catch (ClassNotFoundException e) {
            message = "The specified class was not found";
            throw new MonetizationException(message, e);
        } catch (InvocationTargetException | AnalyticsException e) {
            message = "Error fetching usage data";
            throw new MonetizationException(message, e);
        } catch (InstantiationException e) {
            message = "Error creating class instance";
            throw new MonetizationException(message, e);
        } catch (IllegalAccessException e) {
            message = "Error getting class access";
            throw new MonetizationException(message, e);
        } catch (NoSuchMethodException e) {
            message = "Error invoking method";
            throw new MonetizationException(message, e);
        } catch (ClassCastException e) {
            message = "Error getting child class";
            throw new MonetizationException(message, e);
        } //handling of possible exceptionsb
    }
}
