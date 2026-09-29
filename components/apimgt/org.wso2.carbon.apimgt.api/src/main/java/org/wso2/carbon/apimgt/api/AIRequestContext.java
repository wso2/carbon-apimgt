/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.api;

import java.util.List;

/**
 * Details of an outbound AI service request, handed to an {@link AIRequestPropertyEnricher} so that it can decide
 * which additional properties to attach to the request payload.
 * <p>
 * Not every attribute is populated for every AI assistance feature. {@link #getUsername()} is {@code null} on the API
 * publish path, which is triggered by an asynchronous notifier rather than by an end user request, and
 * {@link #getRequestId()} is only populated for the API Chat flows. {@link #getResource()} identifies which AI service
 * operation is being invoked, so an implementation that needs to return different properties per operation can branch
 * on it.
 */
public class AIRequestContext {

    private String username;
    private String organization;
    private String requestId;
    private String resource;

    /**
     * @return the full user name of the invoking user as resolved from the carbon context, or {@code null} when the
     * request is not made on behalf of an end user.
     */
    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    /**
     * @return the organization, that is the tenant domain, the request belongs to.
     */
    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    /**
     * @return correlation id the AI service uses to track the request, populated for the API Chat flows and
     * {@code null} elsewhere.
     */
    public String getRequestId() {

        return requestId;
    }

    public void setRequestId(String requestId) {

        this.requestId = requestId;
    }

    /**
     * @return the AI service resource the request is dispatched to, for example the Marketplace Assistant chat
     * resource. Identifies the AI service operation being invoked.
     */
    public String getResource() {

        return resource;
    }

    public void setResource(String resource) {

        this.resource = resource;
    }
}
