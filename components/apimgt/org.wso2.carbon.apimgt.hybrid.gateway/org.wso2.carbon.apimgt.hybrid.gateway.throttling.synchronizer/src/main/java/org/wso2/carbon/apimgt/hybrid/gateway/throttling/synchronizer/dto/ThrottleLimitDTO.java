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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RequestCountLimitDTO.class, name = "RequestCountLimit"),
    @JsonSubTypes.Type(value = BandwidthLimitDTO.class, name = "BandwidthLimit")
})
@ApiModel(description = "")
public class ThrottleLimitDTO  {

  public enum TypeEnum {
     RequestCountLimit,  BandwidthLimit, 
  };
  @NotNull
  private TypeEnum type = null;
  
  @NotNull
  private String timeUnit = null;
  
  @NotNull
  private Integer unitTime = null;
  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("timeUnit")
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("unitTime")
  public Integer getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Integer unitTime) {
    this.unitTime = unitTime;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleLimitDTO {\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  timeUnit: ").append(timeUnit).append("\n");
    sb.append("  unitTime: ").append(unitTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
