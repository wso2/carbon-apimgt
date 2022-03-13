/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ScopeEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.GroupId;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyManagerDataServiceImpl implements KeyManagerDataService {

    private static final Log log = LogFactory.getLog(KeyManagerDataServiceImpl.class);

    @Override
    public void addOrUpdateApplication(ApplicationEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update Application in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateApplication(getApplicationFromApplicationEvent(event));
    }

    @Override
    public void addOrUpdateAPI(APIEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update API in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateAPIWithUrlTemplates(getAPIFromAPIEvent(event));
    }

    @Override
    public void addOrUpdateSubscription(SubscriptionEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update Subscription in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateSubscription(getSubscriptionFromSubscriptionEvent(event));
    }

    @Override
    public void addOrUpdateApplicationKeyMapping(ApplicationRegistrationEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update Application keymapping in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateApplicationKeyMapping(getApplicationKeyMappingFromApplicationRegistrationEvent(event));

    }

    @Override
    public void addOrUpdateSubscriptionPolicy(SubscriptionPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update Subscription Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateSubscriptionPolicy(getSubscriptionPolicyFromSubscriptionPolicyEvent(event));

    }

    @Override
    public void addOrUpdateApplicationPolicy(ApplicationPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update Application Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateApplicationPolicy(getApplicationPolicyFromApplicationPolicyEvent(event));
    }

    @Override
    public void addOrUpdateAPIPolicy(APIPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Add or Update API Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateApiPolicy(getAPIPolicyFromAPIPolicyEvent(event));
    }

    @Override
    public void removeApplication(ApplicationEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove Application in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeApplication(getApplicationFromApplicationEvent(event));
    }

    @Override
    public void removeAPI(APIEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove API in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeAPI(getAPIFromAPIEvent(event));
    }

    @Override
    public void removeSubscription(SubscriptionEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove Subscription in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeSubscription(getSubscriptionFromSubscriptionEvent(event));
    }

    @Override
    public void removeApplicationKeyMapping(ApplicationRegistrationEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove ApplicationKey Mapping in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeApplicationKeyMapping(getApplicationKeyMappingFromApplicationRegistrationEvent(event));
    }

    @Override
    public void removeSubscriptionPolicy(SubscriptionPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove Subscription Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeSubscriptionPolicy(getSubscriptionPolicyFromSubscriptionPolicyEvent(event));
    }

    @Override
    public void removeApplicationPolicy(ApplicationPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove Application Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeApplicationPolicy(getApplicationPolicyFromApplicationPolicyEvent(event));
    }

    @Override
    public void removeAPIPolicy(APIPolicyEvent event) {

        if (log.isDebugEnabled()) {
            log.debug("Remove API Policy in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.removeApiPolicy(getAPIPolicyFromAPIPolicyEvent(event));
    }

    @Override
    public void addScope(ScopeEvent event) {

        Scope scope = new Scope();
        scope.setName(event.getName());
        scope.setRoles(event.getRoles());
        scope.setDisplayName(event.getDisplayName());
        scope.setDescription(event.getDescription());
        scope.setTimeStamp(event.getTimeStamp());
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the event as the tenant " + event.getTenantDomain() + " is not loaded");
            }
            return;
        }
        store.addOrUpdateScope(scope);
    }

    @Override
    public void deleteScope(ScopeEvent event) {

        Scope scope = new Scope();
        scope.setName(event.getName());
        scope.setTimeStamp(event.getTimeStamp());
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the event as the tenant " + event.getTenantDomain() + " is not loaded");
            }
            return;
        }
        store.deleteScope(scope);
    }

    private ApplicationKeyMapping getApplicationKeyMappingFromApplicationRegistrationEvent(
            ApplicationRegistrationEvent event) {

        ApplicationKeyMapping mapping = new ApplicationKeyMapping();
        mapping.setApplicationId(event.getApplicationId());
        mapping.setApplicationUUID(event.getApplicationUUID());
        mapping.setConsumerKey(event.getConsumerKey());
        mapping.setKeyType(event.getKeyType());
        mapping.setKeyManager(event.getKeyManager());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + mapping.toString());
        }
        return mapping;
    }

    private Application getApplicationFromApplicationEvent(ApplicationEvent event) {

        Application application = new Application();
        application.setId(event.getApplicationId());
        application.setName(event.getApplicationName());
        application.setPolicy(event.getApplicationPolicy());
        application.setTokenType(event.getTokenType());
        application.setUUID(event.getUuid());
        application.setOrganization(event.getTenantDomain());
        event.getAttributes().forEach(application::addAttribute);
        application.setSubName(event.getSubscriber());
        //add group ids list to application
        if (!StringUtils.isEmpty(event.getGroupId())) {
            String[] groupIdArray = event.getGroupId().split(",");
            List<GroupId> groupIdList = Arrays.asList(groupIdArray).stream().map(id -> {
                GroupId groupId = new GroupId();
                groupId.setApplicationId(event.getApplicationId());
                groupId.setGroupId(id);
                return groupId;
            }).collect(Collectors.toList());
        }
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + application.toString());
        }
        return application;
    }

    private Subscription getSubscriptionFromSubscriptionEvent(SubscriptionEvent event) {

        Subscription sub = new Subscription();
        sub.setApiId(event.getApiId());
        sub.setAppId(event.getApplicationId());
        sub.setPolicyId(event.getPolicyId());
        sub.setSubscriptionId(String.valueOf(event.getSubscriptionId()));
        sub.setSubscriptionState(event.getSubscriptionState());
        sub.setApiUUID(event.getApiUUID());
        sub.setApplicationUUID(event.getApplicationUUID());
        sub.setSubscriptionUUId(event.getSubscriptionUUID());
        sub.setTimeStamp(event.getTimeStamp());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + sub.toString());
        }
        return sub;
    }

    private API getAPIFromAPIEvent(APIEvent event) {

        API api = new API();
        api.setUuid(event.getUuid());
        api.setApiId(event.getApiId());
        api.setApiName(event.getApiName());
        api.setApiProvider(event.getApiProvider());
        api.setApiVersion(event.getApiVersion());
        api.setContext(event.getApiContext());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + api.toString());
        }
        return api;
    }

    private ApplicationPolicy getApplicationPolicyFromApplicationPolicyEvent(ApplicationPolicyEvent event) {

        ApplicationPolicy policy = new ApplicationPolicy();
        policy.setId(event.getPolicyId());
        policy.setQuotaType(event.getQuotaType());
        policy.setTenantId(event.getTenantId());
        policy.setTierName(event.getPolicyName());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + policy.toString());
        }
        return policy;
    }

    private SubscriptionPolicy getSubscriptionPolicyFromSubscriptionPolicyEvent(SubscriptionPolicyEvent event) {

        SubscriptionPolicy policy = new SubscriptionPolicy();
        policy.setId(event.getPolicyId());
        policy.setQuotaType(event.getQuotaType());
        policy.setRateLimitCount(event.getRateLimitCount());
        policy.setRateLimitTimeUnit(event.getRateLimitTimeUnit());
        policy.setStopOnQuotaReach(event.isStopOnQuotaReach());
        policy.setTenantId(event.getTenantId());
        policy.setTierName(event.getPolicyName());
        policy.setGraphQLMaxComplexity(event.getGraphQLMaxComplexity());
        policy.setGraphQLMaxDepth(event.getGraphQLMaxDepth());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + policy.toString());
        }
        return policy;
    }

    private ApiPolicy getAPIPolicyFromAPIPolicyEvent(APIPolicyEvent event) {

        ApiPolicy policy = new ApiPolicy();
        policy.setId(event.getPolicyId());
        policy.setTierName(event.getPolicyName());
        policy.setTenantId(event.getTenantId());
        if (log.isDebugEnabled()) {
            log.debug("Event: " + event.toString());
            log.debug("Converted : " + policy.toString());
        }
        return policy;
    }

    @Override
    public void updateDeployedAPIRevision(DeployAPIInGatewayEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Add or Update API in datastore in tenant " + event.getTenantDomain());
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(event.getTenantDomain());
        if (store == null) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring the Event due to tenant " + event.getTenantDomain() + " not loaded");
            }
            return;
        }
        store.addOrUpdateAPIRevisionWithUrlTemplates(event);
    }
}
