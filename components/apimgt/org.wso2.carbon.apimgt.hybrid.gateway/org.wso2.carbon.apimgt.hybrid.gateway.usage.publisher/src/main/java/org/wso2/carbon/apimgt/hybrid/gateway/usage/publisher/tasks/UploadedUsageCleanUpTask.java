/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.ntask.core.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Task for cleaning uploaded old usage files in db
 */
public class UploadedUsageCleanUpTask implements Task {

    private static final Log LOG = LogFactory.getLog(UploadedUsageCleanUpTask.class);
    private Map<String, String> properties;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override public void setProperties(Map<String, String> map) {
        this.properties = map;
    }

    @Override public void init() {

    }

    @Override public void execute() {

        String fileRetentionDays = properties.get("fileRetentionDays");
        if (fileRetentionDays != null && !fileRetentionDays.isEmpty()) {
            Date lastKeptDate = getLastKeptDate(Integer.parseInt(fileRetentionDays));
            LOG.info("Uploaded API Usage data in the db will be cleaned up to : " + dateFormat.format(lastKeptDate));
            try {
                UploadedUsageFileInfoDAO.deleteProcessedOldFiles(lastKeptDate);
            } catch (UsagePublisherException e) {
                LOG.error("Error occurred while cleaning the uploaded usage data.", e);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Uploaded usage cleanup is not enabled.");
            }
        }
    }

    /**
     * Return the {@link Date} up to which files should be retained
     *
     * @param fileRetentionDays No of days to retain the files
     * @return {@link Date} up to which files should be retained
     */
    private Date getLastKeptDate(int fileRetentionDays) {
        Date myDate = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(myDate);
        cal.add(Calendar.DATE, -fileRetentionDays);
        return cal.getTime();
    }

}
