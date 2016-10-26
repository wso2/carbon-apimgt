/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.api;

import java.util.Map;

import org.wso2.carbon.apimgt.core.dao.APIManagementException;

/**
 * This interface used to write Store specific methods
 *
 */
public interface APIConsumer extends APIManager {

    
    /**
     * Returns a paginated list of all APIs in given Status. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     * 
     * @param start starting number
     * @param end ending number
     * @param returnAPITags If true, tags of each API is returned
     * @return set of API
     * @throws APIManagementException if failed to API set
     */

    Map<String,Object> getAllPaginatedAPIsByStatus(int start,int end, String Status,
                                                          boolean returnAPITags) throws APIManagementException;
    
    /**
     * Returns a paginated list of all APIs in given Status list. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     * 
     * @param start starting number
     * @param end ending number
     * @param Status One or more Statuses
     * @param returnAPITags If true, tags of each API is returned
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Map<String,Object> getAllPaginatedAPIsByStatus(int start,int end, String[] Status,
                                                   boolean returnAPITags) throws APIManagementException;
}
