/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.monetization.MonetizationUsagePublishAgent;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.MonetizationAPIMappinUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.MonetizationUsagePublishInfoDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.PublishStatusDTO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MonetizationCommonImpl {

    static Executor executor;

    private MonetizationCommonImpl() {
    }

    /**
     * Run the monetization usage publish job
     *
     * @return Monetization usage publisher status
     * @throws APIManagementException When an internal error occurs
     */
    public static PublishStatusDTO publishMonetizationRecords() throws APIManagementException {
        MonetizationUsagePublishInfo monetizationUsagePublishInfo;
        APIAdmin apiAdmin = new APIAdminImpl();
        monetizationUsagePublishInfo = apiAdmin.getMonetizationUsagePublishInfo();

        if (monetizationUsagePublishInfo == null) {
            monetizationUsagePublishInfo = new MonetizationUsagePublishInfo();
            monetizationUsagePublishInfo.setId(APIConstants.Monetization.USAGE_PUBLISHER_JOB_NAME);
            monetizationUsagePublishInfo.setState(APIConstants.Monetization.INITIATED);
            monetizationUsagePublishInfo.setStatus(APIConstants.Monetization.INPROGRESS);
            //read the number of days to reduce from the current time to derive the from / last publish time
            //when there is no record of the last publish time
            // TODO: // read from config
            String gap = null;
//            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().
//                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
//            String gap = configuration.getMonetizationConfigurationDto().getPublishTimeDurationInDays();
            //if the from time / last publish time is not set , set it to default
            if (gap == null) {
                gap = APIConstants.Monetization.USAGE_PUBLISH_DEFAULT_TIME_GAP_IN_DAYS;
            }
            DateFormat df = new SimpleDateFormat(APIConstants.Monetization.USAGE_PUBLISH_TIME_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone(APIConstants.Monetization.USAGE_PUBLISH_TIME_ZONE));
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            String formattedCurrentDate = df.format(currentDate);
            long currentTimestamp = apiAdmin.getTimestamp(formattedCurrentDate);
            monetizationUsagePublishInfo.setStartedTime(currentTimestamp);
            //reducing the number of days set to get the last published time when there is no record of
            //the last published time
            cal.add(Calendar.DATE, -Integer.parseInt(gap));
            Date fromDate = cal.getTime();
            String formattedFromDate = df.format(fromDate);
            long lastPublishedTimeStamp = apiAdmin.getTimestamp(formattedFromDate);
            monetizationUsagePublishInfo.setLastPublishTime(lastPublishedTimeStamp);
            apiAdmin.addMonetizationUsagePublishInfo(monetizationUsagePublishInfo);

            if (!monetizationUsagePublishInfo.getState().equals(APIConstants.Monetization.RUNNING)) {
                executor = Executors.newSingleThreadExecutor();
                MonetizationUsagePublishAgent agent = new MonetizationUsagePublishAgent(monetizationUsagePublishInfo);
                executor.execute(agent);
                return MonetizationAPIMappinUtil.fromStatusToDTO("Request Accepted",
                        "Server is running the usage publisher");
            } else {
                return MonetizationAPIMappinUtil.fromStatusToDTO("Server could not " +
                        "accept the request", "A job is already running");
            }
        }
        return null;
    }

    /**
     * Retrieves the status of the last monetization usage publishing job
     *
     * @return Monetization usage publish info
     * @throws APIManagementException When an internal error occurs
     */
    public static MonetizationUsagePublishInfoDTO getMonetizationUsagePublisherStatus() throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        MonetizationUsagePublishInfo monetizationUsagePublishInfo = apiAdmin.getMonetizationUsagePublishInfo();
        return MonetizationAPIMappinUtil.fromUsageStateToDTO(monetizationUsagePublishInfo);
    }
}
