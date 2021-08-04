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

        taskList.put("ApplicationRetrieval",null);
        taskList.put("PendingSubscriptionRemoval",null);
        taskList.put("ApplicationCreationWFRemoval",null);
        taskList.put("ApplicationRegistrationRemoval",null);
        taskList.put("ApplicationRemoval",null);
        taskList.put("SubscriberRemoval",null);

    }

    /**
     * Delete organization related application data
     *
     * @param organization organization
     * @throws APIManagementException if failed to cleanup organization
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
                    case "ApplicationRetrieval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            applicationIdList = getApplicationsByOrganization(organization);
                            taskList.put(task, "Successful");
                        }
                        break;
                    case "PendingSubscriptionRemoval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            removePendingSubscriptions(applicationIdList);
                            taskList.put(task, "Successful");
                        }
                        break;
                    case "ApplicationCreationWFRemoval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            removeApplicationCreationWorkflows(applicationIdList);
                            taskList.put(task, "Successful");
                        }
                        break;
                    case "ApplicationRegistrationRemoval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            deletePendingApplicationRegistrations(applicationIdList);
                            taskList.put(task, "Successful");
                        }
                        break;
                    case "ApplicationRemoval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            deleteApplicationList(applicationIdList);
                            taskList.put(task, "Successful");
                        }
                        break;
                    case "SubscriberRemoval":
                        if (taskList.get(task) == null || taskList.get(task).equals("Failed")) {
                            deleteSubscribers(organization);
                            taskList.put(task, "Successful");
                        }
                        break;
                    }
                    break;
                } catch (APIManagementException e) {
                    log.error("Error in " + task + " in application deletion sub component", e);
                    String msg = "Failed to execute application deletion of organization: " + organization;

                    if (++count == maxTries) {
                        taskList.put(task, "Failed");
                        throw new APIManagementException(msg, e);
                    }
                }
            }
        }
    }

    private int[] getApplicationsByOrganization(String organization) throws APIManagementException {
        List<Application> applicationList = apiMgtDAO.getApplicationsByOrganization(organization);
        int[] applicationIdList = new int[applicationList.size()];

        for (int i = 0; i < applicationList.size(); i++) {
            applicationIdList[i] = applicationList.get(i).getId();
        }
        return applicationIdList;
    }

    private void removeApplicationCreationWorkflows(int[] applicationIdList) throws APIManagementException {
        apiMgtDAO.removeApplicationCreationWorkflows(applicationIdList);
    }

    private void removePendingSubscriptions(int[] applicationIdList) throws APIManagementException {
        apiMgtDAO.removePendingSubscriptions(applicationIdList);
    }

    private void deleteApplicationList(int[] applicationIdList) throws APIManagementException {
        apiMgtDAO.deleteApplicationList(applicationIdList);
    }

    // cleanup pending application regs
    private void deletePendingApplicationRegistrations(int[] applicationIdList) throws APIManagementException {
        List<String> keyManagerViseProductionKeyState = apiMgtDAO.getPendingRegistrationsForApplicationList(
                applicationIdList, "PRODUCTION");

        for (String km : keyManagerViseProductionKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList, km, "PRODUCTION");
        }

        List<String> keyManagerViseSandboxKeyState = apiMgtDAO.getPendingRegistrationsForApplicationList(
                applicationIdList, "SANDBOX");

        for (String km : keyManagerViseSandboxKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList, km, "SANDBOX");
        }
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
