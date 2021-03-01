package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.TierMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;

public class TierService {

    public List<TierDTO> getTierDetailsFromDAO(String uuid, String name) throws APIManagementException, RegistryException, UserStoreException, APIPersistenceException {


        //String username = "wso2.anonymous.user";
        //APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        //Map<String, Tier> definedTiers = apiConsumer.getTierDetailsFromDAO(Id);// getTiers(tenantId);
        TierMapping tierMapping = new TierMapping();
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
        List<TierDTO> tierList = tierMapping.fromTierToTierDTO(definedTiers,name);
        return tierList;
    }

    public List<TierNameDTO> getTierName(DevPortalAPI devPortalAPI) throws  APIPersistenceException{
        TierMapping tierMapping = new TierMapping();
        List<TierNameDTO> tierNameDTOS = tierMapping.fromTierNametoTierNameDTO(devPortalAPI);
        return tierNameDTOS;
    }

}
