package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ServiceCatalogApi;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.dao.ServiceCatalogDAO;

import java.util.List;
import java.util.UUID;

public class ServiceCatalogImpl implements ServiceCatalogApi {

    private static final Log log = LogFactory.getLog(APIProviderImpl.class);
    private static final ServiceCatalogImpl serviceCatalogImpl = new ServiceCatalogImpl();

    @Override
    public String addService(ServiceEntry serviceEntry, int tenantId) throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        if (serviceCatalogImpl.getMD5HashByKey(serviceEntry.getKey(), tenantId) != null){ //Don't need
            return null;
        }
        ServiceCatalogDAO.getInstance().addServiceCatalog(serviceEntry, tenantId, uuid);
        return uuid;
    }

    @Override
    public String updateService(ServiceEntry serviceEntry, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().updateServiceCatalog(serviceEntry, tenantId);
    }

    @Override
    public ServiceEntry getServiceByUUID(String serviceCatalogId, int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public void deleteService(String serviceCatalogUuid) throws APIManagementException {

    }

    @Override
    public List<ServiceEntry> getService(int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public String addEndPointDefinition(ServiceEntry serviceEntry, String uuid) throws APIManagementException {

        return ServiceCatalogDAO.getInstance().addEndPointDefinition(serviceEntry, uuid);
    }

    @Override
    public ServiceEntry getMD5Hash(ServiceEntry serviceEntry, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5Hash(serviceEntry, tenantId);
    }

    @Override
    public String getMD5HashByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5HashByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getEndPointResourcesByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getServiceByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getServiceByKey(key, tenantId);
    }

    @Override
    public ServiceEntry getEndPointResourcesByNameAndVersion(String name, String version, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByNameAndVersion(name, version, tenantId);
    }
}
