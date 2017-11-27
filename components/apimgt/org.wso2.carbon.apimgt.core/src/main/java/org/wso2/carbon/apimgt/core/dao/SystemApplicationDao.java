/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

/**
 * This interface used to do insert and get operations of SystemApplication (publisher,store,admin,etc..)
 */
public interface SystemApplicationDao {

    /**
     * Used to insert System Application consumer key into the database
     *
     * @param appName     application name (publisher,store,admin,etc..)
     * @param consumerKey consumer Key Registered For the app.
     * @throws APIMgtDAOException if error occurred when add application key
     */
    public void addApplicationKey(String appName, String consumerKey) throws APIMgtDAOException;

    /**
     * Get the consumer key of application
     *
     * @param appName application name (publisher,store,admin,etc..)
     * @return consumer Key of application
     * @throws APIMgtDAOException if error occurred when get application key
     */
    public String getConsumerKeyForApplication(String appName) throws APIMgtDAOException;

    /**
     * Remove consumer key for application
     *
     * @param appName application name (publisher,store,admin,etc..)
     * @throws APIMgtDAOException if error occurred when remove application key
     */
    public void removeConsumerKeyForApplication(String appName) throws APIMgtDAOException;

    /**
     * Check application exist in System Apps
     *
     * @param appName application name (publisher,store,admin,etc..)
     * @return true if consumerKey Exist for application
     * @throws APIMgtDAOException if error occurred when check application key exist
     */
    public boolean isConsumerKeyExistForApplication(String appName) throws APIMgtDAOException;
}
