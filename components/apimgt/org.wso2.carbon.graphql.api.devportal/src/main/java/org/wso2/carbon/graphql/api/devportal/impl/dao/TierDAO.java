package org.wso2.carbon.graphql.api.devportal.impl.dao;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;

public class TierDAO {

    public List<TierDTO> getTierDetailsFromDAO(String Id, String name) throws APIManagementException, RegistryException, UserStoreException, APIPersistenceException {


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        Map<String, Tier> definedTiers = apiConsumer.getTierDetailsFromDAO(Id);// getTiers(tenantId);

        List<TierDTO> tierList = new ArrayList<TierDTO>();
        String tierPlan=null;
        String displayName = null;
        String description = null;
        String policyContent = null;
        int requestsPerMin = 0;
        int requestCount = 0;
        int unitTime = 0;
        String timeUnit = null;
        String tierAttributes = null;
        String monetizationAttributes = null;
        boolean stopOnQuotaReached = false;
        TierPermission tierPermission = null;
        Tier definedTier = definedTiers.get(name);
        if(definedTier!=null) {
            displayName = definedTier.getDisplayName();
            description = definedTier.getDescription();
            policyContent = String.valueOf(definedTier.getPolicyContent());
            Map<String, Object> tierAttributesMap = definedTier.getTierAttributes();
            tierAttributes = "";
            long requestsPerMinL = definedTier.getRequestsPerMin();
            requestsPerMin = (int) requestsPerMinL;
            long requestCountL = definedTier.getRequestCount();
            requestCount = (int) requestCountL;
            long unitTimeL = definedTier.getUnitTime();
            unitTime = (int) unitTimeL;
            timeUnit = definedTier.getTimeUnit();
            tierPlan = definedTier.getTierPlan();

            stopOnQuotaReached = definedTier.isStopOnQuotaReached();

            tierPermission = definedTier.getTierPermission();
            Map<String, String> monetizationAttributesList = definedTier.getMonetizationAttributes();//nameList.get(i).getMonetizationAttributes();

            if (monetizationAttributesList != null) {
                monetizationAttributes = monetizationAttributesList.toString();
            }

        }




        tierList.add(new TierDTO(displayName,description,policyContent,tierAttributes,requestsPerMin,requestCount,unitTime,timeUnit,tierPlan,stopOnQuotaReached,monetizationAttributes));
        return tierList;

    }

    public List<TierNameDTO> getTierName(DevPortalAPI devPortalAPI) throws  APIPersistenceException{

        List<TierNameDTO> tierNameDTOS = new ArrayList<>();

        Set<String> tierNames = devPortalAPI.getAvailableTierNames();
        for (String tierName : tierNames) {
            tierNameDTOS.add(new TierNameDTO(devPortalAPI.getId(),tierName));
        }
        return tierNameDTOS;


    }

}
