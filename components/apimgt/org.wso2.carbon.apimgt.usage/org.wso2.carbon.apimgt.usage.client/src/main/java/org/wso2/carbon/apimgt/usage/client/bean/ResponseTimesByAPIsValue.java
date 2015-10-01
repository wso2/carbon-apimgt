package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 10/1/15.
 */
public class ResponseTimesByAPIsValue {
    int totalServiceTime;
    int totalResponseCount;

    List<String> api_version_context_facet;

    public int getTotalServiceTime() {
        return totalServiceTime;
    }

    public void setCount_sum(int totalServiceTime) {
        this.totalServiceTime = totalServiceTime;
    }

    public int getTotalResponseCount() {
        return totalResponseCount;
    }

    public void setTotalResponseCount(int totalResponseCount) {
        this.totalResponseCount = totalResponseCount;
    }

    public List<String> getColumnNames() {
        return api_version_context_facet;
    }

    public void setColumnNames(List<String> ColumnNames) {
        this.api_version_context_facet = ColumnNames;
    }

    public ResponseTimesByAPIsValue(int totalServiceTime, int totalResponseCount,
            List<String> ColumnNames) {
        super();
        this.totalServiceTime = totalServiceTime;
        this.totalResponseCount = totalResponseCount;
        this.api_version_context_facet = ColumnNames;
    }
}
