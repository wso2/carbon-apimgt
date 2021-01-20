package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.graphql.api.devportal.modules.TierDTO;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.graphql.api.devportal.modules.TierNameDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getAvailableTiers;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.replaceEmailDomainBack;

public class TierData {

    public List<TierDTO> getTierData(String Id, String name) throws APIManagementException, RegistryException, UserStoreException, APIPersistenceException {


        ArtifactData artifactData = new ArtifactData();

        //List<String> tierParams = artifactData.getApiIdentifireParams(Id);

        //String  apiname = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_NAME);

        APIIdentifier apiIdentifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(Id);
        String  provider = apiIdentifier.getProviderName();
        //.String  version = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_VERSION);
        //String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);

        //APIIdentifier apiIdentifier = new APIIdentifier(provider, apiname, version);
        //int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);

        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

        Map<String, Tier> definedTiers = getTiers(tenantId);


        //Set<Tier> tierData = getAvailableTiers(definedTiers, tiers, apiname);


       // List<org.wso2.carbon.apimgt.api.model.Tier> nameList = new ArrayList<org.wso2.carbon.apimgt.api.model.Tier>(tierData);


        List<TierDTO> tierList = new ArrayList<TierDTO>();
        String tierPlan=null;
        String displayName = null;
        String description = null;
        String policyContent = null;
        //long requestsPerMin = 0;
        int requestsPerMin = 0;
        int requestCount = 0;
        int unitTime = 0;

        String timeUnit = null;
        //Map<String, Object> tierAttributesMap = null;
        String tierAttributes = null;

        String monetizationAttributes = null;
        boolean stopOnQuotaReached = false;

        TierPermission tierPermission = null;

        Tier definedTier = definedTiers.get(name);
       // for (int i=0;i< nameList.size();i++){
            //String name = getTierName(i);

            //if(name.equals(nameList.get(i).getName())){



                displayName = definedTier.getDisplayName();//nameList.get(i).getDisplayName();

                description =definedTier.getDescription(); //nameList.get(i).getDescription();

                policyContent = String.valueOf(definedTier.getPolicyContent());//String.valueOf(nameList.get(i).getPolicyContent());

                Map<String, Object> tierAttributesMap = definedTier.getTierAttributes();

                tierAttributes = "";
                long requestsPerMinL = definedTier.getRequestsPerMin();//nameList.get(i).getRequestsPerMin();
                requestsPerMin = (int) requestsPerMinL;

                long requestCountL = definedTier.getRequestCount();//nameList.get(i).getRequestCount();
                requestCount = (int) requestCountL;

                long unitTimeL = definedTier.getUnitTime();//nameList.get(i).getUnitTime();
                unitTime = (int) unitTimeL;

                timeUnit = definedTier.getTimeUnit();//nameList.get(i).getTimeUnit();

                tierPlan = definedTier.getTierPlan();//nameList.get(i).getTierPlan();

                stopOnQuotaReached = definedTier.isStopOnQuotaReached();//nameList.get(i).isStopOnQuotaReached();

                tierPermission = definedTier.getTierPermission();
                Map<String,String> monetizationAttributesList = definedTier.getMonetizationAttributes();//nameList.get(i).getMonetizationAttributes();

                if(monetizationAttributesList!=null){
                    monetizationAttributes = monetizationAttributesList.toString();
                }
          //  }




        //}
        tierList.add(new TierDTO(displayName,description,policyContent,tierAttributes,requestsPerMin,requestCount,unitTime,timeUnit,tierPlan,stopOnQuotaReached,monetizationAttributes));
        return tierList;

    }

    public List<TierNameDTO> getTierName(String Id) throws RegistryException, APIPersistenceException, UserStoreException {
        ArtifactData artifactData = new ArtifactData();

        List<TierNameDTO> tierNameDTOS = new ArrayList<>();


//       String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);
//        String[] tierNames = tiers.split("\\|\\|");
//
       DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
//
        Set<String> tierNames = devPortalAPI.getAvailableTierNames();
        for (String tierName : tierNames) {
            tierNameDTOS.add(new TierNameDTO(Id,tierName));
        }
        //tierNameDTOS.add(new TierNameDTO(Id,devPortalAPI.getAvailableTierNames().toString()));
        return tierNameDTOS;


    }
}
