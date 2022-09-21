/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides access to API data layer
 */
public interface ApiDAO {

    int addAPI(API api, int tenantId, String organization) throws APIManagementException;

    void recordAPILifeCycleEvent(String uuid, String oldStatus, String newStatus, String userId,
                                 int tenantId) throws APIManagementException;

    int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException;

    void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException;

    PublisherAPI getPublisherAPI(Organization organization, String apiUUID) throws APIManagementException;

    PublisherAPISearchResult searchAPIsForPublisher(Organization organization, String searchQuery, int start,
                                                    int offset, UserContext ctx, String sortBy, String sortOrder) throws APIManagementException;


}
