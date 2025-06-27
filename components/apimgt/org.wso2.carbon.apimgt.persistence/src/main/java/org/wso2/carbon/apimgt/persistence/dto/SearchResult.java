package org.wso2.carbon.apimgt.persistence.dto;

import java.util.List;

public class SearchResult {
    private int totalcount;
    private List<String> result;

    public SearchResult(int totalcount, List<String> result) {
        this.totalcount = totalcount;
        this.result = result;
    }

    public int getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(int totalcount) {
        this.totalcount = totalcount;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

}
