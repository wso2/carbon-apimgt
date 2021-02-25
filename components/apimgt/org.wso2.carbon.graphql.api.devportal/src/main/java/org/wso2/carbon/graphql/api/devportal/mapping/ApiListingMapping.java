package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiListingDTO;
import org.wso2.carbon.graphql.api.devportal.service.ApiRegistryService;
import org.wso2.carbon.graphql.api.devportal.service.RegistryPersistenceService;

import java.util.List;

public class ApiListingMapping {

    public ApiListingDTO getApiListing(int start, int offset) throws APIPersistenceException, APIManagementException {
        ApiRegistryService apiDetails = new ApiRegistryService();
        RegistryPersistenceService artifactData = new RegistryPersistenceService();
        List<ApiDTO> apis = apiDetails.getAllApis(start, offset);
        return  new ApiListingDTO(artifactData.apiCount(start, offset),apis, apiDetails.getPaginationData(start,offset));
    }
}
