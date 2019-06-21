/*
 *
 *  * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.kerberos;

/**
 * Constants for IWA federated authenticator application
 */
public class KerberosConstants {

    public static final String UTF_8 = "UTF-8";
    public static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    public static final String LOGIN_CONFIG_PROPERTY = "java.security.auth.login.config";
    public static final String LOGIN_CONF_FILE_NAME = "login.conf";
    public static final String KERBEROS_CONFIG_PROPERTY = "java.security.krb5.conf";
    public static final String KERBEROS_CONF_FILE_NAME = "krb5.conf";
    public static final String LOGIN_CONTEXT_NAME = "loginContextName";
    // Negotiate header string.
    public static final String NEGOTIATE = "Negotiate";


    private KerberosConstants() {
    }
}
