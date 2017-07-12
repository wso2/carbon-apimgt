/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.util;


import java.io.File;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class ThrottleConstants {

    //Starts DAS based throttling policy implementation related constants
    public static final String CPS_SERVER_URL = "CPSServerUrl";
    public static final String CPS_SERVER_USERNAME = "CPSServerUsername";
    public static final String CPS_SERVER_PASSWORD = "CPSServerPassword";
    public static final String POLICY_FILE_FOLDER = "repository" + File.separator + "deployment" + File.separator +
            "server" + File.separator + "throttle-config";
    public static final String SEQUENCE_FILE_FOLDER = "repository" + File.separator + "deployment" + File.separator +
            "server" + File.separator + "synapse-configs" + File.separator + "default" + File.separator + "sequences";
    public static final String POLICY_FILE_LOCATION = POLICY_FILE_FOLDER + File.separator;
    public static final String SEQUENCE_FILE_LOCATION = SEQUENCE_FILE_FOLDER + File.separator;

    public static final String ELIGIBILITY_QUERY_ELEM = "eligibilityQuery";
    public static final String POLICY_NAME_ELEM = "name";
    public static final String DECISION_QUERY_ELEM = "decisionQuery";
    public static final String XML_EXTENSION = ".xml";

    public static final String POLICY_TEMPLATE_KEY = "keyTemplateValue";
    public static final String TEMPLATE_KEY_STATE = "keyTemplateState";

    public static final String THROTTLE_POLICY_DEFAULT = "_default";

    //Advanced throttling related constants
    public static final String TIME_UNIT_SECOND = "sec";
    public static final String TIME_UNIT_MINUTE = "min";
    public static final String TIME_UNIT_HOUR = "hour";
    public static final String TIME_UNIT_DAY = "day";

    public static final String DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN = "50PerMin";
    public static final String DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN = "20PerMin";
    public static final String DEFAULT_APP_POLICY_TEN_REQ_PER_MIN = "10PerMin";
    public static final String DEFAULT_APP_POLICY_UNLIMITED = "Unlimited";


    public static final String DEFAULT_APP_POLICY_LARGE_DESC = "Allows 50 request per minute";
    public static final String DEFAULT_APP_POLICY_MEDIUM_DESC = "Allows 20 request per minute";
    public static final String DEFAULT_APP_POLICY_SMALL_DESC = "Allows 10 request per minute";
    public static final String DEFAULT_APP_POLICY_UNLIMITED_DESC = "Allows unlimited requests";

    public static final String DEFAULT_SUB_POLICY_GOLD = "Gold";
    public static final String DEFAULT_SUB_POLICY_SILVER = "Silver";
    public static final String DEFAULT_SUB_POLICY_BRONZE = "Bronze";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED = "Unlimited";
    public static final String DEFAULT_SUB_POLICY_UNAUTHENTICATED = "Unauthenticated";

    public static final String DEFAULT_SUB_POLICY_GOLD_DESC = "Allows 5000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_SILVER_DESC = "Allows 2000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_BRONZE_DESC = "Allows 1000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED_DESC = "Allows unlimited requests";
    public static final String DEFAULT_SUB_POLICY_UNAUTHENTICATED_DESC = "Allows 500 request(s) per minute";

    public static final String DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN = "50KPerMin";
    public static final String DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN = "20KPerMin";
    public static final String DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN = "10KPerMin";
    public static final String DEFAULT_API_POLICY_UNLIMITED = "Unlimited";

    public static final String DEFAULT_API_POLICY_ULTIMATE_DESC = "Allows 50000 requests per minute";
    public static final String DEFAULT_API_POLICY_PLUS_DESC = "Allows 20000 requests per minute";
    public static final String DEFAULT_API_POLICY_BASIC_DESC = "Allows 10000 requests per minute";
    public static final String DEFAULT_API_POLICY_UNLIMITED_DESC = "Allows unlimited requests";

    public static final String API_POLICY_USER_LEVEL = "userLevel";
    public static final String API_POLICY_API_LEVEL = "apiLevel";

    public static final String BILLING_PLAN_FREE = "FREE";

    //    need to be used with siddhi M4 onward
    public static final String DAS_REST_API_PATH_ARTIFACT_DEPLOY = "/siddhi-apps/";

}
