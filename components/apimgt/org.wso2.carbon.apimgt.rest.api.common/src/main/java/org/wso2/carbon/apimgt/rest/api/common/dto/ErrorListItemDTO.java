/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class ErrorListItemDTO   {

    private String code = null;
    private String message = null;
    private String description = null;

    /**
     **/
    public ErrorListItemDTO code(String code) {
        this.code = code;
        return this;
    }


    @ApiModelProperty(required = true, value = "")
    @JsonProperty("code")
    @NotNull
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Description about individual errors occurred
     **/
    public ErrorListItemDTO message(String message) {
        this.message = message;
        return this;
    }


    @ApiModelProperty(required = true, value = "Description about individual errors occurred ")
    @JsonProperty("message")
    @NotNull
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A detail description about the error message.
     **/
    public ErrorListItemDTO description(String description) {
        this.description = description;
        return this;
    }


    @ApiModelProperty(value = "A detail description about the error message. ")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorListItemDTO errorListItem = (ErrorListItemDTO) o;
        return Objects.equals(code, errorListItem.code) &&
                Objects.equals(message, errorListItem.message) &&
                Objects.equals(description, errorListItem.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorListItemDTO {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

