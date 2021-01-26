/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "")
public class ErrorDTO  {

  @NotNull
  private Long code = null;
  
  @NotNull
  private String message = null;

  private String description = null;

  private String moreInfo = null;

  private List<ErrorListItemDTO> error = new ArrayList<ErrorListItemDTO>();

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("code")
  public Long getCode() {
    return code;
  }
  public void setCode(Long code) {
    this.code = code;
  }

  /**
   * Error message.
   **/
  @ApiModelProperty(required = true, value = "Error message.")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * A detail description about the error message.\n
   **/
  @ApiModelProperty(value = "A detail description about the error message.\n")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Preferably an url with more details about the error.\n
   **/
  @ApiModelProperty(value = "Preferably an url with more details about the error.\n")
  @JsonProperty("moreInfo")
  public String getMoreInfo() {
    return moreInfo;
  }
  public void setMoreInfo(String moreInfo) {
    this.moreInfo = moreInfo;
  }

  /**
   * If there are more than one error list them out.\nFor example, list out validation errors by each field.\n
   **/
  @ApiModelProperty(value = "If there are more than one error list them out.\nFor example, list out validation errors by each field.\n")
  @JsonProperty("error")
  public List<ErrorListItemDTO> getError() {
    return error;
  }
  public void setError(List<ErrorListItemDTO> error) {
    this.error = error;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorDTO {\n");
    
    sb.append("  code: ").append(code).append("\n");
    sb.append("  message: ").append(message).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  moreInfo: ").append(moreInfo).append("\n");
    sb.append("  error: ").append(error).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
