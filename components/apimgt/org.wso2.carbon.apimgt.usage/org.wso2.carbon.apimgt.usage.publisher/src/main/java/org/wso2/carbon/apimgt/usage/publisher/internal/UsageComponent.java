/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @scr.component name="api.mgt.usage.component" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class UsageComponent {

    private static final Log log = LogFactory.getLog(UsageComponent.class);

    private static APIManagerConfigurationService amConfigService;

    private static Map<String, LoadBalancingDataPublisher> dataPublisherMap;

    protected void activate(ComponentContext ctx) {
        try {
            DataPublisherUtil.setEnabledMetering(
                    Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty("EnableMetering")));

            dataPublisherMap = new ConcurrentHashMap<String, LoadBalancingDataPublisher>();

            log.debug("API Management Usage Publisher bundle is activated ");
        } catch (Throwable e) {
            log.error("API Management Usage Publisher bundle ", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {

    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service bound to the API usage handler");
        amConfigService = service;
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service unbound from the API usage handler");
        amConfigService = null;
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    public static APIManagerConfigurationService getAmConfigService() {
        return amConfigService;
    }


    /**
     * Fetch the data publisher which has been registered under the tenant domain.
     * @param tenantDomain - The tenant domain under which the data publisher is registered
     * @return - Instance of the LoadBalancingDataPublisher which was registered. Null if not registered.
     */
    public static LoadBalancingDataPublisher getDataPublisher(String tenantDomain){
        if(dataPublisherMap.containsKey(tenantDomain)){
            return dataPublisherMap.get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a LoadBalancingDataPublisher to the data publisher map.
     * @param tenantDomain - The tenant domain under which the data publisher will be registered.
     * @param dataPublisher - Instance of the LoadBalancingDataPublisher
     * @throws org.wso2.carbon.apimgt.usage.publisher.internal.DataPublisherAlreadyExistsException - If a data publisher has already been registered under the
     * tenant domain
     */
    public static void addDataPublisher(String tenantDomain, LoadBalancingDataPublisher dataPublisher)
            throws DataPublisherAlreadyExistsException {
        if(dataPublisherMap.containsKey(tenantDomain)){
            throw new DataPublisherAlreadyExistsException("A DataPublisher has already been created for the tenant " +
                    tenantDomain);
        }

        dataPublisherMap.put(tenantDomain, dataPublisher);
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

}
