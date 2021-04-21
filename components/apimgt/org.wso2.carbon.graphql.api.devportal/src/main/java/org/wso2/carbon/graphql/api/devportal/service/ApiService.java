package org.wso2.carbon.graphql.api.devportal.service;


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Time;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.ApiMapping;
import org.wso2.carbon.graphql.api.devportal.mapping.TierMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiListingDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.Pagination;
import org.wso2.carbon.graphql.api.devportal.security.AuthenticationContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;


public class ApiService {


    APIPersistence apiPersistenceInstance;

    public ApiListingDTO getAllApis(int start, int offset) throws APIPersistenceException, APIManagementException {
        PersistenceService persistenceService = new PersistenceService();
        List<ApiDTO> apiDTOList = new ArrayList<ApiDTO>();
        Map<String, Object> result = persistenceService.getDevportalAPIS(start,offset);
        int count = 0;
        Pagination pagination = null;
        if(result.size()==2){
            List<DevPortalAPI> list = (List<DevPortalAPI>) result.get("apis");
            count = (int) result.get("count");
            if(list.size()>0){
                for (DevPortalAPI devPortalAPI: list){
                    ApiMapping apiMapping = new ApiMapping();
                    apiDTOList.add(apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI));
                }
            }
            pagination = getPaginationData(offset,start,count);
        }
        return  new ApiListingDTO(count,apiDTOList, pagination);
    }
    public String getApiDefinition(String uuid) throws OASPersistenceException {
        Properties properties = new Properties();
        properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(properties);
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String apiDefinition = apiPersistenceInstance.getOASDefinition(org, uuid); //
        return apiDefinition;
    }
    public ApiDTO getApi(String uuid) throws APIManagementException, APIPersistenceException {

        PersistenceService persistenceService = new PersistenceService();
        DevPortalAPI devPortalAPI = persistenceService.getApiFromUUID(uuid);
        ApiDTO apiDTO = null;
        if (devPortalAPI!=null){
            ApiMapping apiMapping = new ApiMapping();
            apiDTO =  apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI);
        }
        return apiDTO;
    }
    public Pagination getPaginationData(int offset, int limit, int size){
        String paginatedPrevious = "";
        String paginatedNext = "";
        String query = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }
        return new Pagination(offset,limit,size,paginatedNext,paginatedPrevious);
    }

    public ContextDTO getApiTimeDetails(String uuid) throws APIManagementException{
        String loggedInUserName= AuthenticationContext.getLoggedInUserName();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(loggedInUserName);

        Time time = apiConsumer.getTimeDetailsFromDAO(uuid);
        ContextDTO contextDTO = new ContextDTO(time.getUuid(),time.getCreatedTime(),time.getLastUpdate(), time.getType());
        return contextDTO;


    }

    public Float getApiRatingFromDAO(String uuid) throws APIManagementException {
        String loggedInUserName= AuthenticationContext.getLoggedInUserName();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(loggedInUserName);
        Float rating  = apiConsumer.getRatingFromDAO(uuid);
        return rating;
    }
    public String getMonetizationLabel(String tiers) throws APIManagementException, UserStoreException {

        String[] strParts = tiers.split(",");
        List<String> listTiers = Arrays.asList(strParts);
        Set<String> tierSet = new HashSet<>(listTiers);

        String loggedInUserTenantDomain = AuthenticationContext.getLoggedInTenanDomain();

        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(loggedInUserTenantDomain);

        Map<String, Tier> definedTiers = APIUtil.getTiers(tenantId);
        String monetizationLabel = null;
        int free = 0, commercial = 0;
        for(String name: tierSet){
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
