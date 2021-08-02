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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.api.model.Application;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.List;

public class ApplicationPurge implements OrganizationPurge{
    protected ApiMgtDAO apiMgtDAO;

    public ApplicationPurge(ApiMgtDAO apiMgtDAO) {
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    @Override
    public void deleteOrganization(String organizationId) {
        List<Application> applicationList = apiMgtDAO.getApplicationsByOrgId(organizationId);
        int[] applicationIdList = new int[applicationList.size()];

        for (int i = 0; i < applicationList.size(); i++) {
            applicationIdList[i] = applicationList.get(i).getId();
        }

        try {
            //removing pending subscriptions
            removePendingSubscriptions(applicationIdList);

            //remove application registration workflows
            removeApplicationCreationWorkflows(applicationIdList);

            // removing pending application registrations
            deletePendingApplicationRegistrations(applicationIdList);

            // removing applications list
            deleteApplicationList(applicationIdList);

            // removing subscribers
            deleteSubscribers(organizationId);

        } catch (APIManagementException e) {
            e.printStackTrace();
        }
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
        List<String> keyManagerViseProductionKeyState = apiMgtDAO.
                getPendingRegistrationsForApplicationList(applicationIdList, "PRODUCTION");

        for (String km : keyManagerViseProductionKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList,km,"PRODUCTION");
        }

        List<String> keyManagerViseSandboxKeyState = apiMgtDAO.
                getPendingRegistrationsForApplicationList(applicationIdList, "SANDBOX");

        for (String km : keyManagerViseSandboxKeyState) {
            apiMgtDAO.deleteApplicationRegistrationsWorkflowsForKeyManager(applicationIdList,km,"SANDBOX");
        }
    }

    private void deleteSubscribers(String organization) throws APIManagementException {
        //select subscribers for the organization
        List<Integer> subscriberIdList = apiMgtDAO.getSubscribersForOrganizationId(organization);
        if (subscriberIdList.size() > 0) {
            for (int subscriberId : subscriberIdList) {
                List<String> mappedOrganizations = apiMgtDAO.getMappedOrganizationListForSubscriber(subscriberId);
                apiMgtDAO.removeSubscriberOrganizationMapping(subscriberId, organization);
                if (!(mappedOrganizations.size() > 1 && mappedOrganizations.contains(organization))) {
                    Cache<String, Subscriber> subscriberCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                            .getCache(APIConstants.API_SUBSCRIBER_CACHE);
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
