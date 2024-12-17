package org.wso2.carbon.apimgt.impl.monetization;

import org.wso2.carbon.apimgt.api.model.Monetization;

/**
 * Abstract class for monetization, implements Monetization interface
 */

public abstract class AbstractMonetization implements Monetization {

    /**
     * Returns an instance of the MonetizationSusbscription class
     *
     * @return MonetizationSubscription
     */
    public abstract MonetizationSubscription getMonetizationSubscriptionClass();

}