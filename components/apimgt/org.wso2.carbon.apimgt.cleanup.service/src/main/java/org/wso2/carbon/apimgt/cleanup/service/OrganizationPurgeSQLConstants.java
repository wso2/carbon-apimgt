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

package org.wso2.carbon.apimgt.cleanup.service;

public class OrganizationPurgeSQLConstants {
    public static final String GET_API_LIST_SQL_BY_ORG = "SELECT API.API_ID, API.API_UUID,API.API_NAME," +
            "API.API_VERSION, API.API_PROVIDER FROM AM_API API WHERE API.ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DATA_FROM_AM_API_SQL = "DELETE FROM AM_API WHERE ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DEFAULT_VERSION_SQL = "DELETE FROM AM_API_DEFAULT_VERSION WHERE "
            + "ORGANIZATION = ?";

    public static final String DELETE_BULK_API_WORKFLOWS_REQUEST_SQL = "DELETE FROM AM_WORKFLOWS WHERE " +
            "WF_TYPE=\"AM_API_STATE\" AND WF_REFERENCE IN (SELECT CONVERT(API.API_ID, CHAR) FROM AM_API API " +
            "WHERE API.ORGANIZATION = ?";

    public static final String DELETE_BULK_KEY_MANAGER_LIST = "DELETE FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? "
            + "AND UUID IN (_KM_UUIDS_)";

    public static final String KM_UUID_REGEX = "_KM_UUIDS_";
}
