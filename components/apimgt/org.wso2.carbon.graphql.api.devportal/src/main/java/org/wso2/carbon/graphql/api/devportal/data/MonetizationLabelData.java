package org.wso2.carbon.graphql.api.devportal.data;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
//import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

//import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getAvailableTiers;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.replaceEmailDomainBack;

public class MonetizationLabelData {


    public String getMonetizationLabelData(String Id) throws APIManagementException, UserStoreException, APIPersistenceException {


        ArtifactData artifactData = new ArtifactData();
        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);

        Set<String> tiers = devPortalAPI.getAvailableTierNames();

        APIIdentifier apiIdentifier = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(Id);
        String  provider = apiIdentifier.getProviderName();
        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomainName);

        Map<String, Tier> definedTiers = getTiers(tenantId);

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
