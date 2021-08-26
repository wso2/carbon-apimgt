/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cleanup.service;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationPurge implements OrganizationPurge {
    private String username;
    protected OrganizationPurgeDAO organizationPurgeDAO;
    private static final Log log = LogFactory.getLog(ApplicationPurge.class);
    LinkedHashMap<String, String> applicationPurgeTaskMap = new LinkedHashMap<>();

    private void initTaskList() {
        applicationPurgeTaskMap.put(APIConstants.OrganizationDeletion.PENDING_SUBSCRIPTION_REMOVAL,
                APIConstants.OrganizationDeletion.PENDING);
        applicationPurgeTaskMap.put(APIConstants.OrganizationDeletion.APPLICATION_CREATION_WF_REMOVAL,
                APIConstants.OrganizationDeletion.PENDING);
        applicationPurgeTaskMap.put(APIConstants.OrganizationDeletion.APPLICATION_REGISTRATION_REMOVAL,
                APIConstants.OrganizationDeletion.PENDING);
        applicationPurgeTaskMap.put(APIConstants.OrganizationDeletion.APPLICATION_REMOVAL,
                APIConstants.OrganizationDeletion.PENDING);
    }

    public ApplicationPurge(String username) {
        this.username = username;
        organizationPurgeDAO = OrganizationPurgeDAO.getInstance();
        initTaskList();
    }

    public ApplicationPurge(OrganizationPurgeDAO organizationPurgeDAO, String username) {
        this(username);
        this.organizationPurgeDAO = organizationPurgeDAO;
    }

    /**
     * Delete organization related application data
     *
     * @param organization organization
     */
    @Override
    public LinkedHashMap<String, String> purge(String organization) {
        for (Map.Entry<String, String> task : applicationPurgeTaskMap.entrySet()) {
            int count = 0;
            int maxTries = 3;
            while (true) {
                try {
                    switch (task.getKey()) {
                    case APIConstants.OrganizationDeletion.PENDING_SUBSCRIPTION_REMOVAL:
                        removePendingSubscriptions(organization);
                        break;
                    case APIConstants.OrganizationDeletion.APPLICATION_CREATION_WF_REMOVAL:
                        removeApplicationCreationWorkflows(organization);
                        break;
                    case APIConstants.OrganizationDeletion.APPLICATION_REGISTRATION_REMOVAL:
                        deletePendingApplicationRegistrations(organization);
                        break;
                    case APIConstants.OrganizationDeletion.APPLICATION_REMOVAL:
                        deleteApplicationList(organization);
                        break;
                    }
                    applicationPurgeTaskMap.put(task.getKey(), APIConstants.OrganizationDeletion.COMPLETED);
                    break;
                } catch (APIManagementException e) {
                    log.error("Error while deleting Application Data in organization " + organization, e);
                    applicationPurgeTaskMap.put(task.getKey(), APIConstants.OrganizationDeletion.FAIL);
                    log.info("Re-trying to execute " + task.getKey() + " process for organization" + organization, e);

                    if (++count == maxTries) {
                        log.error("Cannot execute " + task.getKey() + " process for organization" + organization, e);
                        applicationPurgeTaskMap.put(task.getKey(), e.getMessage());
                        break;
                    }

                }
            }
        }

        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.ORGANIZATION, new Gson().toJson(applicationPurgeTaskMap),
                APIConstants.AuditLogConstants.DELETED, username);
        return applicationPurgeTaskMap;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private void removePendingSubscriptions(String organization) throws APIManagementException {
        organizationPurgeDAO.removePendingSubscriptions(organization);
    }

    private void removeApplicationCreationWorkflows(String organization) throws APIManagementException {
        organizationPurgeDAO.removeApplicationCreationWorkflows(organization);
    }

    private void deletePendingApplicationRegistrations(String organization) throws APIManagementException {
        organizationPurgeDAO.deletePendingApplicationRegistrations(organization);
    }

    private void deleteApplicationList(String organization) throws APIManagementException {
        organizationPurgeDAO.deleteApplicationList(organization);
    }
}
