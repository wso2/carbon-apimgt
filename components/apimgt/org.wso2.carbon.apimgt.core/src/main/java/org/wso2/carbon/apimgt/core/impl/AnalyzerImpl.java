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
package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.AnalyticsException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;

import java.util.List;

/**
 * Implementation class of Analyzer operations
 */
public class AnalyzerImpl implements Analyzer {

    private static final Logger log = LoggerFactory.getLogger(AnalyzerImpl.class);

    private String username;
    private AnalyticsDAO analyticsDAO;

    public AnalyzerImpl(String username, AnalyticsDAO analyticsDAO) {
        this.username = username;
        this.analyticsDAO = analyticsDAO;
    }

    @Override
    public List<ApplicationCount> getApplicationCount(String createdBy, String subscribedTo, String fromTime,
                                                      String toTime) throws APIManagementException {
        List<ApplicationCount> applicationCountList;
        try {
            applicationCountList = getAnalyticsDAO().getApplicationCount(createdBy, subscribedTo, fromTime, toTime);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching application count information";
            throw new AnalyticsException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return applicationCountList;
    }

    @Override
    public List<APIInfo> getAPIInfo(String createdBy, String fromTime,
                                    String toTime) throws APIManagementException {
        List<APIInfo> apiInfoList;
        try {
            apiInfoList = getAnalyticsDAO().getAPIInfo(createdBy, fromTime, toTime);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching API information";
            throw new AnalyticsException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiInfoList;
    }

    @Override
    public List<APICount> getAPICount(String createdBy, String fromTime, String toTime) throws APIManagementException {
        List<APICount> apiCountList;
        try {
            apiCountList = getAnalyticsDAO().getAPICount(createdBy, fromTime, toTime);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Error occurred while fetching API count information";
            throw new AnalyticsException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return apiCountList;
    }

    public AnalyticsDAO getAnalyticsDAO() {
        return analyticsDAO;
    }

    public String getUsername() {
        return username;
    }
}
