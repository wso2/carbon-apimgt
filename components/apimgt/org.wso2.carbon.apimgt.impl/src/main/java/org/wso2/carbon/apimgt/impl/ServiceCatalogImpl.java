package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ServiceCatalogApi;
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.dao.ServiceCatalogDAO;

import java.util.List;

public class ServiceCatalogImpl implements ServiceCatalogApi {

    private static final Log log = LogFactory.getLog(APIProviderImpl.class);
    private static final ServiceCatalogImpl serviceCatalogImpl = new ServiceCatalogImpl();

    @Override
    public String addServiceCatalog(ServiceCatalogEntry serviceCatalog, int tenantId) throws APIManagementException {
        if (serviceCatalogImpl.getMD5HashByKey(serviceCatalog.getServiceCatalogInfo().getKey(), tenantId) != null){
            return null; //Exceptions??
        }
        String uuid =  ServiceCatalogDAO.getInstance().addServiceCatalog(serviceCatalog.getServiceCatalogInfo(), tenantId);
        return serviceCatalogImpl.addEndPointDefinition(serviceCatalog.getEndPointInfo(), uuid);
    }

    @Override
    public ServiceCatalogInfo getServiceCatalogByUUID(String serviceCatalogId, int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public void deleteServiceCatalog(String serviceCatalogUuid) throws APIManagementException {

    }

    @Override
    public List<ServiceCatalogInfo> getServiceCatalogs(int tenantId) throws APIManagementException {
        return null;
    }

    @Override
    public String addEndPointDefinition(EndPointInfo endPointInfo, String uuid) throws APIManagementException {

        return ServiceCatalogDAO.getInstance().addEndPointDefinition(endPointInfo, uuid);
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
    public EndPointInfo getEndPointResourcesByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByKey(key, tenantId);
    }

    @Override
    public ServiceCatalogInfo getServiceByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getServiceByKey(key, tenantId);
    }

    @Override
    public EndPointInfo getEndPointResourcesByNameAndVersion(String name, String version, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getCatalogResourcesByNameAndVersion(name, version, tenantId);
    }
}
