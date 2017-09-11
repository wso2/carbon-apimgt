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
package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.util.List;

/**
 * Provides access to API Analytics data layer.
 */
public interface AnalyticsDAO {

    /**
     * Retrieves applications created overtime information.
     *
     * @param fromTimestamp Filter for from timestamp
     * @param toTimestamp   Filter for to timestamp
     * @return valid {@link ApplicationCount} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<ApplicationCount> getApplicationCount(String fromTimestamp,
                                               String toTimestamp) throws APIMgtDAOException;

    /**
     * Retrieves APIs created overtime information.
     *
     * @param fromTimestamp Filter for from timestamp
     * @param toTimestamp   Filter for to timestamp
     * @return valid {@link APIInfo} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<APIInfo> getAPIInfo(String fromTimestamp, String toTimestamp) throws APIMgtDAOException;

    /**
     * Retrieves APIs created overtime information.
     *
     * @param fromTimestamp Filter for from timestamp
     * @param toTimestamp   Filter for to timestamp
     * @return valid {@link APIInfo} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<APICount> getAPICount(String fromTimestamp,
                               String toTimestamp) throws APIMgtDAOException;

    /**
     * Retrieves API subscription count information.
     *
     * @param fromTime Filter for from timestamp
     * @param toTime   Filter for to timestamp
     * @param apiId    Filter for api provider
     * @return valid {@link APISubscriptionCount} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<APISubscriptionCount> getAPISubscriptionCount(String fromTime, String toTime, String apiId) throws
    APIMgtDAOException;

    /**
     * Retrieves Subscriptions count created over time.
     *
     * @param fromTimestamp Filter for from timestamp
     * @param toTimestamp   Filter for to timestamp
     * @return valid {@link SubscriptionCount} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<SubscriptionCount> getSubscriptionCount(String fromTimestamp,
                                                 String toTimestamp) throws APIMgtDAOException;

    /**
     * Retrieves Subscriptions info created over time.
     *
     * @param fromTimestamp Filter for from timestamp
     * @param toTimestamp   Filter for to timestamp
     * @return valid {@link SubscriptionInfo} List or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<SubscriptionInfo> getSubscriptionInfo(String fromTimestamp, String toTimestamp) throws APIMgtDAOException;
}
