/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.core.dao.impl;

/**
 * This interface produces search queries for API store.
 */
interface StoreApiAttributeSearch {

    /**
     * This method will return the query query to be executed, for each search type
     * (i.e - tag, subcontext or general search)
     *
     * @param roleCount Number of roles to be passed to query
     * @param searchQuery a sub string containing only the attributes
     * @return the query to be executed, as a string
     */
    String getStoreAttributeSearchQuery(int roleCount, StringBuilder searchQuery);
}
