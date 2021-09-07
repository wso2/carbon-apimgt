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
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SolaceTopicsDTO;

import java.util.Objects;

import javax.validation.Valid;

public class ApplicationSolaceTopicsObjectDTO   {

    private SolaceTopicsDTO defaultSyntax = null;
    private SolaceTopicsDTO mqttSyntax = null;

    public ApplicationSolaceTopicsObjectDTO defaultSyntax(SolaceTopicsDTO defaultSyntax) {
        this.defaultSyntax = defaultSyntax;
        return this;
    }

    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("defaultSyntax")
    public SolaceTopicsDTO getDefaultSyntax() {
        return defaultSyntax;
    }
    public void setDefaultSyntax(SolaceTopicsDTO defaultSyntax) {
        this.defaultSyntax = defaultSyntax;
    }

    public ApplicationSolaceTopicsObjectDTO mqttSyntax(SolaceTopicsDTO mqttSyntax) {
        this.mqttSyntax = mqttSyntax;
        return this;
    }

    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("mqttSyntax")
    public SolaceTopicsDTO getMqttSyntax() {
        return mqttSyntax;
    }

    public void setMqttSyntax(SolaceTopicsDTO mqttSyntax) {
        this.mqttSyntax = mqttSyntax;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationSolaceTopicsObjectDTO applicationSolaceTopicsObject = (ApplicationSolaceTopicsObjectDTO) o;
        return Objects.equals(defaultSyntax, applicationSolaceTopicsObject.defaultSyntax) &&
                Objects.equals(mqttSyntax, applicationSolaceTopicsObject.mqttSyntax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultSyntax, mqttSyntax);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApplicationSolaceTopicsObjectDTO {\n");

        sb.append("    defaultSyntax: ").append(toIndentedString(defaultSyntax)).append("\n");
        sb.append("    mqttSyntax: ").append(toIndentedString(mqttSyntax)).append("\n");
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
