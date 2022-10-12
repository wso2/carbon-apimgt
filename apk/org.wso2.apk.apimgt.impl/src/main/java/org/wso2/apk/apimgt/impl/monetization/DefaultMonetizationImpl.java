/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.apk.apimgt.impl.monetization;

import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.MonetizationException;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.Monetization;
import org.wso2.apk.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.apk.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DefaultMonetizationImpl implements Monetization {

    @Override
    public boolean createBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean updateBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean deleteBillingPlan(SubscriptionPolicy subPolicy) throws MonetizationException {
        return true;
    }

    @Override
    public boolean enableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties)
            throws MonetizationException {
        return true;
    }

    @Override
    public boolean disableMonetization(String tenantDomain, API api, Map<String, String> monetizationProperties)
            throws MonetizationException {
        return true;
    }

    @Override
    public Map<String, String> getMonetizedPoliciesToPlanMapping(API api) throws MonetizationException {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getCurrentUsageForSubscription(String subscriptionUUID, APIProvider apiProvider)
            throws MonetizationException {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getTotalRevenue(API api, APIProvider apiProvider) throws MonetizationException {
        return new HashMap<String, String>();
    }

    /**
     * Update info about monetization usage publish job
     *
     * @param monetizationUsagePublishInfo
     * @return boolean always return true if there is no exception
     * @throws MonetizationException
     */
    @Override
    public boolean publishMonetizationUsageRecords(MonetizationUsagePublishInfo monetizationUsagePublishInfo)
            throws MonetizationException {

        APIAdmin apiAdmin = new APIAdminImpl();
        monetizationUsagePublishInfo.setState(APIConstants.Monetization.COMPLETED);
        monetizationUsagePublishInfo.setStatus(APIConstants.Monetization.SUCCESSFULL);
        DateFormat df = new SimpleDateFormat(APIConstants.Monetization.USAGE_PUBLISH_TIME_FORMAT);
        Date dateobj = new Date();
        //get the time in UTC format
        df.setTimeZone(TimeZone.getTimeZone(APIConstants.Monetization.USAGE_PUBLISH_TIME_ZONE));
        String currentDate = df.format(dateobj);
        long currentTimestamp = apiAdmin.getTimestamp(currentDate);
        monetizationUsagePublishInfo.setLastPublishTime(currentTimestamp);
        try {
            apiAdmin.updateMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
        } catch (APIManagementException e) {
            throw new MonetizationException("Failed to update the monetization usage publish info", e);
        }
        return true;
    }

}
