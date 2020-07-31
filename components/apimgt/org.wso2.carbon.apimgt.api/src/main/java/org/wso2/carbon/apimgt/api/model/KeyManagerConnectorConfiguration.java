/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.List;

/**
 * This Interface providing functionality to register KeyManagerConnector Related Configurations
 */
public interface KeyManagerConnectorConfiguration {

    /**
     *  This method returns the KeyManager implementation class name
     * @return keymanager implementation class name
     */
    public String getImplementation();

    /**
     *  This method returns JWTValidator class name if defined.
     * @return JWTValidator class name
     */
    public String getJWTValidator();

    /**
     * This method returns the Configurations related to keymanager registration
     * @return
     */
    public List<ConfigurationDto> getConnectionConfigurations();

    /**
     * This method returns the Configurations related to Oauth Application Creation
     * @return
     */
    public List<ConfigurationDto> getApplicationConfigurations();

    /**
     * This method used to get Type
     */
    public String getType();

    /**
     * This method used to get Disaply Name
     */
    public default String getDisplayName() {

        return getType();
    }
}
