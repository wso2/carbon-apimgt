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
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIUtils;

/**
 * 
 *
 */
public class UserAwareAPIStore extends APIStoreImpl {

    private static final Logger log = LoggerFactory.getLogger(UserAwareAPIStore.class);

    public UserAwareAPIStore(String username, ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, PolicyDAO policyDAO) {
        super(username, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO);
    }

    @Override
    public void deleteApplication(String appId) throws APIManagementException {
        try {
            Application application = getApplicationDAO().getApplication(appId);
            if (application != null && application.getCreatedUser().equals(getUsername())) {
                super.deleteApplication(appId);
            } else {
                APIUtils.logAndThrowException("Could not find application - " + appId, log);
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while deleting application - " + appId, e, log);
        }
    }

    @Override
    public void updateApplication(String uuid, Application application) throws APIManagementException {
        try {
            Application oldApplication = getApplicationDAO().getApplication(uuid);
            if (oldApplication != null && oldApplication.getCreatedUser().equals(getUsername())) {
                super.updateApplication(uuid, application);
            } else {
                APIUtils.logAndThrowException("Could not find Application - " + uuid, log);
            }
        } catch (APIMgtDAOException e) {
            APIUtils.logAndThrowException("Error occurred while updating application - " + uuid, e, log);
        }
    }
}
