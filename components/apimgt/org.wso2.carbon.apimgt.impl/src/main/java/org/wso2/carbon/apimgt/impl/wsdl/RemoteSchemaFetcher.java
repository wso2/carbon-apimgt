/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.wsdl;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstraction for fetching the body of a remote XML schema/WSDL document by URL, AFTER the requested URL's
 * host has been validated against the network-security access-control policy. Implementations are expected
 * to perform that validation before opening any connection, so callers (e.g. the WSDL 1.1 remote-import
 * locator) never need to reason about network access-control safety themselves for the initial request.
 * <p>
 * Redirect targets are NOT re-validated: a redirect returned by the server is followed using the JDK's
 * default behavior. This is an accepted residual, consistent with the shipped WSDL 2.0 gate — there is no
 * per-hop revalidation and no "too many redirects" guarantee here.
 */
public interface RemoteSchemaFetcher {

    /**
     * Fetches the body of the given URL. The URL itself is validated against the network-security policy
     * before the fetch; any redirect the server returns is then followed using JDK-default behavior without
     * re-validating the redirect target.
     *
     * @param url the URL to fetch
     * @return an {@link InputStream} over the response body
     * @throws APIManagementException if the URL is blocked by network-security policy
     * @throws IOException            if the fetch fails for a transport reason (e.g. connection failure)
     */
    InputStream fetch(String url) throws APIManagementException, IOException;
}
