package org.wso2.carbon.apimgt.core;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//@Configuration(namespace = "wso2.carbon", description = "Carbon Configuration Parameters")

/**
 *
 */
public class APIMConfigurations {
    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    public static final String CF_NAME_PREFIX = "connectionfactory.";
    public static final String CF_NAME = "qpidConnectionfactory";
    public static final String CARBON_CLIENT_ID = "carbon";
    public static final String CARBON_VIRTUAL_HOST_NAME = "carbon";
    public static final String CARBON_DEFAULT_HOSTNAME = "localhost";
    public static final String CARBON_DEFAULT_PORT = "5672";
    public static final String TOPIC_NAME = "MYTopic";
    public static final String USERNAME = "MYTopic";
    public static final String PASSWORD = "MYTopic";
}
