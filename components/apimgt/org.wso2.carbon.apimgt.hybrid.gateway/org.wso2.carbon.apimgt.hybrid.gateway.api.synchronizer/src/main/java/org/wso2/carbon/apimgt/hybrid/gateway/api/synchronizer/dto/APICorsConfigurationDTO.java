/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;


/**
 * CORS configuration for the API\n
 **/


@ApiModel(description = "CORS configuration for the API\n")
public class APICorsConfigurationDTO  {
  
  
  
  private List<String> accessControlAllowOrigins = new ArrayList<String>();
  
  
  private Boolean accessControlAllowCredentials = false;
  
  
  private Boolean corsConfigurationEnabled = false;
  
  
  private List<String> accessControlAllowHeaders = new ArrayList<String>();
  
  
  private List<String> accessControlAllowMethods = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowOrigins")
  public List<String> getAccessControlAllowOrigins() {
    return accessControlAllowOrigins;
  }
  public void setAccessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowCredentials")
  public Boolean getAccessControlAllowCredentials() {
    return accessControlAllowCredentials;
  }
  public void setAccessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("corsConfigurationEnabled")
  public Boolean getCorsConfigurationEnabled() {
    return corsConfigurationEnabled;
  }
  public void setCorsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowHeaders")
  public List<String> getAccessControlAllowHeaders() {
    return accessControlAllowHeaders;
  }
  public void setAccessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowMethods")
  public List<String> getAccessControlAllowMethods() {
    return accessControlAllowMethods;
  }
  public void setAccessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICorsConfigurationDTO {\n");
    
    sb.append("  accessControlAllowOrigins: ").append(accessControlAllowOrigins).append("\n");
    sb.append("  accessControlAllowCredentials: ").append(accessControlAllowCredentials).append("\n");
    sb.append("  corsConfigurationEnabled: ").append(corsConfigurationEnabled).append("\n");
    sb.append("  accessControlAllowHeaders: ").append(accessControlAllowHeaders).append("\n");
    sb.append("  accessControlAllowMethods: ").append(accessControlAllowMethods).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
