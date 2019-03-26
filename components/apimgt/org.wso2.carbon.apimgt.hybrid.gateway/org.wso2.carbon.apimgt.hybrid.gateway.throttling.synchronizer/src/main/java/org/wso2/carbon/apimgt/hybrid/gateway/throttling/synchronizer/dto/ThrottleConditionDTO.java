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


/**
 * Throttling Conditions
 **/

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HeaderConditionDTO.class, name = "HeaderCondition"),
    @JsonSubTypes.Type(value = IPConditionDTO.class, name = "IPCondition"),
    @JsonSubTypes.Type(value = JWTClaimsConditionDTO.class, name = "JWTClaimsCondition"),
    @JsonSubTypes.Type(value = QueryParameterConditionDTO.class, name = "QueryParameterCondition")
})
@ApiModel(description = "Throttling Conditions")
public class ThrottleConditionDTO  {

  public enum TypeEnum {
     HeaderCondition,  IPCondition,  JWTClaimsCondition,  QueryParameterCondition, 
  };
  @NotNull
  private TypeEnum type = null;

  private Boolean invertCondition = false;

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
  @ApiModelProperty(value = "")
  @JsonProperty("invertCondition")
  public Boolean getInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  invertCondition: ").append(invertCondition).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
