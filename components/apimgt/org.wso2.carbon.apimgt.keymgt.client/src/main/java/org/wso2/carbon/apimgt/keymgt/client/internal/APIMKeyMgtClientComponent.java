/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.client.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "api.keymgt.client.component", 
         immediate = true)
public class APIMKeyMgtClientComponent {

    private static final Log log = LogFactory.getLog(APIMKeyMgtClientComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Activating API Key Management Client Component");
        }
        try {
            String clientRepoLocation = getClientRepoLocation();
            String axis2ClientXmlLocation = getAxis2ClientXmlLocation();
            if (log.isDebugEnabled()) {
                log.debug("Creating configuration context from repository: " + clientRepoLocation + 
                         " and axis2 client xml: " + axis2ClientXmlLocation);
            }
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    clientRepoLocation, axis2ClientXmlLocation);
            ServiceReferenceHolder.getInstance().setAxis2ConfigurationContext(ctx);
            log.info("API Key Management Client Component activated successfully");
        } catch (AxisFault axisFault) {
            log.error("Failed to initialize APIMKeyMgtClientComponent", axisFault);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API Key Management Client Component");
        }
        log.info("API Key Management Client Component deactivated successfully");
    }

    private String getAxis2ClientXmlLocation() {
        String axis2ClientXml = ServerConfiguration.getInstance().getFirstProperty("Axis2Config" + 
                ".clientAxis2XmlLocation");
        if (axis2ClientXml == null && log.isDebugEnabled()) {
            log.debug("Axis2Config.clientAxis2XmlLocation property not found in server configuration");
        }
        return axis2ClientXml;
    }

    private String getClientRepoLocation() {
        String clientRepoLocation = ServerConfiguration.getInstance().getFirstProperty("Axis2Config" + 
                ".ClientRepositoryLocation");
        if (clientRepoLocation == null && log.isDebugEnabled()) {
            log.debug("Axis2Config.ClientRepositoryLocation property not found in server configuration");
        }
        return clientRepoLocation;
    }
}

