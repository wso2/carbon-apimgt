/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
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

/**
 * Model class to hold OpenAPI Specification parser options.
 */
public class OASParserOptions {

    private boolean explicitStyleAndExplode = true;

    public OASParserOptions() {
    }

    public boolean isExplicitStyleAndExplode() {
        return explicitStyleAndExplode;
    }

    /**
     * Configure whether the parser should explicitly populate the OpenAPI `style` and `explode`
     * attributes when they are omitted in the definition
     *
     * @param explicitStyleAndExplode String value representing boolean
     */
    public void setExplicitStyleAndExplode(String explicitStyleAndExplode) {
        // Treat "false" as false, everything else as true to preserve default behavior
        this.explicitStyleAndExplode = !(Boolean.FALSE.toString()).equalsIgnoreCase(explicitStyleAndExplode);
    }

}