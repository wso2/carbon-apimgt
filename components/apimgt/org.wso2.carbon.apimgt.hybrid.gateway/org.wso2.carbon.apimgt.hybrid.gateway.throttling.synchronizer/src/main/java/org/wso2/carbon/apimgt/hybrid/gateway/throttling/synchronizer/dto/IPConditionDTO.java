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


@ApiModel(description = "")
public class IPConditionDTO extends ThrottleConditionDTO {

  public enum IpConditionTypeEnum {
     IPRange,  IPSpecific, 
  };
  
  private IpConditionTypeEnum ipConditionType = null;

  private String specificIP = null;

  private String startingIP = null;

  private String endingIP = null;

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("ipConditionType")
  public IpConditionTypeEnum getIpConditionType() {
    return ipConditionType;
  }
  public void setIpConditionType(IpConditionTypeEnum ipConditionType) {
    this.ipConditionType = ipConditionType;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("specificIP")
  public String getSpecificIP() {
    return specificIP;
  }
  public void setSpecificIP(String specificIP) {
    this.specificIP = specificIP;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("startingIP")
  public String getStartingIP() {
    return startingIP;
  }
  public void setStartingIP(String startingIP) {
    this.startingIP = startingIP;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endingIP")
  public String getEndingIP() {
    return endingIP;
  }
  public void setEndingIP(String endingIP) {
    this.endingIP = endingIP;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPConditionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  ipConditionType: ").append(ipConditionType).append("\n");
    sb.append("  specificIP: ").append(specificIP).append("\n");
    sb.append("  startingIP: ").append(startingIP).append("\n");
    sb.append("  endingIP: ").append(endingIP).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
