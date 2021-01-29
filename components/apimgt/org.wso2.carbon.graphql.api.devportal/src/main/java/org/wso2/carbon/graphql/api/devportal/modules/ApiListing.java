package org.wso2.carbon.graphql.api.devportal.modules;

import java.util.List;

public class ApiListing {
    private int count;
    private List<Api> getAllapis;
    private Pagination pagination;

    public ApiListing(int count, List<Api> getAllapis,Pagination pagination){
        this.count = count;
        this.getAllapis = getAllapis;
        this.pagination = pagination;
    }
}
