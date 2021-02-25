package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TierMapping {

    public List<TierDTO> fromTierToTierDTO( Map<String, Tier> definedTiers,String name){
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

    public List<TierNameDTO> fromTierNametoTierNameDTO(DevPortalAPI devPortalAPI) throws APIPersistenceException {

        List<TierNameDTO> tierNameDTOS = new ArrayList<>();

        Set<String> tierNames = devPortalAPI.getAvailableTierNames();
        for (String tierName : tierNames) {
            tierNameDTOS.add(new TierNameDTO(devPortalAPI.getId(),tierName));
        }
        return tierNameDTOS;


    }
}
