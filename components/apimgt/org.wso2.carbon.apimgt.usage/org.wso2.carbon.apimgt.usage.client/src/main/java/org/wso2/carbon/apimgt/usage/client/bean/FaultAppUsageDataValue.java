package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 9/29/15.
 */
public class FaultAppUsageDataValue {
    long count;
    List<String> consumerKey_api_facet;
    public FaultAppUsageDataValue(long count, List<String> columnNames) {
        super();
        this.count = count;
        this.consumerKey_api_facet = columnNames;
    }
    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }
    public List<String> getColumnNames() {
        return consumerKey_api_facet;
    }
    public void setColumnNames(List<String> columnNames) {
        this.consumerKey_api_facet = columnNames;
    }
}
