/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticator;
import org.wso2.carbon.apimgt.multitenant.auth.MultiTenantJITProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;

import java.util.Hashtable;

/**
 * OSGi Declarative Services component that registers the
 * {@link MultiTenantAuthenticator} and binds required services.
 */
@Component(name = "MultiTenantAuthServiceComponent", immediate = true)
public class MultiTenantAuthServiceComponent {

    private static final Log LOG = LogFactory.getLog(MultiTenantAuthServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            MultiTenantAuthenticator multiTenantAuthenticator = new MultiTenantAuthenticator();
            Hashtable<String, String> props = new Hashtable<>();
            componentContext.getBundleContext().registerService(
                    ApplicationAuthenticator.class.getName(), multiTenantAuthenticator, props);

            // Register the Multi Tenant JIT Provisioning Handler
            MultiTenantJITProvisioningHandler jitProvisioningHandler = new MultiTenantJITProvisioningHandler();
            Hashtable<String, String> jitHandlerProps = new Hashtable<>();
            componentContext.getBundleContext().registerService(
                    PostAuthenticationHandler.class.getName(), jitProvisioningHandler, jitHandlerProps);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Multi Tenant Authenticator bundle is activated.");
            }
        } catch (Exception e) {
            LOG.error("Error while activating Multi Tenant Authenticator.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Multi Tenant Authenticator bundle is deactivated.");
        }
    }

    @Reference(
            name = "identity.application.management.component",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService"
    )
    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the ApplicationManagementService.");
        }
        MultiTenantAuthDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the ApplicationManagementService.");
        }
        MultiTenantAuthDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(
            name = "identity.oauth.admin.service",
            service = OAuthAdminServiceImpl.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuthAdminService"
    )
    protected void setOAuthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the OAuthAdminService.");
        }
        MultiTenantAuthDataHolder.getInstance().setOAuthAdminService(oAuthAdminService);
    }

    protected void unsetOAuthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the OAuthAdminService.");
        }
        MultiTenantAuthDataHolder.getInstance().setOAuthAdminService(null);
    }

}
