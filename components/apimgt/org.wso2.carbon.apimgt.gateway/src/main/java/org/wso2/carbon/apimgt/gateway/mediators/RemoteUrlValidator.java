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

import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 * Seam over the network access-control gate so the remote-fetch validation can be
 * unit-tested without the full {@code APIUtil} static context. Production wiring delegates to
 * {@code APIUtil.validateRemoteURL(url, tenantDomain)}.
 */
@FunctionalInterface
public interface RemoteUrlValidator {

    /**
     * @param url absolute URL to check.
     * @throws APIManagementException if the URL is not permitted by the configured
     *                                network access-control policy.
     */
    void validate(String url) throws APIManagementException;
}
