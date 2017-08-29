/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.util.List;

/**
 * Interface for analytics specific methods.
 */
public interface Analyzer {

    /**
     * Retrieves application counts created overtime information.
     *
     * @param fromTime     Filter for from timestamp
     * @param toTime       Filter for to timestamp
     * @return valid {@link ApplicationCount} List or null
     * @throws APIManagementException if error occurs while accessing data
     */
    List<ApplicationCount> getApplicationCount(String fromTime, String toTime)
            throws APIManagementException;

    /**
     * Retrieves APIs information.
     *
     * @param createdBy Filter for create user
     * @param fromTime  Filter for from timestamp
     * @param toTime    Filter for to timestamp
     * @return valid {@link APIInfo} List or null
     * @throws APIManagementException if error occurs while accessing data layer
     */
    List<APIInfo> getAPIInfo(String createdBy, String fromTime, String toTime) throws APIManagementException;

    /**
     * Retrieves API counts created overtime information.
     *
     * @param createdBy Filter for create user
     * @param fromTime  Filter for from timestamp
     * @param toTime    Filter for to timestamp
     * @return valid {@link APICount} List or null
     * @throws APIManagementException if error occurs while accessing data layer
     */
    List<APICount> getAPICount(String createdBy, String fromTime, String toTime) throws APIManagementException;

    /**
     * Retrieves subscription count information against an API.
     *
     * @param createdBy Filter for created user
     * @return valid {@link APISubscriptionCount} List or null
     * @throws APIManagementException if error occurs while accessing data layer
     */
    List<APISubscriptionCount> getAPISubscriptionCount(String createdBy) throws APIManagementException;

    /**
     * Retrieves Subscriptions count created over time.
     *
     * @param createdBy Filter for api createdBy
     * @param fromTime  Filter for from timestamp
     * @param toTime    Filter for to timestamp
     * @return valid {@link SubscriptionCount} List or null
     * @throws APIManagementException if error occurs while accessing data layer
     */
    List<SubscriptionCount> getSubscriptionCount(String createdBy, String fromTime, String toTime)
            throws APIManagementException;

    /**
     * Retrieves Subscriptions info details.
     *
     * @param createdBy Filter for api createdBy
     * @param fromTime  Filter for from timestamp
     * @param toTime    Filter for to timestamp
     * @return valid {@link SubscriptionCount} List or null
     * @throws APIManagementException if error occurs while accessing data layer
     */
    List<SubscriptionInfo> getSubscriptionInfo(String createdBy, String fromTime, String toTime)
            throws APIManagementException;

}
