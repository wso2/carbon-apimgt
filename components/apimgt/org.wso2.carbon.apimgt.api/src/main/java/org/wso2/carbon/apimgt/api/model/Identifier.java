/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.api.model;

/**
 * Interface for All Identifiers
 */
public interface Identifier {
    /**
     * Name of provider 
     * @return name
     */
    String getProviderName();
    
    /**
     * Tier related to the identifier
     * @return Tier
     */
    String getTier();
    
    /**
     * Set tier related to the identifier
     * @param tier
     * @return
     */
    void setTier(String tier);
    
    /**
     * Name of the identifier
     * @return name
     */
    String getName();
    
    /**
     * Version of the identifier
     * @return version
     */
    String getVersion();
    
    /**
     * UUID of the identifier
     * @return uuid
     */
    String getUUID();
    
    /**
     * Internal API Id
     * @return id
     */
    int getId();

    /**
     * UUID of the identifier
     * @return uuid
     */
    void setUuid(String uuid);

    /**
     * Internal API Id
     * @return id
     */
    void setId(int id);

    void setOrganization(String organization);

    String getOrganization();
}
