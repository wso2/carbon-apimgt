/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

/**
 * Thrown when a reference encountered while compiling an XSD (a nested
 * xsd:import/include/redefine or an external DTD) is not permitted by the network
 * access-control policy, or uses a non-HTTP(S) scheme. Unchecked because it must
 * propagate out of {@link org.w3c.dom.ls.LSResourceResolver#resolveResource}, which
 * cannot declare checked exceptions.
 */
public class XsdRefBlockedException extends RuntimeException {

    public XsdRefBlockedException(String message) {
        super(message);
    }

    public XsdRefBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
