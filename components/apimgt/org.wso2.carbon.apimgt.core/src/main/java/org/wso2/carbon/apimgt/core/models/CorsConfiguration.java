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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CORS Config related information
 */
public class CorsConfiguration {
    private boolean isEnabled = false;
    private boolean isAllowCredentials = false;
    private List<String>  allowOrigins = new ArrayList<>();
    private List<String>  allowHeaders = new ArrayList<>();
    private List<String>  allowMethods = new ArrayList<>();

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isAllowCredentials() {
        return isAllowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        isAllowCredentials = allowCredentials;
    }

    public List<String> getAllowOrigins() {
        return allowOrigins;
    }

    public void setAllowOrigins(List<String> allowOrigins) {
        this.allowOrigins = allowOrigins;
    }

    public List<String> getAllowHeaders() {
        return allowHeaders;
    }

    public void setAllowHeaders(List<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    public List<String> getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(List<String> allowMethods) {
        this.allowMethods = allowMethods;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CorsConfiguration that = (CorsConfiguration) o;
        return isEnabled == that.isEnabled &&
                isAllowCredentials == that.isAllowCredentials &&
                APIUtils.isListsEqualIgnoreOrder(allowOrigins, that.allowOrigins) &&
                APIUtils.isListsEqualIgnoreOrder(allowHeaders, that.allowHeaders) &&
                APIUtils.isListsEqualIgnoreOrder(allowMethods, that.allowMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnabled, isAllowCredentials, allowOrigins, allowHeaders, allowMethods);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isEnabled", isEnabled)
                .append("isAllowCredentials", isAllowCredentials)
                .append("allowOrigins", allowOrigins)
                .append("allowHeaders", allowHeaders)
                .append("allowMethods", allowMethods)
                .toString();
    }
}
