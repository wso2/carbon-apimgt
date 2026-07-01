/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Model class to hold OpenAPI Specification parser options.
 */
public class OASParserOptions {

    private static final Log log = LogFactory.getLog(OASParserOptions.class);

    private boolean explicitStyleAndExplode = true;
    private Integer yamlCodePointLimit = null;
    private String refValidationTenantDomain = null;
    private transient RefValidator refValidator = null;
    private transient HttpClientProvider httpClientProvider = null;
    private long refFetchMaxBytes = 0L;

    /**
     * Network access-control hook. Set by the impl layer to {@code APIUtil::validateRemoteURL} so the parser layer can
     * run the platform/tenant network-security policy on each direct external $ref without a compile-time impl
     * dependency.
     */
    public interface RefValidator {
        void validate(String url, String tenantDomain) throws org.wso2.carbon.apimgt.api.APIManagementException;
    }

    /**
     * Supplies an HTTP client for crawling remote $refs, decoupling spec.parser from impl. The impl layer provides
     * {@code org.apache.http.client.HttpClient} instances (built from the platform truststore/TLS/proxy config); this
     * interface keeps the api module from forcing an Apache HttpComponents compile-time requirement onto callers that
     * never crawl. The crawl uses it to fetch allowed remote $refs and discover nested refs.
     */
    public interface HttpClientProvider {
        org.apache.http.client.HttpClient getClient(String protocol, int port)
                throws org.wso2.carbon.apimgt.api.APIManagementException;
    }

    public OASParserOptions() {
    }

    public OASParserOptions(OASParserOptions other) {
        if (other != null) {
            this.explicitStyleAndExplode = other.explicitStyleAndExplode;
            this.yamlCodePointLimit = other.yamlCodePointLimit;
            this.refValidationTenantDomain = other.refValidationTenantDomain;
            this.refValidator = other.refValidator;
            this.httpClientProvider = other.httpClientProvider;
            this.refFetchMaxBytes = other.refFetchMaxBytes;
        }
    }

    public boolean isExplicitStyleAndExplode() {
        return explicitStyleAndExplode;
    }

    /**
     * Configure whether the parser should explicitly populate the OpenAPI `style` and `explode` attributes when they
     * are omitted in the definition
     *
     * @param explicitStyleAndExplode String value representing boolean
     */
    public void setExplicitStyleAndExplode(String explicitStyleAndExplode) {
        // Treat "false" as false, everything else as true to preserve default behavior
        this.explicitStyleAndExplode = !(Boolean.FALSE.toString()).equalsIgnoreCase(explicitStyleAndExplode);
    }

    public Integer getYamlCodePointLimit() {
        return yamlCodePointLimit;
    }

    /**
     * Set the YAML code point limit based on the configured upload/download file size limit.
     *
     * @param snakeYamlMaxFileSizeLimit Maximum file size limit in megabytes as a String
     */
    public void setYamlCodePointLimit(String snakeYamlMaxFileSizeLimit) {

        if (snakeYamlMaxFileSizeLimit == null || (snakeYamlMaxFileSizeLimit = snakeYamlMaxFileSizeLimit.trim()).isEmpty()) {
            log.debug("YAML size limit for API upload/download is not configured. Using default value.");
            this.yamlCodePointLimit = null;
            return;
        }
        double fileSizeInMB;
        try {
            fileSizeInMB = Double.parseDouble(snakeYamlMaxFileSizeLimit);
        } catch (NumberFormatException e) {
            log.error("Invalid YAML size limit value: " + snakeYamlMaxFileSizeLimit + ". Using default value.");
            this.yamlCodePointLimit = null;
            return;
        }
        if (fileSizeInMB <= 0) {
            log.debug("YAML size limit is zero or negative. Using default value.");
            this.yamlCodePointLimit = null;
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("YAML size limit for API upload/download is set to " + fileSizeInMB + " MB");
        }
        // Consider 4 bytes per character
        double limit = fileSizeInMB * 1024 * 1024 * 4;
        this.yamlCodePointLimit = limit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) limit;
    }

    public String getRefValidationTenantDomain() { return refValidationTenantDomain; }
    public void setRefValidationTenantDomain(String v) { this.refValidationTenantDomain = v; }
    public RefValidator getRefValidator() { return refValidator; }
    public void setRefValidator(RefValidator v) { this.refValidator = v; }
    public HttpClientProvider getHttpClientProvider() { return httpClientProvider; }
    public void setHttpClientProvider(HttpClientProvider v) { this.httpClientProvider = v; }

    public long getRefFetchMaxBytes() {
        return refFetchMaxBytes;
    }

    /**
     * Set the per-document size cap for the remote $ref crawl from the configured OAS import file-size limit (the same
     * limit the top-level by-URL fetch uses). Parsing mirrors {@link #setYamlCodePointLimit(String)}: a
     * null/blank/non-numeric/non-positive value leaves the cap unset (0), in which case the crawl applies its own
     * fallback. The value is interpreted in megabytes.
     *
     * @param maxFileSizeMB maximum fetched-document size in megabytes as a String (e.g. "10")
     */
    public void setRefFetchMaxFileSize(String maxFileSizeMB) {
        if (maxFileSizeMB == null || (maxFileSizeMB = maxFileSizeMB.trim()).isEmpty()) {
            this.refFetchMaxBytes = 0L;
            return;
        }
        double fileSizeInMB;
        try {
            fileSizeInMB = Double.parseDouble(maxFileSizeMB);
        } catch (NumberFormatException e) {
            log.error("Invalid remote $ref fetch size limit value: " + maxFileSizeMB + ". Using crawl default.");
            this.refFetchMaxBytes = 0L;
            return;
        }
        if (fileSizeInMB <= 0) {
            this.refFetchMaxBytes = 0L;
            return;
        }
        double bytes = fileSizeInMB * 1024 * 1024;
        this.refFetchMaxBytes = bytes > Long.MAX_VALUE ? Long.MAX_VALUE : (long) bytes;
    }

}
