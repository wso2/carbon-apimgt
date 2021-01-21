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

    public static HashMap<String, List<String>> filterNewServices(List<VerifierDTO> verifier, int tenantId) throws APIManagementException {
        HashMap<String, List<String>> filteredNewServices = new HashMap<>();
        List<String> newServices = new ArrayList<>();
        List<String> ignoredNewServices = new ArrayList<>();

        for (VerifierDTO verifierDTO : verifier) {
            String key = verifierDTO.getKey();
            if (StringUtils.isBlank(verifierDTO.getMd5()) && serviceCatalog.getMD5HashByKey(key, tenantId) == null) {
                newServices.add(key);
            } else {
//                ignoredNewServices.add(key);
            }
        }
        filteredNewServices.put(APIConstants.MAP_KEY_ACCEPTED, newServices);
        filteredNewServices.put(APIConstants.MAP_KEY_IGNORED, ignoredNewServices);
        return filteredNewServices;
    }

    public static HashMap<String, List<String>> verifierListValidate(List<VerifierDTO> verifier,
                                                               HashMap<String, String> newResourcesHash, int tenantId)
            throws APIManagementException {
        HashMap<String, List<String>> filteredServices = new HashMap<>();
        List<String> verifiedServices = new ArrayList<>();
        List<String> ignoredServices = new ArrayList<>();
        List<String> statusNotChanged = new ArrayList<>();

        for (VerifierDTO verifierDTO : verifier) {
            String key = verifierDTO.getKey();
            if (!StringUtils.isBlank(verifierDTO.getMd5()) && serviceCatalog.getMD5HashByKey(key, tenantId) != null &&
                    verifierDTO.getMd5().equals(serviceCatalog.getMD5HashByKey(key, tenantId))) {
                if(!StringUtils.equals(verifierDTO.getMd5(), newResourcesHash.get(key))) {
                    verifiedServices.add(key);
                } else {
                    statusNotChanged.add(key);
                }
            } else if (StringUtils.isBlank(verifierDTO.getMd5()) && serviceCatalog.getMD5HashByKey(key, tenantId) != null){
                ignoredServices.add(key);
            }
        }
        filteredServices.put(APIConstants.MAP_KEY_VERIFIED, verifiedServices);
        filteredServices.put(APIConstants.MAP_KEY_IGNORED, ignoredServices);
        filteredServices.put(APIConstants.MAP_KEY_NOT_CHANGED, statusNotChanged);
        return filteredServices;
    }
}
