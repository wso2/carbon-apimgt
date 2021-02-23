package org.wso2.carbon.graphql.api.devportal.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.impl.registry.ApiRegistry;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.ApiListingDTO;

import java.util.List;

public class ApiListing {

    public ApiListingDTO getApiListing(int start, int offset) throws APIPersistenceException, APIManagementException {
        ApiRegistry apiDetails = new ApiRegistry();
        RegistryPersistenceImpl artifactData = new RegistryPersistenceImpl();
        List<ApiDTO> apis = apiDetails.getAllApis(start, offset);
        return  new ApiListingDTO(artifactData.apiCount(start, offset),apis, apiDetails.getPaginationData(start,offset));
    }
}
