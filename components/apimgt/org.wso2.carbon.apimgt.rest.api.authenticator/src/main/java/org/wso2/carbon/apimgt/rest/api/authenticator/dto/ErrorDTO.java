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

package org.wso2.carbon.apimgt.rest.api.authenticator.dto;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;

/**
 * DTO class for Error
 */
public class ErrorDTO {

    @SerializedName("code")
    @ApiModelProperty(required = true, value = "")
    private long code;

    @ApiModelProperty(required = true, value = "Error message.")
    @SerializedName("message")
    private String message = null;

    @ApiModelProperty(value = "A detail description about the error message.")
    @SerializedName("description")
    private String description = null;

    @ApiModelProperty(value = "Preferably an url with more details about the error.")
    @SerializedName("moreInfo")
    private HashMap<String, String> paramList = null;


    /**
     * @return error code
     **/
    public Long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    /**
     * Error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * A detail description about the error message.
     *
     * @return detail of description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Preferably an url with more details about the error.
     *
     * @return more details on error
     */
    public HashMap<String, String> getMoreInfo() {
        return paramList;
    }

    public void setMoreInfo(HashMap<String, String> moreInfo) {
        this.paramList = moreInfo;
    }

}
