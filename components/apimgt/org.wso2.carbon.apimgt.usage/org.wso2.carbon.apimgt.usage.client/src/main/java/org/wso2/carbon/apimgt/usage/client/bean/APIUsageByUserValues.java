package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 10/1/15.
 */
public class APIUsageByUserValues {
    int count;
    List<String> api_version_userId_apiPublisher_facet;

    public int getCount_sum() {
        return count;
    }

    public void setCount_sum(int count_sum) {
        this.count = count_sum;
    }

    public List<String> getColumnNames() {
        return api_version_userId_apiPublisher_facet;
    }

    public void setColumnNames(List<String> ColumnNames) {
        this.api_version_userId_apiPublisher_facet = ColumnNames;
    }

    public APIUsageByUserValues(int count_sum, List<String> ColumnNames) {
        super();
        this.count = count_sum;
        this.api_version_userId_apiPublisher_facet = ColumnNames;
    }
}
