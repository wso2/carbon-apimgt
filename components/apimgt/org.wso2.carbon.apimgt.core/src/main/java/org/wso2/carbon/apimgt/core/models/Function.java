/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

import java.net.URI;
import java.util.Objects;

/**
 * Object mapper for Function.
 */
public class Function {

    private String name;
    private URI endpointURI;

    /**
     * Constructor.
     *
     * @param name        Name of the function
     * @param endpointURI Endpoint URI of the function
     */
    public Function(String name, URI endpointURI) {
        if (name == null) {
            throw new IllegalArgumentException("Function name must not be null");
        }
        if (endpointURI == null) {
            throw new IllegalArgumentException("Function endpoint URI must not be null");
        }
        this.name = name;
        this.endpointURI = endpointURI;
    }

    /**
     * To get the function name.
     *
     * @return Name of the function
     */
    public String getName() {
        return name;
    }

    /**
     * To get the endpoint URI of the function.
     *
     * @return Endpoint URI of the function
     */
    public URI getEndpointURI() {
        return endpointURI;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Function) {
            Function functionObj = (Function) obj;
            return name != null && name.equals(functionObj.getName()) && endpointURI != null && endpointURI
                    .equals(functionObj.getEndpointURI());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, endpointURI);
    }
}
