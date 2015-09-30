package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 9/28/15.
 */
public class PerAppPerAPIUsageValues {
    int count;
    List<String> key_api_facet;
    public int getCount_sum() {
        return count;
    }
    public void setCount_sum(int count_sum) {
        this.count = count_sum;
    }
    public List<String> getColumnNames() {
        return key_api_facet;
    }
    public void setColumnNames(List<String> ColumnNames) {
        this.key_api_facet = ColumnNames;
    }
    public PerAppPerAPIUsageValues(int count_sum, List<String> ColumnNames) {
        super();
        this.count = count_sum;
        this.key_api_facet = ColumnNames;
    }
}
