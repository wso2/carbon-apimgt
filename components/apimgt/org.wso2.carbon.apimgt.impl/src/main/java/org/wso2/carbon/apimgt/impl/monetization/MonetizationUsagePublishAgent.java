/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.monetization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MonetizationUsagePublishAgent implements Runnable {

    private static final Log log = LogFactory.getLog(MonetizationUsagePublishAgent.class);
    MonetizationUsagePublishInfo monetizationUsagePublishInfo;

    public MonetizationUsagePublishAgent(MonetizationUsagePublishInfo publishInfo) {
        monetizationUsagePublishInfo = publishInfo;
    }

    @Override
    public void run() {

        Monetization monetizationImpl = null;
        APIAdmin apiAdmin = null;
        try {
            apiAdmin = new APIAdminImpl();
            monetizationImpl = apiAdmin.getMonetizationImplClass();
            monetizationUsagePublishInfo.setState(APIConstants.Monetization.RUNNING);
            monetizationUsagePublishInfo.setStatus(APIConstants.Monetization.INPROGRESS);
            DateFormat df = new SimpleDateFormat(APIConstants.Monetization.USAGE_PUBLISH_TIME_FORMAT);
            Date dateobj = new Date();
            df.setTimeZone(TimeZone.getTimeZone(APIConstants.Monetization.USAGE_PUBLISH_TIME_ZONE));
            String currentDate = df.format(dateobj);
            long currentTimestamp = apiAdmin.getTimestamp(currentDate);
            //set the current time as starting time of the job
            monetizationUsagePublishInfo.setStartedTime(currentTimestamp);
            apiAdmin.updateMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
            monetizationImpl.publishMonetizationUsageRecords(monetizationUsagePublishInfo);
        } catch (Exception e) {
            try {
                //update the state and status of the job incase of any execptions
                monetizationUsagePublishInfo.setState(APIConstants.Monetization.COMPLETED);
                monetizationUsagePublishInfo.setStatus(APIConstants.Monetization.FAILED);
                apiAdmin.updateMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
            } catch (APIManagementException ex) {
                String errorMsg = "Failed to update the state of monetization ussge publisher";
                log.error(errorMsg, ex);
            }
            String errorMsg = "Failed to publish monetization usage to billing Engine";
            log.error(errorMsg, e);
        }
    }
}
