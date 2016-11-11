/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscriber;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of API Store operations.
 *
 */
public class APIStoreImpl extends AbstractAPIManager implements APIStore {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    public APIStoreImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO,new APILifeCycleManagerImpl());
    }

    @Override
    public Map<String, Object> getAllAPIsByStatus(int offset, int limit, String[] status, boolean returnAPITags)
            throws APIManagementException {

        return null;
    }

    @Override
    public Application getApplicationsByName(String userId, String applicationName, String groupId)
            throws APIManagementException {
        Application application = null;
        try {
            application = getApplicationDAO().getApplicationByName(userId, applicationName, groupId);
        } catch (SQLException e) {
            APIUtils.logAndThrowException(
                    "Error occurred while fetching application for the given applicationName - " + applicationName, e,
                    log);
        }
        return application;
    }

    @Override public Application[] getApplications(Subscriber subscriber, String groupingId)
            throws APIManagementException {
        return new Application[0];
    }

    @Override public void updateApplication(Application application) throws APIManagementException {

    }

    @Override public Map<String, Object> requestApprovalForApplicationRegistration(String userId,
            String applicationName, String tokenType, String callbackUrl, String[] allowedDomains, String validityTime,
            String tokenScope, String groupingId, String jsonString) throws APIManagementException {
        return null;
    }

    public APISummaryResults searchAPIs(String query, int offset, int limit)
            throws APIManagementException {

        APISummaryResults apiSummaryResults = null;
        try {
            List<String> roles = new ArrayList<>(); // TODO -- roles list
            apiSummaryResults = getApiDAO().searchAPIsForRoles(query, offset, limit,
                    roles);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while updating searching APIs - " + query, e, log);
        }

        return apiSummaryResults;
    }


    @Override public void removeApplication(Application application) throws APIManagementException {

    }

    @Override public String addApplication(Application application, String userId) throws APIManagementException {
        return null;
    }

  }
