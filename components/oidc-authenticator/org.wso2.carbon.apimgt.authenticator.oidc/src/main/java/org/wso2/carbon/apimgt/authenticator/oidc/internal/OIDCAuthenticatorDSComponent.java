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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "oidc.authenticator.dscomponent", 
         immediate = true)
public class OIDCAuthenticatorDSComponent {

    private static final Log log = LogFactory.getLog(OIDCAuthenticatorDSComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {
        OIDCAuthBEDataHolder.getInstance().setBundleContext(ctxt.getBundleContext());
        OIDCAuthenticator authenticator = new OIDCAuthenticator();
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(CarbonConstants.AUTHENTICATOR_TYPE, authenticator.getAuthenticatorName());
        ctxt.getBundleContext().registerService(CarbonServerAuthenticator.class.getName(), authenticator, props);
        // configureIdPCertAlias();
        if (log.isDebugEnabled()) {
            log.debug("OIDC Authenticator BE Bundle activated successfuly.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        OIDCAuthBEDataHolder.getInstance().setBundleContext(null);
        log.debug("OIDC Authenticator BE Bundle is deactivated ");
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        OIDCAuthBEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        OIDCAuthBEDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        OIDCAuthBEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        OIDCAuthBEDataHolder.getInstance().setRealmService(null);
    }
}

