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
package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.api.model.Application;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.*;

public class ApplicationPurge implements OrganizationPurge{
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(ApplicationPurge.class);
    private Map<String, String> taskList;

    private final int MAX_TRIES = 5;

    public ApplicationPurge() {
        apiMgtDAO = ApiMgtDAO.getInstance();
        init();
    }

    public ApplicationPurge(ApiMgtDAO apiMgtDAO) {
        this.apiMgtDAO = apiMgtDAO;
        init();
    }

    private void init() {
        taskList = new LinkedHashMap<>();

        taskList.put(APIConstants.ApplicationPurgeConstants.PENDING_SUBSCRIPTION_REMOVAL,
                APIConstants.ApplicationPurgeConstants.STATUS_PENDING);
        taskList.put(APIConstants.ApplicationPurgeConstants.APPLICATION_CREATION_WF_REMOVAL,
                APIConstants.ApplicationPurgeConstants.STATUS_PENDING);
        taskList.put(APIConstants.ApplicationPurgeConstants.APPLICATION_REGISTRATION_REMOVAL,
                APIConstants.ApplicationPurgeConstants.STATUS_PENDING);
        taskList.put(APIConstants.ApplicationPurgeConstants.APPLICATION_REMOVAL,
                APIConstants.ApplicationPurgeConstants.STATUS_PENDING);
        taskList.put(APIConstants.ApplicationPurgeConstants.SUBSCRIPTION_REMOVAL,
                APIConstants.ApplicationPurgeConstants.STATUS_PENDING);
    }

    /**
     * Delete organization related application data
     *
     * @param organization organization
     * @throws APIManagementException if failed to clean up organization
     */
    @Override
    public void deleteOrganization(String organization) throws APIManagementException {
        int[] applicationIdList = null;
        for (String task : taskList.keySet()) {

            int count = 0;
            int maxTries = MAX_TRIES;

            while (true) {
                try {
                    switch (task) {
                    case APIConstants.ApplicationPurgeConstants.PENDING_SUBSCRIPTION_REMOVAL:
                        if (taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_PENDING)
                                || taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_FAILED)) {
                            removePendingSubscriptions(organization);
                            taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_SUCCESSFUL);
                        }
                        break;
                    case APIConstants.ApplicationPurgeConstants.APPLICATION_CREATION_WF_REMOVAL:
                        if (taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_PENDING)
                                || taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_FAILED)) {
                            removeApplicationCreationWorkflows(organization);
                            taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_SUCCESSFUL);
                        }
                        break;
                    case APIConstants.ApplicationPurgeConstants.APPLICATION_REGISTRATION_REMOVAL:
                        if (taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_PENDING)
                                || taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_FAILED)) {
                            deletePendingApplicationRegistrations(organization);
                            taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_SUCCESSFUL);
                        }
                        break;
                    case APIConstants.ApplicationPurgeConstants.APPLICATION_REMOVAL:
                        if (taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_PENDING)
                                || taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_FAILED)) {
                            deleteApplicationList(organization);
                            taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_SUCCESSFUL);
                        }
                        break;
                    case APIConstants.ApplicationPurgeConstants.SUBSCRIPTION_REMOVAL:
                        if (taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_PENDING)
                                || taskList.get(task).equals(APIConstants.ApplicationPurgeConstants.STATUS_FAILED)) {
                            deleteSubscribers(organization);
                            taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_SUCCESSFUL);
                        }
                        break;
                    }
                    break;
                } catch (APIManagementException e) {
                    log.error("Error in " + task + " in application deletion sub component", e);
                    String msg = "Failed to execute application deletion of organization: " + organization;
                    taskList.put(task, APIConstants.ApplicationPurgeConstants.STATUS_FAILED);
                    if (++count == maxTries) {
                        throw new APIManagementException(msg, e);
                    }
                }
            }
        }
    }

    private void removeApplicationCreationWorkflows(String organization) throws APIManagementException {
        apiMgtDAO.removeApplicationCreationWorkflows(organization);
    }

    private void removePendingSubscriptions(String organization) throws APIManagementException {
        apiMgtDAO.removePendingSubscriptions(organization);
    }

    private void deleteApplicationList(String organization) throws APIManagementException {
        apiMgtDAO.deleteApplicationList(organization);
    }

    // cleanup pending application regs
    private void deletePendingApplicationRegistrations(String organization) throws APIManagementException {
        apiMgtDAO.deletePendingApplicationRegistrations(organization);
    }

    private void deleteSubscribers(String organization) throws APIManagementException {

        //select subscribers for the organization
        List<Integer> subscriberIdList = apiMgtDAO.getSubscribersForOrganization(organization);

        if (subscriberIdList.size() > 0) {
            for (int subscriberId : subscriberIdList) {

                List<String> mappedOrganizations = apiMgtDAO.getMappedOrganizationListForSubscriber(subscriberId);
                apiMgtDAO.removeSubscriberOrganizationMapping(subscriberId, organization);

                if (!(mappedOrganizations.size() > 1 && mappedOrganizations.contains(organization))) {

                    Cache<String, Subscriber> subscriberCache = Caching.getCacheManager(
                            APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.API_SUBSCRIBER_CACHE);

                    Subscriber subscriber = apiMgtDAO.getSubscriber(subscriberId);

                    if (subscriberCache.get(subscriber.getName()) != null) {
                        subscriberCache.remove(subscriber.getName());
                    }

                    apiMgtDAO.removeSubscriber(subscriberId);
                }
            }
        }
    }

}
