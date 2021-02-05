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

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ServiceCatalog;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.ServiceFilterParams;
import org.wso2.carbon.apimgt.impl.dao.ServiceCatalogDAO;

import java.util.List;
import java.util.UUID;

public class ServiceCatalogImpl implements ServiceCatalog {

    private ServiceCatalogDAO catalogDAO = ServiceCatalogDAO.getInstance();

    @Override
    public String addService(ServiceEntry serviceEntry, int tenantId, String user) throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        catalogDAO.addServiceEntry(serviceEntry, tenantId, user);
        return uuid;
    }

    @Override
    public void addServices(List<ServiceEntry> serviceList, int tenantId, String username) throws APIManagementException {
        catalogDAO.addServices(serviceList, tenantId, username, null);
    }

    @Override
    public void updateServices(List<ServiceEntry> serviceList, int tenantId, String username) throws APIManagementException {
        catalogDAO.updateServices(serviceList, tenantId, username);
    }

    @Override
    public String updateService(ServiceEntry serviceEntry, int tenantId, String user) throws APIManagementException {
        return catalogDAO.updateServiceCatalog(serviceEntry, tenantId, user);
    }

    @Override
    public ServiceEntry getServiceByUUID(String serviceId, int tenantId) throws APIManagementException {
        ServiceEntry service = catalogDAO.getServiceByUUID(serviceId, tenantId);
        if (service != null) {
            return service;
        } else {
            String msg = "Failed to retrieve the Service. Service with " + serviceId + " does not exist";
            throw new APIMgtResourceNotFoundException(msg);
        }
    }

    @Override
    public void deleteService(String serviceKey, int tenantId) throws APIManagementException {
        catalogDAO.deleteService(serviceKey, tenantId);
    }

    @Override
    public List<ServiceEntry> getService(int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public String addEndPointDefinition(ServiceEntry serviceEntry, String uuid) throws APIManagementException {
        return catalogDAO.addEndPointDefinition(serviceEntry, uuid);
    }

    @Override
    public ServiceEntry getMD5Hash(ServiceEntry serviceEntry, int tenantId) throws APIManagementException {
        return catalogDAO.getMd5Hash(serviceEntry, tenantId);
    }

    @Override
    public String getMD5HashByKey(String key, int tenantId) throws APIManagementException {
        return catalogDAO.getMd5HashByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getEndPointResourcesByKey(String key, int tenantId) throws APIManagementException {
        return catalogDAO.getServiceResourcesByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getServiceByKey(String key, int tenantId) throws APIManagementException {
        return catalogDAO.getServiceByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getEndPointResourcesByNameAndVersion(String name, String version, int tenantId)
            throws APIManagementException {
        return catalogDAO.getServiceByNameAndVersion(name, version, tenantId);
    }

    @Override
    public List<ServiceEntry> getServices(ServiceFilterParams filterParams, int tenantId, boolean shrink)
            throws APIManagementException {
        return catalogDAO.getServices(filterParams, tenantId, shrink);
    }
}
