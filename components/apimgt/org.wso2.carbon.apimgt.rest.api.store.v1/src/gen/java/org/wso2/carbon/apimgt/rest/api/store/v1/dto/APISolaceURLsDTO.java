/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class APISolaceURLsDTO   {

    private String protocol = null;
    private String endpointURL = null;

    public APISolaceURLsDTO protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @ApiModelProperty(example = "Defalt", value = "")
    @JsonProperty("protocol")
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public APISolaceURLsDTO endpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
        return this;
    }

    @ApiModelProperty(example = "Default", value = "")
    @JsonProperty("endpointURL")
    public String getEndpointURL() {
        return endpointURL;
    }
    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APISolaceURLsDTO apISolaceURLs = (APISolaceURLsDTO) o;
        return Objects.equals(protocol, apISolaceURLs.protocol) &&
                Objects.equals(endpointURL, apISolaceURLs.endpointURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, endpointURL);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class APISolaceURLsDTO {\n");

        sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
        sb.append("    endpointURL: ").append(toIndentedString(endpointURL)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
