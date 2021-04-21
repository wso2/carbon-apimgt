package org.wso2.carbon.graphql.api.devportal.modules.api;

import java.util.List;

public class ApiListingDTO {
    private int count;
    private List<ApiDTO> nodes;
    private Pagination pagination;

    public ApiListingDTO(int count, List<ApiDTO> nodes, Pagination pagination){
        this.count = count;
        this.nodes = nodes;
        this.pagination = pagination;
    }
}
