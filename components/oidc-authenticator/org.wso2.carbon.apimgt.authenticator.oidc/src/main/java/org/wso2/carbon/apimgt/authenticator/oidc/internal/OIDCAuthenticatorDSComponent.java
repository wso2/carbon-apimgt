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
package org.wso2.carbon.apimgt.authenticator.oidc.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.apimgt.authenticator.oidc.OIDCAuthenticator;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Hashtable;
import java.util.Map;

/**
 * @scr.component name="oidc.authenticator.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class OIDCAuthenticatorDSComponent {

    private static final Log log = LogFactory.getLog(OIDCAuthenticatorDSComponent.class);

    protected void activate(ComponentContext ctxt) {
        OIDCAuthBEDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());
        OIDCAuthenticator authenticator = new OIDCAuthenticator();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
        ctxt.getBundleContext().registerService(CarbonServerAuthenticator.class.getName(), authenticator, props);

        // Check whether the IdPCertAlias is set for signature validations of Tenant 0.
        //configureIdPCertAlias();

        if (log.isDebugEnabled()) {
            log.debug("OIDC Authenticator BE Bundle activated successfuly.");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        OIDCAuthBEDataHolder.getInstance().setBundleContext(null);
        log.debug("OIDC Authenticator BE Bundle is deactivated ");
    }

    protected void setRegistryService(RegistryService registryService) {
        OIDCAuthBEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        OIDCAuthBEDataHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        OIDCAuthBEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        OIDCAuthBEDataHolder.getInstance().setRealmService(null);
    }


}
