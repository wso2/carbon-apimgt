package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.VerifierDTO;

import java.util.ArrayList;
import java.util.List;

public class ServiceCatalogUtils {
    private static final Log log = LogFactory.getLog(ServiceCatalogUtils.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

    public static List<String> VerifierListValidate(List<VerifierDTO> verifier, int tenantId) throws APIManagementException {
        List<String> verifiedServices = new ArrayList<>();

        for(VerifierDTO verifierDTO : verifier){
            String key = verifierDTO.getKey();
            if(!StringUtils.isBlank(verifierDTO.getMd5()) && verifierDTO.getMd5().equals(serviceCatalog.getMD5HashByKey(key, tenantId))){
                verifiedServices.add(key);
            }
        }
        return verifiedServices;
    }


//    List<String> verifiedServices = new ArrayList<>();
//
//        for(String key : md5Hashes.keySet()){
//        if(md5Hashes.get(key).equals(serviceCatalog.getMD5HashByKey(key, tenantId))){
//            verifiedServices.add(key);
//        };
//    }
//        return verifiedServices;
//}
}
