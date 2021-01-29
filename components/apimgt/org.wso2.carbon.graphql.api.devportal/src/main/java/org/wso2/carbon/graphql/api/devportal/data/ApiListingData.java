package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import org.wso2.carbon.graphql.api.devportal.modules.ApiListing;
import org.wso2.carbon.graphql.api.devportal.modules.Pagination;

import java.util.List;

public class ApiListingData {

    public ApiListing getApiListing(int start, int offset) throws APIPersistenceException {
        PaginationData paginationData = new PaginationData();
        ApiDetails apiDetails = new ApiDetails();
        ArtifactData artifactData = new ArtifactData();
        List<Api> apis = apiDetails.getAllApis(start, offset);
        return  new ApiListing(artifactData.apiCount(start, offset),apis, paginationData.getPaginationData(start,offset));
    }
}
