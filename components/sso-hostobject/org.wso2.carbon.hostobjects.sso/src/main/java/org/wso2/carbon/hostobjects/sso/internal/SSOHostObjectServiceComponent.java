/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.hostobjects.sso.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.hostobjects.sso.internal.util.*;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="identity.sso.saml.component" immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class SSOHostObjectServiceComponent {

    private static Log log = LogFactory.getLog(SSOHostObjectServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("SSO host bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("SSO host bundle is deactivated");
        }
    }

    protected void setRealmService(RealmService realmService) {
        Util.setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in SSO host bundle");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the SAML SSO host bundle");
        }
    }
    protected void setConfigurationContextService(ConfigurationContextService configCtxtService) {
        SSOHostObjectDataHolder.getInstance().setConfigurationContextService(configCtxtService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxtService) {
        SSOHostObjectDataHolder.getInstance().setConfigurationContextService(null);
    }

}