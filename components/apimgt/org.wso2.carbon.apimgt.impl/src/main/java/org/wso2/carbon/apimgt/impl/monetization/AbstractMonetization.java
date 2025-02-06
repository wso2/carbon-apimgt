package org.wso2.carbon.apimgt.impl.monetization;

import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.AnalyticsforMonetization;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

import java.util.Map;

/**
 * Abstract class for monetization, implements Monetization interface
 */

public abstract class AbstractMonetization implements Monetization {

    private AnalyticsforMonetization analyticsClass;
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    public static final String ANALYTICS_IMPL = "analyticsImpl";

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
     * Returns an instance of the MonetizationSusbscription class
     *
     * @return MonetizationSubscription
     */
    public abstract MonetizationSubscription getMonetizationSubscriptionClass();

    /**
     * Gets Usage Data from Analytics Provider
     *
     * @param monetizationUsagePublishInfo monetization publish info
     * @return usage data from analytics provider
     * @throws MonetizationException if the action failed
     */
    public Object monetizationAnalyticsDataProvider(MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws MonetizationException{
        Map<String,String> configs = configuration.getAnalyticsProperties();
        String className;
        if (configs.containsKey(ANALYTICS_IMPL) && !configs.get(ANALYTICS_IMPL).isEmpty()) {
            className = configs.get(ANALYTICS_IMPL);
        } else {
            className = "org.wso2.am.analytics.retriever.choreo.ChoreoAnalyticsforMonetizationImpl";
        }
        String message;
        try {
            Class<?> callingClass = APIUtil.getClassForName(className);
            Constructor<?> cons = callingClass.getConstructors()[0];
            analyticsClass = (AnalyticsforMonetization) cons.newInstance();;
            return analyticsClass.getUsageData(monetizationUsagePublishInfo);
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
        } catch (ClassCastException e) {
            message = "Error getting child class";
            throw new MonetizationException(message, e);
        } //handling of possible exceptions
    }
}