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

import java.util.List;

/**
 * Interface for analytics specific methods.
 */
public interface Analyzer {

    List<ApplicationCount> getApplicationCount(String createdBy, String subscribedTo, String fromTime, String toTime)
            throws APIManagementException;

    List<APIInfo> getAPIInfo(String createdBy, String fromTime, String toTime) throws APIManagementException;

    List<APICount> getAPICount(String createdBy, String fromTime, String toTime) throws APIManagementException;

    List<APISubscriptionCount> getAPISubscriptionCount(String createdBy) throws APIManagementException;

    List<SubscriptionCount> getSubscriptionCount(String createdBy, String fromTime, String toTime)
            throws APIManagementException;
}
