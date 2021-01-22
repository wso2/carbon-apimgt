package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ServiceCatalogApi;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.dao.ServiceCatalogDAO;

import java.util.List;
import java.util.UUID;

public class ServiceCatalogImpl implements ServiceCatalogApi {

    private static final Log log = LogFactory.getLog(APIProviderImpl.class);
    private static final ServiceCatalogImpl serviceCatalogImpl = new ServiceCatalogImpl();

    @Override
    public String addService(ServiceCatalogInfo serviceCatalogInfo, int tenantId) throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        if (serviceCatalogImpl.getMD5HashByKey(serviceCatalogInfo.getKey(), tenantId) != null){
            return null;
        }
        ServiceCatalogDAO.getInstance().addServiceCatalog(serviceCatalogInfo, tenantId, uuid);
        return uuid;
    }

    @Override
    public ServiceCatalogInfo getServiceByUUID(String serviceCatalogId, int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public void deleteService(String serviceCatalogUuid) throws APIManagementException {

    }

    @Override
    public List<ServiceCatalogInfo> getService(int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public String addEndPointDefinition(ServiceCatalogInfo serviceCatalogInfo, String uuid) throws APIManagementException {

        return ServiceCatalogDAO.getInstance().addEndPointDefinition(serviceCatalogInfo, uuid);
    }

    @Override
    public ServiceCatalogInfo getMD5Hash(ServiceCatalogInfo serviceCatalogInfo, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5Hash(serviceCatalogInfo, tenantId);
    }

    @Override
    public String getMD5HashByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5HashByKey(key, tenantId);
    }

    @Override
    public ServiceCatalogInfo getEndPointResourcesByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByKey(key, tenantId);
    }

    @Override
    public ServiceCatalogInfo getServiceByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getServiceByKey(key, tenantId);
    }

    @Override
    public ServiceCatalogInfo getEndPointResourcesByNameAndVersion(String name, String version, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByNameAndVersion(name, version, tenantId);
    }
}
