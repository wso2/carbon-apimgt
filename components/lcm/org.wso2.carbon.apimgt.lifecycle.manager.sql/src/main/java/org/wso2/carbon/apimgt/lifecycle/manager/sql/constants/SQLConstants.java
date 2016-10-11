/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.sql.constants;

public class SQLConstants {

    public static final String DB_CHECK_SQL = "select 1 from LC_DEFINITIONS";

    public static final String ADD_LIFECYCLE_SQL =
            " INSERT INTO LC_DEFINITIONS (LC_NAME,LC_CONTENT,LC_TENANT_ID)" + " VALUES (?,?,?)";

    public static final String GET_LIFECYCLE_LIST_SQL = " SELECT DEF.LC_NAME AS LIFECYCLE_LIST FROM LC_DEFINITIONS DEF "
            + "WHERE DEF.LC_TENANT_ID=? ";

    public static final String GET_LIFECYCLE_CONFIG_SQL = " SELECT DEF.LC_NAME AS LIFECYCLE_NAME, DEF.LC_CONTENT AS "
            + "LIFECYCLE_CONTENT FROM LC_DEFINITIONS DEF WHERE DEF.LC_NAME=? AND DEF.LC_TENANT_ID=? ";

    public static final String GET_ALL_LIFECYCLE_CONFIGS_SQL =
            " SELECT DEF.LC_NAME AS LIFECYCLE_NAME, DEF.LC_CONTENT AS LIFECYCLE_CONTENT, DEF.LC_TENANT_ID AS "
                    + "TENANT_ID FROM LC_DEFINITIONS DEF";

    public static final String GET_LIFECYCLE_DEFINITION_ID_FROM_NAME_SQL = " SELECT DEF.ID AS LIFECYCLE_DEFINITION_ID "
            + "FROM LC_DEFINITIONS DEF WHERE DEF.LC_NAME=? AND DEF.LC_TENANT_ID=?";

    public static final String ADD_LIFECYCLE_STATE_SQL =
            " INSERT INTO LC_DATA (LC_STATE_ID,LC_DEFINITION_ID,LC_STATUS,LC_TENANT_ID)" + " VALUES (?,?,?,?)";

    public static final String UPDATE_LIFECYCLE_STATE_SQL = "UPDATE LC_DATA SET LC_STATUS=? WHERE LC_STATE_ID=? " +
            "AND LC_TENANT_ID=?";

    public static final String GET_LIFECYCLE_NAME_FROM_ID_SQL =
            "SELECT DEF.LC_NAME AS LIFECYCLE_NAME, DATA.LC_STATUS AS LIFECYCLE_STATUS FROM "
                    + "LC_DEFINITIONS DEF, LC_DATA DATA WHERE DEF.ID = DATA.LC_DEFINITION_ID AND DATA.LC_STATE_ID=? "
                    + "AND  DATA" + ".LC_TENANT_ID=?";

    public static final String CHECK_LIFECYCLE_EXIST_SQL =
            "SELECT DEF.LC_NAME FROM LC_DEFINITIONS DEF WHERE DEF.LC_NAME=? AND DEF.LC_TENANT_ID=?";
}