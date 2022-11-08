/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.constants;

/**
 * Keep the constants related to environment specific api property DAO.
 */
public class EnvironmentSpecificAPIPropertyConstants {
    public static final String ADD_ENVIRONMENT_SPECIFIC_API_PROPERTIES_SQL =
            "INSERT INTO AM_API_ENVIRONMENT_KEYS(UUID, API_UUID, ENVIRONMENT_ID, PROPERTY_CONFIG) VALUES(?,?,?,?)";

    public static final String UPDATE_ENVIRONMENT_SPECIFIC_API_PROPERTIES_SQL =
            "UPDATE AM_API_ENVIRONMENT_KEYS SET PROPERTY_CONFIG = ? WHERE API_UUID=? AND ENVIRONMENT_ID=?";

    public static final String GET_ENVIRONMENT_SPECIFIC_API_PROPERTIES_SQL =
            "SELECT PROPERTY_CONFIG FROM AM_API_ENVIRONMENT_KEYS WHERE API_UUID=? AND ENVIRONMENT_ID=?";

    public static final String IS_ENVIRONMENT_SPECIFIC_API_PROPERTIES_EXIST_SQL =
            "SELECT 1 FROM AM_API_ENVIRONMENT_KEYS WHERE API_UUID=? AND ENVIRONMENT_ID=?";

    public static final String GET_ENVIRONMENT_SPECIFIC_API_PROPERTIES_BY_APIS_SQL =
            "SELECT AM_GATEWAY_ENVIRONMENT.UUID ENV_ID,"
                    + "       AM_GATEWAY_ENVIRONMENT.NAME ENV_NAME,"
                    + "       AM_API_ENVIRONMENT_KEYS.API_UUID API_UUID,"
                    + "       AM_API_ENVIRONMENT_KEYS.PROPERTY_CONFIG CONFIG"
                    + " FROM AM_API_ENVIRONMENT_KEYS,AM_GATEWAY_ENVIRONMENT"
                    + " WHERE AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID = AM_GATEWAY_ENVIRONMENT.UUID AND"
                    + "        AM_API_ENVIRONMENT_KEYS.API_UUID IN (_API_ID_LIST_)"
                    + " ORDER BY API_UUID, ENV_NAME, ENV_ID";

    public static final String GET_ENVIRONMENT_SPECIFIC_API_PROPERTIES_BY_APIS_ENVS_SQL =
            "SELECT AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID ENV_ID,"
                    + "       AM_API_ENVIRONMENT_KEYS.API_UUID API_UUID,"
                    + "       AM_API_ENVIRONMENT_KEYS.PROPERTY_CONFIG CONFIG"
                    + " FROM AM_API_ENVIRONMENT_KEYS"
                    + " WHERE AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID IN (_ENV_ID_LIST_) AND"
                    + "        AM_API_ENVIRONMENT_KEYS.API_UUID IN (_API_ID_LIST_)"
                    + " ORDER BY API_UUID, ENV_ID";;
}
