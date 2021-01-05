package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.graphql.api.devportal.modules.Pagination;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaginationData {

    List<Pagination> paginationList = Arrays.asList(
            new Pagination(1,25,2,"x1","x1")
    );

    public List<Pagination> apiPagination(){
        return paginationList;
    }

    public Pagination getPaginationData() throws UserStoreException, RegistryException {
        ApiDetails apiDetails = new ApiDetails();
        Integer offset = 0;
        Integer limit=25;
        Integer size = 2;
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";
        String query = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }
        return new Pagination(offset,limit,size,paginatedNext,paginatedPrevious);
    }
}
