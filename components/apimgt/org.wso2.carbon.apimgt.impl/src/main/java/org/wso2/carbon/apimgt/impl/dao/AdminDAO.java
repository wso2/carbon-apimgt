package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;

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

}
