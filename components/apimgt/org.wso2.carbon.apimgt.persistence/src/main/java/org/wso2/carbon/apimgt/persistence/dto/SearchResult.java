package org.wso2.carbon.apimgt.persistence.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class SearchResult {
    private static final Log log = LogFactory.getLog(SearchResult.class);
    private int totalCount;
    private List<String> result;

    public SearchResult(int totalCount, List<String> result) {
        log.debug("Creating SearchResult with totalCount: " + totalCount + " and result size: " + (result != null ? result.size() : 0));
        this.totalCount = totalCount;
        this.result = result;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        log.debug("Setting totalCount to: " + totalCount);
        this.totalCount = totalCount;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        log.debug("Setting result with size: " + (result != null ? result.size() : 0));
        this.result = result;
    }

}
