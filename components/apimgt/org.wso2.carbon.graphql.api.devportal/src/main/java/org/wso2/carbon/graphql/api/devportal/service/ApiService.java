package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Time;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.TimeDTO;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;
import java.util.Set;

public class ApiService {

    public TimeDTO getApiTimeDetailsFromDAO(String Id) throws APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        Time time = apiConsumer.getTimeDetailsFromDAO(Id);
        String createTime = time.getCreatedTime();
        String lastUpdate = time.getLastUpdate();
        return new TimeDTO(createTime,lastUpdate);
    }
    public Float getApiRatingFromDAO(String Id) throws APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        Float rating  = apiConsumer.getRatingFromDAO(Id);
        return rating;
    }
    public String getMonetizationLabel(String Id) throws APIManagementException, UserStoreException, APIPersistenceException {


        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);

        Set<String> tiers = devPortalAPI.getAvailableTierNames();


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        Map<String, Tier> definedTiers = apiConsumer.getTierDetailsFromDAO(Id);

        String monetizationLabel = null;
        int free = 0, commercial = 0;
        for(String name: tiers){
            Tier definedTier = definedTiers.get(name);
            if (definedTier.getTierPlan().equalsIgnoreCase(APIConstants.API_CATEGORY_FREE)) {
                free = free + 1;
            } else if (definedTier.getTierPlan().equalsIgnoreCase(APIConstants.COMMERCIAL_TIER_PLAN)) {
                commercial = commercial + 1;
            }
        }
        if (free > 0 && commercial == 0) {
            monetizationLabel= APIConstants.API_CATEGORY_FREE;
        } else if (free == 0 && commercial > 0) {
            monetizationLabel = APIConstants.API_CATEGORY_PAID;
        } else if (free > 0 && commercial > 0) {
            monetizationLabel = APIConstants.API_CATEGORY_FREEMIUM;
        }
        return monetizationLabel;
    }

}
