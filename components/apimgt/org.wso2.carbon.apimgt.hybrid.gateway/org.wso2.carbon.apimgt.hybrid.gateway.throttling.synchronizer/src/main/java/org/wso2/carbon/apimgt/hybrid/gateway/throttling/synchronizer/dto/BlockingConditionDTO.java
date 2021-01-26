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

/**
 * Blocking Conditions
 **/


@ApiModel(description = "Blocking Conditions")
public class BlockingConditionDTO  {

  private String conditionId = null;
  
  @NotNull
  private String conditionType = null;
  
  @NotNull
  private String conditionValue = null;

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditionId")
  public String getConditionId() {
    return conditionId;
  }
  public void setConditionId(String conditionId) {
    this.conditionId = conditionId;
  }
  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("conditionType")
  public String getConditionType() {
    return conditionType;
  }
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("conditionValue")
  public String getConditionValue() {
    return conditionValue;
  }
  public void setConditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("  conditionId: ").append(conditionId).append("\n");
    sb.append("  conditionType: ").append(conditionType).append("\n");
    sb.append("  conditionValue: ").append(conditionValue).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
