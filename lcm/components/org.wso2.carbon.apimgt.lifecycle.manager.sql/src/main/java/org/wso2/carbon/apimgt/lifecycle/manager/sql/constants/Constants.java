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

/**
 * This class contains constants which is referred when performing database operations.
 */
public class Constants {
    public static final String LIFECYCLE_DATASOURCE = "java:comp/env/jdbc/WSO2LifecycleDB";
    public static final String LIFECYCLE_DB_NAME = "WSO2_LIFECYCLE_DB";
    public static final String LC_DEFINITION_TABLE_NAME = "LC_DEFINITIONS";
    public static final int SUPER_TENANT_ID = -1234;
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final String CARBON_HOME = "carbon.home";
    public static final String LIFECYCLE_LIST = "LIFECYCLE_LIST";
    public static final String LIFECYCLE_NAME = "LIFECYCLE_NAME";
    public static final String LIFECYCLE_DEFINITION_ID = "LIFECYCLE_DEFINITION_ID";
    public static final String LIFECYCLE_STATUS = "LIFECYCLE_STATUS";
    public static final String LIFECYCLE_CONTENT = "LIFECYCLE_CONTENT";
    public static final String TENANT_ID = "TENANT_ID";
    public static final String LC_ID = "LC_ID";
}
