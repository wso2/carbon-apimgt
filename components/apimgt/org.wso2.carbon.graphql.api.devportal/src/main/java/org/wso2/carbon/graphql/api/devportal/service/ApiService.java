package org.wso2.carbon.graphql.api.devportal.service;


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Time;
import org.wso2.carbon.apimgt.impl.APIConstants;
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
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiListingDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.Pagination;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.*;


public class ApiService {


    APIPersistence apiPersistenceInstance;

    public ApiListingDTO getAllApis(int start, int offset, String token, String oauth) throws APIPersistenceException, APIManagementException {
        PersistenceService artifactData = new PersistenceService();
        List<ApiDTO> apiDTOList = new ArrayList<ApiDTO>();
        // List<DevPortalAPI> list = artifactData.getDevportalAPIS(start,offset,token,oauth);
        Map<String, Object> result = artifactData.getDevportalAPIS(start,offset,token,oauth);
        List<DevPortalAPI> list = (List<DevPortalAPI>) result.get("apis");
        for (DevPortalAPI devPortalAPI: list){
            ApiMapping apiMapping = new ApiMapping();
            apiDTOList.add(apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI));
        }
        int count = (int) result.get("count");
        Pagination pagination = getPaginationData(offset,start,count);
        return  new ApiListingDTO(count,apiDTOList, pagination);

        // return apiDTOList;
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

        PersistenceService artifactData = new PersistenceService();
        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(uuid);
        ApiMapping apiMapping = new ApiMapping();
        return  apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI);
    }
    public Pagination getPaginationData(int offset, int limit, int size) throws APIPersistenceException, APIManagementException {
        PersistenceService artifactData = new PersistenceService();
        //int size = artifactData.apiCount(offset, limit);
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

    public ContextDTO getApiTimeDetails(String uuid){
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = null;
        try {
            apiConsumer = RestApiCommonUtil.getConsumer(username);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        Time time = apiConsumer.getTimeDetailsFromDAO(uuid);
        ContextDTO contextDTO = new ContextDTO(time.getUuid(),time.getCreatedTime(),time.getLastUpdate(), time.getType());
        return contextDTO;


    }

    //    public TimeDTO getApiTimeDetailsFromDAO(String uuid) throws APIManagementException {
//        String username = "wso2.anonymous.user";
//        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
//        Time time = apiConsumer.getTimeDetailsFromDAO(uuid);
//        String createTime = time.getCreatedTime();
//        String lastUpdate = time.getLastUpdate();
//        return new TimeDTO(createTime,lastUpdate);
//    }
    public Float getApiRatingFromDAO(String uuid) throws APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        Float rating  = apiConsumer.getRatingFromDAO(uuid);
        return rating;
    }
    public String getMonetizationLabel(String uuid) throws APIManagementException, UserStoreException, APIPersistenceException {

        PersistenceService persistenceService = new PersistenceService();
        DevPortalAPI devPortalAPI = persistenceService.getApiFromUUID(uuid);

        Set<String> tiers = devPortalAPI.getAvailableTierNames();

        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        Map<String, Tier> definedTiers = apiConsumer.getTierDetailsFromDAO(uuid);

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
