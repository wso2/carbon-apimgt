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

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.APIConsumer;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Application;

import java.util.Map;

/**
 * Implementation of API Store operations.
 *
 */
public class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    @Override
    public Map<String, Object> getAllPaginatedAPIsByStatus(int start, int end, String status, boolean returnAPITags)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAllPaginatedAPIsByStatus(int start, int end, String[] status, boolean returnAPITags)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public Application getApplicationByUUID(String uuid) throws APIManagementException {
        return null;
    }

    @Override public void removeApplication(Application application) throws APIManagementException {

    }

    @Override public int addApplication(Application application, String userId) throws APIManagementException {
        return 0;
    }

    @Override public Application getApplicationById(int id) throws APIManagementException {
        return null;
    }

}
