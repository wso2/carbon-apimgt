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
public class BandwidthLimitDTO extends ThrottleLimitDTO {

  private Long dataAmount = 0l;

  private String dataUnit = null;

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("dataAmount")
  public Long getDataAmount() {
    return dataAmount;
  }
  public void setDataAmount(Long dataAmount) {
    this.dataAmount = dataAmount;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("dataUnit")
  public String getDataUnit() {
    return dataUnit;
  }
  public void setDataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BandwidthLimitDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  dataAmount: ").append(dataAmount).append("\n");
    sb.append("  dataUnit: ").append(dataUnit).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
