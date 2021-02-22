/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.mongodb;

public class MongoDBConstants {

    public static final String MONGODB_DEFAULT_DATABASE = "APIM_DB";
    public static final String MONGODB_COLLECTION_SUR_FIX = "_apis";
    public static final String MONGODB_GRIDFS_THMBNAIL_SUR_FIX = "_thumbnail";
    public static final String MONGODB_COLLECTION_DEFAULT_ORG = "carbon.super";

    /**
     * Constants for correlation logging
     * */
    public static final String CORRELATION_ID = "Correlation-ID";
    public static final String ENABLE_CORRELATION_LOGS = "enableCorrelationLogs";
    public static final String CORRELATION_LOGGER = "correlation";
    public static final String LOG_ALL_METHODS = "logAllMethods";
    public static final String AM_ACTIVITY_ID = "activityid";
}
