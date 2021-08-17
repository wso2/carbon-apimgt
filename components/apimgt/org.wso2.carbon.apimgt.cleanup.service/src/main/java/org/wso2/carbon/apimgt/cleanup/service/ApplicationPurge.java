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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.*;

public class ApplicationPurge implements OrganizationPurge{
    protected ApiMgtDAO apiMgtDAO;
    protected OrganizationPurgeDAO organizationPurgeDAO;
    private static final Log log = LogFactory.getLog(ApplicationPurge.class);
    LinkedHashMap<String, String> applicationPurgeTaskMap = new LinkedHashMap<>();

    public ApplicationPurge() {
        apiMgtDAO = ApiMgtDAO.getInstance();
        organizationPurgeDAO = OrganizationPurgeDAO.getInstance();
    }

    public ApplicationPurge(ApiMgtDAO apiMgtDAO, OrganizationPurgeDAO organizationPurgeDAO) {
        this.organizationPurgeDAO = organizationPurgeDAO;
    }
    /**
     * Delete organization related application data
     *
     * @param organization organization
     */
    @Override
    public LinkedHashMap<String, String> deleteOrganization(String organization) {
        try {
            removePendingSubscriptions(organization);

            removeApplicationCreationWorkflows(organization);

            deletePendingApplicationRegistrations(organization);

            deleteApplicationList(organization);

            deleteSubscribers(organization);
        } catch (APIManagementException e) {
            log.error(e);
        }
        return applicationPurgeTaskMap;
    }

    @Override public int getPriority() {
        return 0;
    }

    private void removeApplicationCreationWorkflows(String organization) throws APIManagementException {
        organizationPurgeDAO.removeApplicationCreationWorkflows(organization);
    }

    private void removePendingSubscriptions(String organization) throws APIManagementException {
        organizationPurgeDAO.removePendingSubscriptions(organization);
    }

    private void deleteApplicationList(String organization) throws APIManagementException {
        organizationPurgeDAO.deleteApplicationList(organization);
    }

    // cleanup pending application regs
    private void deletePendingApplicationRegistrations(String organization) throws APIManagementException {
        organizationPurgeDAO.deletePendingApplicationRegistrations(organization);
    }

    private void deleteSubscribers(String organization) throws APIManagementException {

        //select subscribers for the organization
        List<Integer> subscriberIdList = organizationPurgeDAO.getSubscribersForOrganization(organization);

        if (subscriberIdList.size() > 0) {
            for (int subscriberId : subscriberIdList) {

                List<String> mappedOrganizations = organizationPurgeDAO.getOrganizationsOfSubscriber(subscriberId);
                organizationPurgeDAO.removeOrganizationFromSubscriber(subscriberId, organization);

                if (!(mappedOrganizations.size() > 1 && mappedOrganizations.contains(organization))) {

                    Cache<String, Subscriber> subscriberCache = Caching.getCacheManager(
                            APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.API_SUBSCRIBER_CACHE);

                    Subscriber subscriber = apiMgtDAO.getSubscriber(subscriberId);

                    if (subscriberCache.get(subscriber.getName()) != null) {
                        subscriberCache.remove(subscriber.getName());
                    }

                    organizationPurgeDAO.removeSubscriber(subscriberId);
                }
            }
        }
    }

}
