/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.configurations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import javax.validation.constraints.NotNull;

/**
 * DTO class for Error
 */
@ApiModel(description = "Error DTO for Configurations API")
public class ErrorDTO {

    @NotNull
    private long code;

    @NotNull
    private String message = null;

    private String description = null;

    private HashMap<String, String> paramList = null;


    /**
     * Error-Code value.
     */
    @ApiModelProperty(required = true, value = "Error Code")
    @JsonProperty("code")
    public Long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    /**
     * Error message.
     */
    @ApiModelProperty(required = true, value = "Error Message.")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A detailed description about the error message.
     */
    @ApiModelProperty(value = "A detailed description about the error message.")
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Preferably an url with more details about the error.
     */
    @ApiModelProperty(value = "Preferably an url with more details about the error.")
    @JsonProperty("moreInfo")
    public HashMap<String, String> getMoreInfo() {
        return paramList;
    }

    public void setMoreInfo(HashMap<String, String> moreInfo) {
        this.paramList = moreInfo;
    }

}
