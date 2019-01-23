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


@ApiModel(description = "")
public class APIBusinessInformationDTO  {
  
  
  
  private String technicalOwnerEmail = null;
  
  
  private String businessOwnerEmail = null;
  
  
  private String businessOwner = null;
  
  
  private String technicalOwner = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("technicalOwnerEmail")
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwnerEmail")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessOwner")
  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("technicalOwner")
  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIBusinessInformationDTO {\n");
    
    sb.append("  technicalOwnerEmail: ").append(technicalOwnerEmail).append("\n");
    sb.append("  businessOwnerEmail: ").append(businessOwnerEmail).append("\n");
    sb.append("  businessOwner: ").append(businessOwner).append("\n");
    sb.append("  technicalOwner: ").append(technicalOwner).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
