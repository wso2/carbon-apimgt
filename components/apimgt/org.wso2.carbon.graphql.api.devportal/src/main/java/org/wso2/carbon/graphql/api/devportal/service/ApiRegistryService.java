package org.wso2.carbon.graphql.api.devportal.service;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
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
import org.wso2.carbon.graphql.api.devportal.modules.api.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.APIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.graphql.api.devportal.modules.api.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.DefaultAPIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.DeploymentClusterInfoDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.IngressUrlDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.LabelNameDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.Pagination;
import org.wso2.carbon.graphql.api.devportal.modules.api.TierNameDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;


public class ApiRegistryService {


    APIPersistence apiPersistenceInstance;

    public List<ApiDTO> getAllApis(int start, int offset) throws APIPersistenceException, APIManagementException {
        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        List<ApiDTO> apiDTOList = new ArrayList<ApiDTO>();
        List<DevPortalAPI> list = artifactData.getDevportalAPIS(start,offset);
        for (DevPortalAPI devPortalAPI: list){
            ApiMapping apiMapping = new ApiMapping();
            apiDTOList.add(apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI));
        }
     return apiDTOList;
    }
    public String getApiDefinition(String Id) throws OASPersistenceException {
        Properties properties = new Properties();
        properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(properties);
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String apiDefinition = apiPersistenceInstance.getOASDefinition(org, Id); //
        return apiDefinition;
    }
    public ApiDTO getApi(String Id) throws APIManagementException, APIPersistenceException {

        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        ApiMapping apiMapping = new ApiMapping();
        return  apiMapping.fromDevpotralApiTOApiDTO(devPortalAPI);
    }
    public Pagination getPaginationData(int offset, int limit) throws APIPersistenceException, APIManagementException {
        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        int size = artifactData.apiCount(offset, limit);
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
}
