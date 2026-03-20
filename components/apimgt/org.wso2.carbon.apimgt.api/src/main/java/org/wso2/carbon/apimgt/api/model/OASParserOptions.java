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

    public OASParserOptions() {
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

}