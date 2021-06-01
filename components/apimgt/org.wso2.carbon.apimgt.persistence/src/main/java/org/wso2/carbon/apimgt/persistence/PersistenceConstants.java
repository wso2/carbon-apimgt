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

package org.wso2.carbon.apimgt.persistence;

public class PersistenceConstants {

    //Mongodb related
    public static final String REGISTRY_CONFIG_API_URI = "RegistryConfigs.APIUri";
    public static final String REGISTRY_CONFIG_GROUP_ID = "RegistryConfigs.GroupId";
    public static final String REGISTRY_CONFIG_CLUSTER_NAME = "RegistryConfigs.ClusterName";
    public static final String REGISTRY_CONFIG_PUBLIC_KEY = "RegistryConfigs.PublicKey";
    public static final String REGISTRY_CONFIG_PRIVATE_KEY = "RegistryConfigs.PrivateKey";
    public static final String REGISTRY_CONFIG_CONNECTION_STRING = "RegistryConfigs.ConnectionString";
    public static final String REGISTRY_CONFIG_TYPE = "RegistryConfigs.Type";
    public static final String REGISTRY_CONFIG_TREAD_COUNT = "RegistryConfigs.ThreadCount";
    public static final String REGISTRY_CONFIG_RETRY_COUNT = "RegistryConfigs.RetryCount";
    public static final String REGISTRY_CONFIG_TYPE_MONGODB = "mongodb";
    public static final int DEFAULT_RETRY_COUNT = 3;
    public static final int DEFAULT_TREAD_COUNT = 5;

}
