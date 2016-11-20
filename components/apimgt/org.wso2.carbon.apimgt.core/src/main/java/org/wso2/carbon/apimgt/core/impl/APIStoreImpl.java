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
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of API Store operations.
 *
 */
public class APIStoreImpl extends AbstractAPIManager implements APIStore {

    private static final Logger log = LoggerFactory.getLogger(APIStoreImpl.class);

    public APIStoreImpl(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO,
            APISubscriptionDAO apiSubscriptionDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, new APILifeCycleManagerImpl());
    }

    @Override public List<API> getAllAPIsByStatus(int offset, int limit, String[] statuses)
            throws APIManagementException {
        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().getAPIsByStatus(new ArrayList<>(Arrays.asList(statuses)));
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException(
                    "Error occurred while fetching APIs for the given statuses - " + Arrays.toString(statuses), e, log);
        }
        return apiResults;
    }

    @Override public Application getApplicationsByName(String userId, String applicationName, java.lang.String groupId)
            throws APIManagementException {
        Application application = null;
        try {
            application = getApplicationDAO().getApplicationByName(userId, applicationName, groupId);
        } catch (SQLException e) {
            APIUtils.logAndThrowException(
                    "Error occurred while fetching application for the given applicationName - " + applicationName
                            + " with groupId - " + groupId, e, log);
        }
        return application;
    }

    @Override public Application[] getApplications(String subscriber, String groupId) throws APIManagementException {
        Application[] applicationList = null;
        try {
            applicationList = getApplicationDAO().getApplications(subscriber, groupId);
        } catch (SQLException e) {
            APIUtils.logAndThrowException(
                    "Error occurred while fetching applications for the given subscriber - " + subscriber
                            + " with groupId - " + groupId, e, log);
        }
        return applicationList;
    }

    @Override public void updateApplication(String uuid, Application application) throws APIManagementException {
        try {
            getApplicationDAO().updateApplication(uuid, application);
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while updating application - " + application.getName(), e,
                    log);
        }
    }

    @Override public Map<String, Object> requestApprovalForApplicationRegistration(String userId,
            String applicationName, String tokenType, String callbackUrl, String[] allowedDomains, String validityTime,
            String tokenScope, String groupingId, String jsonString) throws APIManagementException {
        return null;
    }

    public List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException {

        List<API> apiResults = null;
        try {
            apiResults = getApiDAO().searchAPIs(query);
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while updating searching APIs - " + query, e, log);
        }

        return apiResults;
    }

    @Override public void deleteApplication(Application application) throws APIManagementException {
        try {
            getApplicationDAO().deleteApplication(application.getUuid());
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while deleting application - " + application.getName(), e,
                    log);
        }
    }

    @Override public String addApplication(Application application) throws APIManagementException {
        String applicationUuid = null;
        try {
            if (getApplicationDAO().isApplicationNameExists(application.getName())) {
                handleResourceAlreadyExistsException(
                        "An application already exists with a duplicate name - " + application.getName());
            }
            String generatedUuid = UUID.randomUUID().toString();
            application.setUuid(generatedUuid);
            getApplicationDAO().addApplication(application);
            applicationUuid = application.getUuid();
        } catch (SQLException e) {
            APIUtils.logAndThrowException("Error occurred while adding application - " + application.getName(), e, log);
        }
        return applicationUuid;
        //// TODO: 16/11/16 Workflow related implementation has to be done 
    }

}
