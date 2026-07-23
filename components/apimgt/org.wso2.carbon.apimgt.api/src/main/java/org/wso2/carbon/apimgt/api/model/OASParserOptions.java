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

import java.util.List;

/**
 * Model class to hold OpenAPI Specification parser options.
 */
public class OASParserOptions {

    private static final Log log = LogFactory.getLog(OASParserOptions.class);

    private boolean explicitStyleAndExplode = true;
    private Integer yamlCodePointLimit = null;
    private List<String> remoteRefAllowList;
    private List<String> remoteRefBlockList;
    private boolean networkAccessControlEnabled = false;

    public OASParserOptions() {
    }

    /**
     * Copy-constructor. Copies fields directly (no re-conversion) so that already-computed values such as
     * {@code yamlCodePointLimit} are not re-interpreted through their String setters.
     *
     * @param other the instance to copy from; if {@code null}, this instance retains its default values
     */
    public OASParserOptions(OASParserOptions other) {
        if (other != null) {
            this.explicitStyleAndExplode = other.explicitStyleAndExplode;
            this.yamlCodePointLimit = other.yamlCodePointLimit;
            this.remoteRefAllowList = other.remoteRefAllowList;
            this.remoteRefBlockList = other.remoteRefBlockList;
            this.networkAccessControlEnabled = other.networkAccessControlEnabled;
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

    public List<String> getRemoteRefAllowList() {
        return remoteRefAllowList;
    }

    public void setRemoteRefAllowList(List<String> remoteRefAllowList) {
        this.remoteRefAllowList = remoteRefAllowList;
    }

    public List<String> getRemoteRefBlockList() {
        return remoteRefBlockList;
    }

    public void setRemoteRefBlockList(List<String> remoteRefBlockList) {
        this.remoteRefBlockList = remoteRefBlockList;
    }

    public boolean isNetworkAccessControlEnabled() {
        return networkAccessControlEnabled;
    }

    /**
     * Configure whether the network access-control policy is in force for remote {@code $ref} resolution. When
     * {@code false} (the default), the parser retains its historical behaviour and resolves remote refs without any
     * host validation - preserving backwards compatibility for deployments that have not configured the policy. It
     * is set to {@code true} only when a platform or tenant network access-control policy is present.
     *
     * @param networkAccessControlEnabled whether the network access-control policy is configured
     */
    public void setNetworkAccessControlEnabled(boolean networkAccessControlEnabled) {
        this.networkAccessControlEnabled = networkAccessControlEnabled;
    }

}
