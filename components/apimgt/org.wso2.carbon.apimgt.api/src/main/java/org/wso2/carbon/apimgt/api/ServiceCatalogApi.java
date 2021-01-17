package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;

import java.util.List;

public interface ServiceCatalogApi {

    /**
     * Adds a new Service Catalog
     *
     * @param serviceCatalog ServiceCatalogInfo
     * @param tenantId       Tenant Identifier
     * @return ServiceCatalogId UUID of the created Service Catalog ID
     * @throws APIManagementException if failed to add ServiceCatalogInfo
     */
    String addServiceCatalog(ServiceCatalogEntry serviceCatalog, int tenantId) throws APIManagementException;

    /**
     * Returns details of an Service Catalog
     *
     * @param serviceCatalogId ServiceCatalog Identifier
     * @param tenantId         Tenant Identifier
     * @return An ServiceCatalogInfo object related to the given identifier or null
     * @throws APIManagementException if failed to get details of an Service Catalog
     */
    ServiceCatalogInfo getServiceCatalogByUUID(String serviceCatalogId, int tenantId)
            throws APIManagementException;

    /**
     * Deletes an Service Catalog
     *
     * @param serviceCatalogUuid ServiceCatalog Identifier(UUID)
     * @throws APIManagementException if failed to delete the Service Catalog
     */
    void deleteServiceCatalog(String serviceCatalogUuid) throws APIManagementException;

    /**
     * Returns details of all Service Catalogs belong to a given tenant
     *
     * @param tenantId Tenant Identifier
     * @return A list of ServiceCatalogInfo objects
     * @throws APIManagementException if failed to get details of Service Catalogs
     */
    List<ServiceCatalogInfo> getServiceCatalogs(int tenantId) throws APIManagementException;

    /**
     * Adds a new end-point definition
     *
     * @param endPointInfo EndPoint related information
     * @return ServiceCatalogId UUID of the created Service Catalog ID
     * @throws APIManagementException if failed to add ServiceCatalogInfo
     */
    String addEndPointDefinition(EndPointInfo endPointInfo) throws APIManagementException;

    /**
     * Get MD5 hash value of a service endpoint
     *
     * @param serviceCatalogInfo EndPoint related information
     * @return ServiceCatalogInfo Endpoint information with md5 hash value
     * @throws APIManagementException if failed to add ServiceCatalogInfo
     */
    ServiceCatalogInfo getMD5Hash(ServiceCatalogInfo serviceCatalogInfo, int tenantId) throws APIManagementException;

    /**
     * Get MD5 hash value of a service endpoint
     *
     * @param key Service key unique to each tenant
     * @return ServiceCatalogInfo Endpoint information with md5 hash value
     * @throws APIManagementException if failed to add ServiceCatalogInfo
     */
    String getMD5HashByKey(String key, int tenantId) throws APIManagementException;

}
