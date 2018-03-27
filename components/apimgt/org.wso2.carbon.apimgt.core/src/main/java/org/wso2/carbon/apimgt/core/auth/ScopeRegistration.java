/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.core.auth;

import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.Scope;
/**
 * Scope Registration and Management Interface
 */
public interface ScopeRegistration {

    /**
     * Used to Register Scope
     * @param scope Scope object
     * @return true if scope get created
     * @throws KeyManagementException if scope Registration endpoint failed
     */
    boolean registerScope(Scope scope) throws KeyManagementException;

    /**
     * Used to get Scope by name
     * @param name name of scope
     * @return Scope object
     * @throws KeyManagementException  if scope Registration endpoint failed
     */
    Scope getScopeByName(String name) throws KeyManagementException;

    /**
     * Used to update scope
     * @param scope Scope object
     * @return true if scope get updated
     * @throws KeyManagementException  if scope Registration endpoint failed
     */
    boolean updateScope(Scope scope) throws KeyManagementException;

    /**
     * Used to delete Scope
     * @param name name of scope
     * @return true if scope is deleted
     * @throws KeyManagementException  if scope Registration endpoint failed
     */
    boolean deleteScope(String name) throws KeyManagementException;

    /**
     * Used to check existence of scope
     * @param name name of scope
     * @return true if scope exist
     * @throws KeyManagementException if scope Registration endpoint failed
     */
    boolean isScopeExist(String name) throws KeyManagementException;
}
