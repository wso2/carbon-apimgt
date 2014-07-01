package org.wso2.carbon.forum.dto;

import java.util.List;

/**
 * Created by nuwan on 5/5/14.
 */
public class ForumSearchDTO<T> {

    private List<T> paginatedResults;

    private long totalResultCount;

    public List<T> getPaginatedResults() {
        return paginatedResults;
    }

    public void setPaginatedResults(List<T> paginatedResults) {
        this.paginatedResults = paginatedResults;
    }

    public long getTotalResultCount() {
        return totalResultCount;
    }

    public void setTotalResultCount(long totalResultCount) {
        this.totalResultCount = totalResultCount;
    }
}
