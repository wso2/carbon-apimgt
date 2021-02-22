package org.wso2.carbon.graphql.api.devportal.modules.api;

import java.util.List;

public class ApiListingDTO {
    private int count;
    private List<ApiDTO> getAllapis;
    private Pagination pagination;

    public ApiListingDTO(int count, List<ApiDTO> getAllapis, Pagination pagination){
        this.count = count;
        this.getAllapis = getAllapis;
        this.pagination = pagination;
    }
}
