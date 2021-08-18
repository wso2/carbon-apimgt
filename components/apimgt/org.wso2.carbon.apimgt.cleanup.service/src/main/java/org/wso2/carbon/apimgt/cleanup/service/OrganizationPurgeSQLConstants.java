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
    public static final String DELETE_PENDING_SUBSCRIPTIONS = "DELETE WF FROM AM_WORKFLOWS WF JOIN AM_SUBSCRIPTION SUB "
            + "ON SUB.SUBSCRIPTION_ID = WF.WF_REFERENCE JOIN AM_APPLICATION APPLICATION ON "
            + "APPLICATION.APPLICATION_ID = SUB.APPLICATION_ID WHERE APPLICATION.ORGANIZATION = ? AND "
            + "WF.WF_TYPE = 'AM_SUBSCRIPTION_CREATION'";

    public static final String DELETE_APPLICATION_CREATION_WORKFLOWS = "DELETE WF FROM AM_WORKFLOWS WF JOIN "
            + "AM_APPLICATION APP ON WF.WF_REFERENCE = APP.APPLICATION_ID AND WF.WF_TYPE = 'AM_APPLICATION_CREATION' "
            + "AND APP.ORGANIZATION = ?";


    public static final String REMOVE_PENDING_APPLICATION_REGISTRATIONS = "DELETE WF FROM AM_WORKFLOWS WF JOIN "
            + "AM_APPLICATION_REGISTRATION APP_REG ON WF.WF_EXTERNAL_REFERENCE = APP_REG.WF_REF  JOIN AM_APPLICATION APP on "
            + "APP.APPLICATION_ID = APP_REG.APP_ID WHERE APP.ORGANIZATION = ?";

    public static final String GET_CONSUMER_KEYS_OF_APPLICATION_LIST_SQL = "SELECT MAP.CONSUMER_KEY, MAP.CREATE_MODE, "
            + "AKM.NAME, AKM.ORGANIZATION FROM AM_APPLICATION_KEY_MAPPING MAP JOIN AM_KEY_MANAGER AKM on "
            + "MAP.KEY_MANAGER = AKM.UUID JOIN AM_APPLICATION APP on MAP.APPLICATION_ID = APP.APPLICATION_ID WHERE "
            + "APP.ORGANIZATION = ?";

    public static final String REMOVE_APPLICATION_LIST_FROM_APPLICATIONS_SQL = "DELETE FROM AM_APPLICATION WHERE ORGANIZATION = ?";
}
