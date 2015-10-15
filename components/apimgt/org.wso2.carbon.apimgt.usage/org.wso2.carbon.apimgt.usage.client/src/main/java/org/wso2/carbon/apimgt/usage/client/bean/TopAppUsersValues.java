/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class TopAppUsersValues {
    private int count;
    private List<String> key_userId_facet;

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
