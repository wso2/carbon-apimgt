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
            monetizationUsagePublishInfo.setState(APIConstants.MonetizationUsagePublisher.RUNNING);
            monetizationUsagePublishInfo.setStatus(APIConstants.MonetizationUsagePublisher.INPROGRESS);
            DateFormat df = new SimpleDateFormat(APIConstants.MonetizationUsagePublisher.TIME_FORMAT);
            Date dateobj = new Date();
            df.setTimeZone(TimeZone.getTimeZone(APIConstants.MonetizationUsagePublisher.TIME_ZONE));
            String currentDate = df.format(dateobj);
            long currentTimestamp = apiAdmin.getTimestamp(currentDate);
            //set the current time as starting time of the job
            monetizationUsagePublishInfo.setStartedTime(currentTimestamp);
            apiAdmin.updateMonetizationUsagePublishInfo(monetizationUsagePublishInfo);
            monetizationImpl.publishMonetizationUsageRecords(monetizationUsagePublishInfo);
        } catch (Exception e) {
            try {
                //update the state and status of the job incase of any execptions
                monetizationUsagePublishInfo.setState(APIConstants.MonetizationUsagePublisher.COMPLETED);
                monetizationUsagePublishInfo.setStatus(APIConstants.MonetizationUsagePublisher.FAILED);
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
