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

public class OrganizationPurgeConstants {

    public static final String ORG_CLEANUP_EXECUTOR = "Organization-Cleanup-Executor";

    public static final String API_ORGANIZATION_COMBINATION_EXIST = "SELECT API.API_ID FROM AM_API API WHERE "
            + "API.ORGANIZATION = ?";

    public static final String APPLICATION_ORGANIZATION_COMBINATION_EXIST = "SELECT APP.APPLICATION_ID FROM AM_"
            + "APPLICATION APP WHERE APP.ORGANIZATION = ?";

    public static final String IDP_ORGANIZATION_COMBINATION_EXIST = "SELECT KM.UUID FROM AM_KEY_MANAGER KM WHERE "
            + "KM.ORGANIZATION = ?";

    public static final String GET_API_LIST_SQL_BY_ORG_SQL = "SELECT API.API_ID, API.API_UUID, API.API_NAME," +
            "API.API_VERSION, API.API_PROVIDER FROM AM_API API WHERE API.ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DATA_FROM_AM_API_SQL = "DELETE FROM AM_API WHERE ORGANIZATION = ?";

    public static final String REMOVE_BULK_APIS_DEFAULT_VERSION_SQL = "DELETE FROM AM_API_DEFAULT_VERSION WHERE "
            + "ORGANIZATION = ?";

    public static final String DELETE_BULK_API_WORKFLOWS_REQUEST_SQL = "DELETE FROM AM_WORKFLOWS WHERE " +
            "WF_TYPE='AM_API_STATE' AND WF_REFERENCE IN (SELECT _CONVERT_PLACEHOLDER_ FROM AM_API API " +
            "WHERE API.ORGANIZATION = ?)";

    public static final String DELETE_BULK_KEY_MANAGER_LIST_SQL = "DELETE FROM AM_KEY_MANAGER WHERE ORGANIZATION = ? "
            + "AND UUID IN (_KM_UUIDS_)";

    public static final String KM_UUID_REGEX = "_KM_UUIDS_";

    public static final String DELETE_PENDING_SUBSCRIPTIONS_SQL = "DELETE WF FROM AM_WORKFLOWS WF JOIN AM_SUBSCRIPTION SUB "
            + "ON SUB.SUBSCRIPTION_ID = WF.WF_REFERENCE JOIN AM_APPLICATION APPLICATION ON "
            + "APPLICATION.APPLICATION_ID = SUB.APPLICATION_ID WHERE APPLICATION.ORGANIZATION = ? AND "
            + "WF.WF_TYPE = 'AM_SUBSCRIPTION_CREATION'";

    public static final String DELETE_APPLICATION_CREATION_WORKFLOWS_SQL = "DELETE WF FROM AM_WORKFLOWS WF JOIN "
            + "AM_APPLICATION APP ON WF.WF_REFERENCE = APP.APPLICATION_ID AND WF.WF_TYPE = 'AM_APPLICATION_CREATION' "
            + "AND APP.ORGANIZATION = ?";

    public static final String REMOVE_PENDING_APPLICATION_REGISTRATIONS_SQL = "DELETE WF FROM AM_WORKFLOWS WF JOIN "
            + "AM_APPLICATION_REGISTRATION APP_REG ON WF.WF_EXTERNAL_REFERENCE = APP_REG.WF_REF  JOIN AM_APPLICATION APP on "
            + "APP.APPLICATION_ID = APP_REG.APP_ID WHERE APP.ORGANIZATION = ?";

    public static final String GET_CONSUMER_KEYS_OF_APPLICATION_LIST_SQL = "SELECT MAP.CONSUMER_KEY, MAP.CREATE_MODE, "
            + "AKM.NAME, AKM.ORGANIZATION FROM AM_APPLICATION_KEY_MAPPING MAP JOIN AM_KEY_MANAGER AKM on "
            + "MAP.KEY_MANAGER = AKM.UUID JOIN AM_APPLICATION APP on MAP.APPLICATION_ID = APP.APPLICATION_ID WHERE "
            + "APP.ORGANIZATION = ?";

    public static final String REMOVE_APPLICATION_LIST_FROM_APPLICATIONS_SQL = "DELETE FROM AM_APPLICATION WHERE ORGANIZATION = ?";

    public static final String REMOVE_GROUP_ID_MAPPING_BULK_SQL =
            "DELETE APP_GROUP FROM AM_APPLICATION_GROUP_MAPPING APP_GROUP JOIN AM_APPLICATION APP on "
                    + "APP_GROUP.APPLICATION_ID = APP.APPLICATION_ID";

    public static final String REMOVE_MIGRATED_GROUP_ID_SQL_BULK_SQL =
            "UPDATE AM_APPLICATION APP SET APP.GROUP_ID = '' WHERE APP.ORGANIZATION = ?";

    public static final String REMOVE_AM_URL_MAPPINGS_SQL = "DELETE URLMAP FROM AM_API_URL_MAPPING URLMAP "
            + "JOIN AM_API AM ON AM.API_ID = URLMAP.API_ID WHERE AM.ORGANIZATION = ?";
}
