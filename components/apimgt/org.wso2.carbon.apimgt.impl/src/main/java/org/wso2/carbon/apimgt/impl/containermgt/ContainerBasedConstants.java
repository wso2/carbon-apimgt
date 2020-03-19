/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.IntOrString;

/**
 * This class represents the
 * constants that are used for private-jet mode implementation
 */
public final class ContainerBasedConstants {

    public static final String API_CRD_GROUP = "wso2.com";

    public static final String API_CRD_NAME = "apis." + API_CRD_GROUP;

    public static final String API_CRD_VERSION = "v1alpha1";
    public static final String API_VERSION = API_CRD_GROUP + "/" + API_CRD_VERSION;

    public static final String CRD_KIND = "API";

    public static final String DEPLOYMENT_ENVIRONMENTS = "DeploymentEnvironments";
    public static final String DEPLOYMENT_ENV = "kubernetes";

    public static final String SWAGGER = "swagger";
    public static final String MODE = "privateJet";
    public static final String MASTER_URL = "MasterURL";
    public static final String INGRESS_URL = "IngressURL";
    public static final String NAMESPACE = "Namespace";
    public static final String REPLICAS = "Replicas";
    public static final String SATOKEN = "SAToken";
    public static final String JWT_SECURITY_CR_NAME = "JWTSecurityCustomResourceName";
    public static final String OAUTH2_SECURITY_CR_NAME = "OauthSecurityCustomResourceName";
    public static final String BASICAUTH_SECURITY_CR_NAME = "BasicSecurityCustomResourceName";
    public static final String TYPE = "Type";
    public static final String CLUSTER_INFO = "ClusterInfo";
    public static final String CONTAINER_MANAGEMENT_INFO = "ContainerMgtInfo";
    public static final String PROPERTIES = "Properties";
    public static final String CLUSTER_ID = "ClusterId";
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String CONTAINER_MANAGEMENT = "ContainerMgt";
    public static final String CLASS_NAME = "ClassName";
    public static final String CLIENT_KEY_PASSPHRASE = "javax.net.ssl.keyStorePassword";

    //Service discovery constants
    public static final String SERVICE ="Service"; // my edit
    public static final String SYSTEM_TYPE ="Type";// service discovery system type.
    public static final String SERVICE_NAME ="";
    public static final String SERVICE_TYPE="";
    public static final String EXTERNAL_IP="";
    public static final String CLUSTER_IP="";
    public static final String NODE_PORT="";
    public static String TARGET_PORT=""; // We need only this to connect with the service
    public static final String PORT="";
    public static String PROTOCOL="";
    public static final String SERVICE_DISCOVERY = "ServiceDiscovery";
    public static final String SERVICE_DISCOVERY_TYPES = "ServiceDiscoveryTypes";
    public static final String IMPL_PARAMETERS = "ImplParameters";








    // Service discovery
    /**
     * Life Cycle Events
     */
    public static final String BLOCK = "Block";
    public static final String DEMOTE_TO_CREATED = "Demote to Created";
    public static final String REPUBLISH = "Re-Publish";
    public static final String PUBLISHED = "Published";
    public static final String PUBLISH = "Publish";
    public static final String V1 = "v1";
    public static final String SECURITY = "security";
    public static final String COMPONENTS = "components";
    public static final String SECURITY_SCHEMES = "securitySchemes";
    public static final String DEFAULT = "default";
    public static final String PATHS = "paths";
    public static final String OAUTH_BASICAUTH_APIKEY_MANDATORY = "oauth_basic_auth_api_key_mandatory";
    public static final String OAUTH2 = "oauth2";
    public static final String BASIC_AUTH = "basic_auth";
}
