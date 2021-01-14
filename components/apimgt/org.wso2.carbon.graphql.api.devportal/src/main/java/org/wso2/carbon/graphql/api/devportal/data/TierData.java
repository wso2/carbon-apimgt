package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.APIConstants;
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

        String  apiname = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_NAME);
        String  provider = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        String  version = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);

        //APIIdentifier apiIdentifier = new APIIdentifier(provider, apiname, version);
        //int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);

        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

        Map<String, Tier> definedTiers = getTiers(tenantId);
        Set<Tier> tierData = getAvailableTiers(definedTiers, tiers, apiname);


        List<org.wso2.carbon.apimgt.api.model.Tier> nameList = new ArrayList<org.wso2.carbon.apimgt.api.model.Tier>(tierData);


        List<TierDTO> tierList = new ArrayList<TierDTO>();
        String tierPlan=null;
        String description = null;
        String monetizationAttributes = null;

        for (int i=0;i< nameList.size();i++){
            //String name = getTierName(i);

            if(name.equals(nameList.get(i).getName())){
                tierPlan = nameList.get(i).getTierPlan();

                description = nameList.get(i).getDescription();

                Map<String,String> monetizationAttributesList = nameList.get(i).getMonetizationAttributes();

                if(monetizationAttributesList!=null){
                    monetizationAttributes = monetizationAttributesList.toString();
                }
            }




        }
        tierList.add(new TierDTO(tierPlan,monetizationAttributes));
        return tierList;

    }

    public List<TierNameDTO> getTierName(String Id) throws RegistryException, APIPersistenceException, UserStoreException {
        ArtifactData artifactData = new ArtifactData();

        List<TierNameDTO> tierNameDTOS = new ArrayList<>();


        String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);
        String[] tierNames = tiers.split("\\|\\|");

        for (String tierName : tierNames) {
            tierNameDTOS.add(new TierNameDTO(Id,tierName));
        }

        return tierNameDTOS;


    }
}
