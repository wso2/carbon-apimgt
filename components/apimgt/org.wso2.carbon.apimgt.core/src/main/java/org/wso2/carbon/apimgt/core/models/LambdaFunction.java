/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Object mapper for Lambda function.
 */
public class LambdaFunction {

    private String name;
    private URI endpointURI;

    public LambdaFunction(String name, URI endpointURI) {
        this.name = name;
        this.endpointURI = endpointURI;
    }

    public String getName() {
        return name;
    }

    public URI getEndpointURI() {
        return endpointURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LambdaFunction function = (LambdaFunction) o;
        return name.equals(function.name) && endpointURI.equals(function.endpointURI);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + endpointURI.hashCode();
        return result;
    }
}
