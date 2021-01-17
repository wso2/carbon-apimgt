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

    @Override
    public String addServiceCatalog(ServiceCatalogEntry serviceCatalog, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().addServiceCatalog(serviceCatalog.getServiceCatalogInfo(), tenantId);
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
    public String addEndPointDefinition(EndPointInfo endPointInfo) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().addEndPointDefinition(endPointInfo);
    }

    @Override
    public ServiceCatalogInfo getMD5Hash(ServiceCatalogInfo serviceCatalogInfo, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5Hash(serviceCatalogInfo, tenantId);
    }

    @Override
    public String getMD5HashByKey(String key, int tenantId) throws APIManagementException {
        return ServiceCatalogDAO.getInstance().getMd5HashByKey(key, tenantId);
    }
}
