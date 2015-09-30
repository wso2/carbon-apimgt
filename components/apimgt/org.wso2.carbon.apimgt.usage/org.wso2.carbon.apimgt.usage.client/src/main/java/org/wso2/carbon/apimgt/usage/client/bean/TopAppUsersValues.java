package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

/**
 * Created by rukshan on 9/29/15.
 */
public class TopAppUsersValues {
    int count;
    List<String> key_userId_facet;
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public TopAppUsersValues(int count, List<String> columns) {
        super();
        this.count = count;
        this.key_userId_facet = columns;
    }
    public List<String> getColumnNames() {
        return key_userId_facet;
    }
    public void setColumnNames(List<String> getTopAppUsersFacet) {
        this.key_userId_facet = getTopAppUsersFacet;
    }
}
