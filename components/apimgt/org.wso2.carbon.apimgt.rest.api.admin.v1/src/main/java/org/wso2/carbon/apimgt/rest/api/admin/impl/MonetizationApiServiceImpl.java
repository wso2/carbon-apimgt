/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationUsagePublishAgent;
import org.wso2.carbon.apimgt.rest.api.admin.v1.MonetizationApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.MonetizationAPIMappinUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.ws.rs.core.Response;

public class MonetizationApiServiceImpl implements MonetizationApiService {

    private static final Log log = LogFactory.getLog(MonetizationApiServiceImpl.class);
    Executor executor;

    /**
     * Run the monetization usage publish job
     *
     * @return Response of the server
     */
    @Override
    public Response monetizationPublishUsagePost(MessageContext messageContext) {

        MonetizationUsagePublishInfo monetizationUsagePublishInfo;
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            monetizationUsagePublishInfo = apiAdmin.getMonetizationUsagePublishInfo();
            if (monetizationUsagePublishInfo == null) {
                monetizationUsagePublishInfo = new MonetizationUsagePublishInfo();
                monetizationUsagePublishInfo.setId(APIConstants.Monetization.USAGE_PUBLISHER_JOB_NAME);
                monetizationUsagePublishInfo.setState(APIConstants.Monetization.INITIATED);
                monetizationUsagePublishInfo.setStatus(APIConstants.Monetization.INPROGRESS);
                //read the number of days to reduce from the current time to derive the from / last publish time
                //when there is no record of the last publish time
                APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String gap = configuration.getFirstProperty(APIConstants.Monetization.FROM_TIME_CONFIGURATION_PROPERTY);
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
            }
            if (!monetizationUsagePublishInfo.getState().equals(APIConstants.Monetization.RUNNING)) {
                executor = Executors.newSingleThreadExecutor();
                MonetizationUsagePublishAgent agent = new MonetizationUsagePublishAgent(monetizationUsagePublishInfo);
                executor.execute(agent);
                return Response.accepted().entity(MonetizationAPIMappinUtil.fromStatusToDTO("Request Accepted",
                        "Server is running the usage publisher")).build();
            } else {
                return Response.serverError().entity(MonetizationAPIMappinUtil.fromStatusToDTO("Server could not " +
                        "accept the request", "A job is already running")).build();
            }
        } catch (APIManagementException ex) {
            RestApiUtil.handleInternalServerError("Could not add or derive monetization usage publish info", ex, log);
        }
        return null;
    }

    /**
     * Retrieves the status of the last monetization usage publishing job
     *
     * @return Retruns the status of the last monetization usage publishing jon
     */
    @Override
    public Response monetizationPublishUsageStatusGet(MessageContext messageContext) {

        MonetizationUsagePublishInfo monetizationUsagePublishInfo;
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            monetizationUsagePublishInfo = apiAdmin.getMonetizationUsagePublishInfo();
            return Response.ok().entity(MonetizationAPIMappinUtil.fromUsageStateToDTO(monetizationUsagePublishInfo)).build();
        } catch (APIManagementException ex) {
            RestApiUtil.handleInternalServerError("Could not derive monetization usage publish info", ex, log);
        }
        return null;
    }
}
