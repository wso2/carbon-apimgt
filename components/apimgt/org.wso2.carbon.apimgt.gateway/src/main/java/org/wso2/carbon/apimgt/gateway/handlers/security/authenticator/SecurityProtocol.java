/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.authenticator;

/**
 * Represents the Security protocol that is supported in API Gateway level.
 */
public class SecurityProtocol {
    private String protocolName;
    private String authenticatorClassName;
    private String[] listOfParameters;
    private Class[] parameterTypes;

    /**
     * Constructs a security protocol object.
     *
     * @param protocolName           Name of the security protocol.
     * @param authenticatorClassName Name of the class of the authenticator.
     * @param parameters             list of parameter names that need to be passed to the constructor.
     * @param parameterTypes         list of parameter types that need to be passed to the constructor.
     */
    public SecurityProtocol(String protocolName, String authenticatorClassName, String[] parameters,
            Class[] parameterTypes) {
        this.protocolName = protocolName;
        this.authenticatorClassName = authenticatorClassName;
        this.listOfParameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    /**
     * To get list of parameters of a security protocol.
     *
     * @return list of parameters that need to be passed to the security protocol.
     */
    public String[] getListOfParameters() {
        return listOfParameters;
    }

    /**
     * To get the authenticator class name.
     *
     * @return authenticator class name.
     */
    public String getAuthenticatorClassName() {
        return authenticatorClassName;
    }

    /**
     * To get the security protocol name.
     *
     * @return Name of the security protocol.
     */
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * To get the list of parameter types that need to be passed while constructing the protocol.
     *
     * @return relevant parameter type.
     */
    public Class[] getParameterTypes() {
        return parameterTypes;
    }
}
