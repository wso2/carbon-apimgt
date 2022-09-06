/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to return API search results of solr queries. It just simply wraps list of APIs returned and
 * provides the total num of APIs that matches the query. Here, length of API list may not be equal to the apiCount
 * in case of paginated searches.
 */
public class APISearchResult {
    private List<API> apis;
    private int apiCount;

    public APISearchResult() {
        this.apiCount = 0;
        this.apis = new ArrayList<>();
    }

    public APISearchResult(List<API> apis, int apiCount) {
        this.apis = apis;
        this.apiCount = apiCount;
    }

    public List<API> getApis() {
        return apis;
    }

    public int getApiCount() {
        return apiCount;
    }

    public void setApis(List<API> apis) {
        this.apis = apis;
    }

    public void setApiCount(int apiCount) {
        this.apiCount = apiCount;
    }
}
