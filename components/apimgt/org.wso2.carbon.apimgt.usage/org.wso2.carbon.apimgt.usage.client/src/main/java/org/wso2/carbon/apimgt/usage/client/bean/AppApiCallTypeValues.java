package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 9/29/15.
 */
public class AppApiCallTypeValues {
    String count;
    List<String> key_api_method_path_facet;
    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
    public List<String> getColumnNames() {
        return key_api_method_path_facet;
    }
    public void setcolumnNames(List<String> columnNames) {
        this.key_api_method_path_facet = columnNames;
    }
    public AppApiCallTypeValues(String count, List<String> columnNames) {
        super();
        this.count = count;
        this.key_api_method_path_facet = columnNames;
    }
}
