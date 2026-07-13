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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.SizeLimitedInputStream;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Policy-gated {@link RemoteSchemaFetcher} used to retrieve remote XML schema/WSDL documents (e.g. the
 * WSDL 1.1 remote-import locator's nested {@code xsd:import}/{@code include} targets). The requested URL is
 * validated against the network-security access-control policy ({@link APIUtil#validateRemoteURL(String,
 * String)}) BEFORE a connection is opened to it, so a request that targets a blocked host is never made.
 * <p>
 * Redirect handling is intentionally out of scope: any redirect returned by the server is followed using
 * the JDK's default behavior without re-validation, matching the accepted residual of the shipped WSDL 2.0
 * gate. This class does not disable or loop over redirects itself.
 * <p>
 * The response body is wrapped in a {@link SizeLimitedInputStream} enforcing the same
 * {@code API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT} configuration limit used by
 * {@link WSDL11ProcessorImpl#init(URL)} for direct WSDL fetches, so a remote schema fetched through this
 * class cannot be used to exhaust memory/disk.
 */
public class PolicyGatedSchemaFetcher implements RemoteSchemaFetcher {

    private static final Logger log = LoggerFactory.getLogger(PolicyGatedSchemaFetcher.class);

    private final String tenantDomain;
    private final RemoteUrlValidator validator;
    private final long maxFileSize;

    public PolicyGatedSchemaFetcher(String tenantDomain) {
        this(tenantDomain, APIUtil::validateRemoteURL);
    }

    PolicyGatedSchemaFetcher(String tenantDomain, RemoteUrlValidator validator) {
        this(tenantDomain, validator, getMaxFileSize());
    }

    PolicyGatedSchemaFetcher(String tenantDomain, RemoteUrlValidator validator, long maxFileSize) {
        this.tenantDomain = tenantDomain;
        this.validator = validator;
        this.maxFileSize = maxFileSize;
    }

    @Override
    public InputStream fetch(String url) throws APIManagementException, IOException {
        validator.validate(url, tenantDomain);
        return new SizeLimitedInputStream(new URL(url).openStream(), maxFileSize);
    }

    /**
     * Resolves the maximum allowed remote-schema body size, reusing the same configuration key
     * ({@code API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT}) that {@link WSDL11ProcessorImpl#init(URL)} reads
     * for direct WSDL fetches. Falls back to the same default if the value cannot be resolved.
     * <p>
     * The API Manager configuration service being unavailable (e.g. in unit tests, before OSGi wiring) is
     * expected and falls back quietly. Any other failure (e.g. a non-numeric configured value) is
     * unexpected and is logged at WARN so a misconfiguration is visible instead of silently falling back.
     */
    private static long getMaxFileSize() {
        try {
            String maxWSDLSizeStr = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration().getFirstProperty(
                            APIConstants.API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT);
            if (maxWSDLSizeStr == null || maxWSDLSizeStr.trim().isEmpty()) {
                maxWSDLSizeStr = APIConstants.API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT_DEFAULT_MB;
            }
            return Long.parseLong(maxWSDLSizeStr) * 1024L * 1024L;
        } catch (NullPointerException e) {
            // The API Manager configuration service is not available (e.g. running outside OSGi in a unit
            // test) — this is expected in that context, so fall back to the default quietly.
            return defaultMaxFileSize();
        } catch (NumberFormatException e) {
            log.warn("Configured WSDL import file size limit ('" + APIConstants
                    .API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT + "') is not a valid number — falling back to "
                    + "the default of " + APIConstants.API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT_DEFAULT_MB
                    + "MB", e);
            return defaultMaxFileSize();
        }
    }

    private static long defaultMaxFileSize() {
        return Long.parseLong(APIConstants.API_PUBLISHER_IMPORT_WSDL_FILE_SIZE_LIMIT_DEFAULT_MB) * 1024L * 1024L;
    }

    /**
     * Validates a URL against the network-security access-control policy before it is fetched. Extracted
     * as a functional interface so tests can supply a fake without needing to mock {@link APIUtil}.
     */
    @FunctionalInterface
    interface RemoteUrlValidator {
        void validate(String url, String tenantDomain) throws APIManagementException;
    }
}
