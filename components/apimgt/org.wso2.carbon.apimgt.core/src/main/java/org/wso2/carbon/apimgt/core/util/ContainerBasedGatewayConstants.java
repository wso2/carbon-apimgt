/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.core.util;


/**
 * This class represents the
 * constants that are used for APIManager implementation
 */
public final class ContainerBasedGatewayConstants {

    public static final String PER_API_GATEWAY_PREFIX = "perapigw-";
    public static final String CMS_SERVICE_SUFFIX = "-service";
    public static final String CMS_DEPLOYMENT_SUFFIX = "-deployemnt";
    public static final String GATEWAY_SERVICE_TEMPLATE = "container_service_template.yaml";
    public static final String GATEWAY_DEPLOYMENT_TEMPLATE = "container_deployment_template.yaml";
    public static final String NAMESPACE = "namespace";
    public static final String GATEWAY_LABEL = "gatewayLabel";
    public static final String SERVICE_NAME = "serviceName";
    public static final String DEPLOYMENT_NAME = "deploymentName";
    public static final String CONTAINER_NAME = "containerName";
    public static final String DOCKER_IMAGE = "image";
    public static final String API_CORE_URL = "apiCoreURL";
    public static final String BROKER_HOST = "brokerHost";
    public static final String CMS_TYPE = "cmsType";
    public static final String SERVICE_ACCOUNT_TOKEN_FILE = "saTokenFile";
    public static final String K8 = "kubernetes";
    public static final String OPENSHIFT = "openshift";
    public static final String CERT_FILE_LOCATION = "certFile";
    public static final String IS_DEDICATED_GATEWAY_ENABLED = "HAS_OWN_GATEWAY";


    public static final String PROPERTY_KUB_CONFIG_BASE64_ENCODED_DATA = "KubernetesClusterConfig.Base64EncodedData";
    public static final String PROPERTY_KUB_TMP_CONFIG = "KubernetesClusterConfig.TempKubeConfigFile";

}
