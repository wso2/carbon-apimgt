package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.graphql.api.devportal.modules.TierDTO;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getAvailableTiers;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.replaceEmailDomainBack;

public class TierData {

    public List<TierDTO> getTierData(String Id) throws APIManagementException, GovernanceException, UserStoreException {


        ArtifactData artifactData = new ArtifactData();

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

        for (int i=0;i< nameList.size();i++){
            String name = nameList.get(i).getName();

            String tierPlan = nameList.get(i).getTierPlan();

            Map<String,String> monetizationAttributesList = nameList.get(i).getMonetizationAttributes();
            String monetizationAttributes = null;
            if(monetizationAttributesList!=null){
                monetizationAttributes = monetizationAttributesList.toString();
            }

            tierList.add(new TierDTO(name, tierPlan,monetizationAttributes));
        }
        return tierList;

    }
}
