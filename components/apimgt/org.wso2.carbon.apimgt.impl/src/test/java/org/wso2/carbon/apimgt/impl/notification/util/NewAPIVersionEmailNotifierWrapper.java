/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.notification.util;

import org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NewAPIVersionEmailNotifierWrapper extends NewAPIVersionEmailNotifier {
    Registry registry;
    ClaimsRetriever claimsRetriever;

    public NewAPIVersionEmailNotifierWrapper(Registry registry, ClaimsRetriever claimsRetriever) {
        this.registry = registry;
        this.claimsRetriever = claimsRetriever;
    }

    @Override
    protected String getTenantDomain() {
        return "carbon.admin";
    }

    @Override
    protected void getOutputEventAdapterTypes() {
        return;
    }

    @Override
    protected void createOutputEventAdapterService(OutputEventAdapterConfiguration outputEventAdapterConfiguration)
            throws OutputEventAdapterException {
        return;
    }

    @Override
    public Set<String> getNotifierSet(NotificationDTO notificationDTO) throws NotificationException {
        String email = "admin@apim.com";
        Set<String> notifierSet = new HashSet<String>();
        notifierSet.add(email);
        return notifierSet;
    }

    @Override
    protected ClaimsRetriever getClaimsRetriever(String claimsRetrieverImplClass)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return claimsRetriever;
    }

    @Override
    public NotificationDTO loadMessageTemplate(NotificationDTO notificationDTO) throws NotificationException {
        notificationDTO.setTitle("New Version Released");
        notificationDTO.setMessage("<html></html>");

        return notificationDTO;
    }

    @Override
    protected void publishNotification(Map<String, String> emailProperties, String adapterName, String message) {
        return;
    }

    @Override
    protected Registry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return registry;
    }
}
