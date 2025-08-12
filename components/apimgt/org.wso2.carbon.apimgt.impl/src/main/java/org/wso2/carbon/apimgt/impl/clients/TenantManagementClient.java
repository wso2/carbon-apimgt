/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * Client to invoke TenantMgtAdminService for tenant related tasks.
 */
public class TenantManagementClient {
    private static final Log log = LogFactory.getLog(TenantManagementClient.class);
    private static final String TENANT_MANAGEMENT_ADMIN_SERVICE = "/services/TenantMgtAdminService";
    private TenantMgtAdminServiceStub stub;

    public TenantManagementClient() throws APIManagementException {
        
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        
        EventHubConfigurationDto eventHubConfigurationDto = config.getEventHubConfigurationDto();
        if (eventHubConfigurationDto == null) {
            throw new APIManagementException("Event hub configuration not found");
        }
        String username = eventHubConfigurationDto.getUsername();
        String password = eventHubConfigurationDto.getPassword();
        String url = eventHubConfigurationDto.getServiceUrl();
        if (username == null || password == null || url == null) {
           throw new APIManagementException("Event hub configuration is incomplete");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating TenantMgtAdminServiceStub with URL: " + url + TENANT_MANAGEMENT_ADMIN_SERVICE);
            }
            stub = new TenantMgtAdminServiceStub(url + TENANT_MANAGEMENT_ADMIN_SERVICE);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setCallTransportCleanup(true);
            options.setManageSession(true);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, client);
        } catch (AxisFault axisFault) {
            log.error("Error while accessing tenant management admin service", axisFault);
            throw new APIManagementException("Error while accessing tenant management admin service ", axisFault);
        }

    }

    public void addTenant(String firstName, String lastName, String adminUserName, String adminPassword, String email,
            String tenantDomain, boolean isActive) throws APIManagementException {
        
        if (log.isDebugEnabled()) {
            log.debug("Adding tenant with domain: " + tenantDomain);
        }   
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setTenantDomain(tenantDomain);
        tenantInfoBean.setAdmin(adminUserName);
        tenantInfoBean.setAdminPassword(adminPassword);
        tenantInfoBean.setEmail(email);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(lastName);
        tenantInfoBean.setActive(isActive);

        try {
            stub.addTenant(tenantInfoBean);
        } catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
            log.error("Error while creating tenant with domain: " + tenantDomain, e);
            throw new APIManagementException("Error while creating tenant ", e);
        }

    }

    public void updateTenant(String firstName, String lastName, String adminPassword,
            String email, String tenantDomain, boolean isActive) throws APIManagementException {
        
        if (log.isDebugEnabled()) {
            log.debug("Updating tenant with domain: " + tenantDomain);
        }
        try {
            // get the existing tenant with tenant ID
            TenantInfoBean tenantInfoInAPIM = stub.getTenant(tenantDomain);
            if (tenantInfoInAPIM == null) {
                throw new APIManagementException("Tenant not found: " + tenantDomain);
            }
            
            tenantInfoInAPIM.setAdminPassword(adminPassword);
            tenantInfoInAPIM.setEmail(email);
            tenantInfoInAPIM.setFirstname(firstName);
            tenantInfoInAPIM.setLastname(lastName);
            tenantInfoInAPIM.setActive(isActive);
            stub.updateTenant(tenantInfoInAPIM);
        } catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
            log.error("Error while updating tenant with domain: " + tenantDomain, e);
            throw new APIManagementException("Error while updating tenant ", e);
        }

    }

    public void activateTenant(String tenantDomain) throws APIManagementException {
        
        if (log.isDebugEnabled()) {
            log.debug("Activating tenant with domain: " + tenantDomain);
        }
        try {
            stub.activateTenant(tenantDomain);
        } catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
            log.error("Error while activating tenant with domain: " + tenantDomain, e);
            throw new APIManagementException("Error while activating tenant ", e);
        }
    }

    public void deactivateTenant(String tenantDomain) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Deactivating tenant with domain: " + tenantDomain);
        }
        try {
            stub.deactivateTenant(tenantDomain);
        } catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
            log.error("Error while deactivating tenant with domain: " + tenantDomain, e);
            throw new APIManagementException("Error while deactivating tenant ", e);
        }
    }
}
