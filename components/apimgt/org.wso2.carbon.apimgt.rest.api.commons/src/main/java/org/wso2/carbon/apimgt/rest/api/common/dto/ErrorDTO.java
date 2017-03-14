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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;
/**
 * DTO class for Error
 */
@ApiModel(description = "")
public class ErrorDTO {

    @NotNull private Long code = null;

    @NotNull private String message = null;

    private String description = null;

    private  HashMap<String, String>   paramList = null;

    private List<ErrorListItemDTO> error = new ArrayList<ErrorListItemDTO>();

    /**
     * Method to get the error code
     * @return  error code.
     **/
    @ApiModelProperty(required = true, value = "") @JsonProperty("code") public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    /**
     * Method th get the error message.
     * @return error message.
     */
    @ApiModelProperty(required = true, value = "Error message.") @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A detail description about the error message.
     * @return error description.
     */
    @ApiModelProperty(value = "A detail description about the error message.") @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Preferably an url with more details about the error.
     * @return  map of parameters specific to the error.
     */
    @ApiModelProperty(value = "Preferably an url with more details about the error.") @JsonProperty("moreInfo")
    public HashMap<String, String> getMoreInfo() {
        return paramList;
    }

    public void setMoreInfo(HashMap<String, String> moreInfo) {
        this.paramList = moreInfo;
    }

    /**
     * If there are more than one error list them out. Ex. list out validation errors by each field.
     * @return {@code List<ErrorListItemDTO>}   List of error objects.
     */
    @ApiModelProperty(value = "If there are more than one error list them out. Ex. list out validation errors "
            + "by each field.")
    @JsonProperty("error")

    public List<ErrorListItemDTO> getError() {
        return error;
    }

    public void setError(List<ErrorListItemDTO> error) {
        this.error = error;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorDTO {\n");

        sb.append("  code: ").append(code).append("\n");
        sb.append("  message: ").append(message).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  moreInfo: ").append(paramList).append("\n");
        sb.append("  error: ").append(error).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
