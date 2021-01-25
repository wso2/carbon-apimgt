package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.VerifierDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceCatalogUtils {
    private static final Log log = LogFactory.getLog(ServiceCatalogUtils.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

    public static HashMap<String, List<String>> validateVerifierList(List<VerifierDTO> verifier,
                                                                     HashMap<String, String> newResourcesHash, int tenantId)
            throws APIManagementException {
        HashMap<String, List<String>> filteredServices = new HashMap<>();
        List<String> verifiedServices = new ArrayList<>();//1
        List<String> ignoredServices = new ArrayList<>();//2
        List<String> statusNotChanged = new ArrayList<>();//3
        List<String> newServices = new ArrayList<>();//1

        for (VerifierDTO verifierDTO : verifier) {
            String key = verifierDTO.getKey();
            if (StringUtils.isBlank(verifierDTO.getMd5()) & serviceCatalog.getMD5HashByKey(key, tenantId) == null) {
                newServices.add(key); // adding db can process here (listen recording) & keep one list for both verified one as well
            } else if (!StringUtils.isBlank(verifierDTO.getMd5()) && serviceCatalog.getMD5HashByKey(key, tenantId) != null &&
                    verifierDTO.getMd5().equals(serviceCatalog.getMD5HashByKey(key, tenantId))) {
                if (!StringUtils.equals(verifierDTO.getMd5(), newResourcesHash.get(key))) {
                    verifiedServices.add(key);
                } else {
                    statusNotChanged.add(key);
                }
            } else {
                ignoredServices.add(key);
            }
//            if (!StringUtils.isBlank(verifierDTO.getMd5()) && serviceCatalog.getMD5HashByKey(key, tenantId) != null &&
//                    !verifierDTO.getMd5().equals(serviceCatalog.getMD5HashByKey(key, tenantId)))
        }
        filteredServices.put(APIConstants.MAP_KEY_VERIFIED_EXISTING_SERVICE, verifiedServices);
        filteredServices.put(APIConstants.MAP_KEY_IGNORED_EXISTING_SERVICE, ignoredServices);
        filteredServices.put(APIConstants.MAP_KEY_HASH_NOT_CHANGED_EXISTING_SERVICE, statusNotChanged);
        filteredServices.put(APIConstants.MAP_KEY_ACCEPTED_NEW_SERVICE, newServices);
        return filteredServices;
    }
}
