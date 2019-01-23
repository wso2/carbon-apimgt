/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util;

/**
 * Constants Class
 */
public class MicroGatewayAPIUsageConstants {

    public static final String GZIP_EXTENSION = ".gz";

    public static final String API_USAGE_OUTPUT_DIRECTORY = "api-usage-data";

    public static final String API_USAGE_FILE_CONTENT = "FILE_CONTENT";

    public static final String API_USAGE_OUTPUT_FILE_NAME = "api-usage-data.dat";

    public static final String FILE_NAME_HEADER = "FileName";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String ACCEPT_HEADER = "Accept";

    public static final String ACCEPT_HEADER_APPLICATION_JSON = "application/json";

    public static final String IS_UPLOADED_USAGE_DATA_PUBLISH_ENABLED_PROPERTY = "usage.data.publishing.enabled";

    public static final String UPLOADED_USAGE_PUBLISH_FREQUENCY_PROPERTY = "usage.publishing.frequency";

    public static final String WORKER_THREAD_COUNT_PROPERTY = "microGatewayUsagePublishers";

    public static final int DEFAULT_UPLOADED_USAGE_PUBLISH_FREQUENCY = 300000;

    public static final String UPLOADED_FILE_SUFFIX = ".uploaded";

    public static final String USAGE_UPLOAD_SERVICE_URL = "usage.upload.service.url";
    public static final String DEFAULT_UPLOAD_SERVICE_URL = "https://localhost:9443/micro-gateway/v0.9/upload-file";

    public static final String STREAM_DEFINITIONS_DIRECTORY = "stream-definitions";

    public static final String INSERT_UPLOADED_FILE_INFO_QUERY = "INSERT INTO AM_USAGE_UPLOADED_FILES "
            + "(TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP,FILE_CONTENT) VALUES(?,?,?,?)";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_DEFAULT =
            "SELECT TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES "
                    + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP LIMIT ? FOR UPDATE;";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_ORACLE = "SELECT TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP "
            + "FROM AM_USAGE_UPLOADED_FILES WHERE rownum<=? AND "
            + "FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP FOR UPDATE";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_MSSQL = "SELECT TOP (?) TENANT_DOMAIN,FILE_NAME,"
            + "FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES WITH (UPDLOCK) "
            + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP";

    public static final String GET_NEXT_FILES_TO_PROCESS_QUERY_DB2 =
            "SELECT TENANT_DOMAIN,FILE_NAME,FILE_TIMESTAMP FROM AM_USAGE_UPLOADED_FILES "
                    + "WHERE FILE_PROCESSED=0 ORDER BY FILE_TIMESTAMP LIMIT ? FOR UPDATE";

    public static final String UPDATE_FILE_PROCESSING_STARTED_STATUS = "UPDATE AM_USAGE_UPLOADED_FILES "
            + "SET FILE_PROCESSED=1 WHERE FILE_PROCESSED=0 AND TENANT_DOMAIN = ? AND FILE_NAME = ?";

    public static final String DELETE_OLD_UPLOAD_COMPLETED_FILES = "DELETE FROM AM_USAGE_UPLOADED_FILES "
            + "WHERE (FILE_PROCESSED=1 OR FILE_PROCESSED=2) AND FILE_TIMESTAMP < ?";

    public static final String GET_UPLOADED_FILE_CONTENT_QUERY = "SELECT FILE_CONTENT "
            + "FROM AM_USAGE_UPLOADED_FILES WHERE TENANT_DOMAIN=? AND FILE_NAME=?";

    public static final String UPDATE_COMPETITION_QUERY = "UPDATE AM_USAGE_UPLOADED_FILES "
            + "SET FILE_PROCESSED=2,FILE_CONTENT=NULL WHERE TENANT_DOMAIN=? AND FILE_NAME=?";

    public static final int DEFAULT_WORKER_THREAD_COUNT = 3;

    public static final int MAX_RETRY_COUNT = 3;

    // Separators used for persisting events
    public static final String EVENT_SEPARATOR = "--EVT--";

    public static final String KEY_VALUE_SEPARATOR = "--KVS--";

    public static final String OBJECT_SEPARATOR = "--OBJ--";
}
