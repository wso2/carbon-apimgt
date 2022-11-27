package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CustomRuleDTO extends ThrottlePolicyDTO  {
  
    private String siddhiQuery = null;
    private String keyTemplate = null;

  /**
   * Siddhi query which represents the custom throttling policy
   **/
  public CustomRuleDTO siddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
    return this;
  }

  
  @ApiModelProperty(example = "FROM RequestStream\\nSELECT userId, ( userId == 'admin@carbon.super' ) AS isEligible , str:concat('admin@carbon.super','') as throttleKey\\nINSERT INTO EligibilityStream; \\n\\nFROM EligibilityStream[isEligible==true]#throttler:timeBatch(1 min) \\nSELECT throttleKey, (count(userId) >= 10) as isThrottled, expiryTimeStamp group by throttleKey \\nINSERT ALL EVENTS into ResultStream; ", required = true, value = "Siddhi query which represents the custom throttling policy")
  @JsonProperty("siddhiQuery")
  @NotNull
  public String getSiddhiQuery() {
    return siddhiQuery;
  }
  public void setSiddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
  }

  /**
   * The specific combination of attributes that are checked in the policy.
   **/
  public CustomRuleDTO keyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
    return this;
  }

  
  @ApiModelProperty(example = "$userId", required = true, value = "The specific combination of attributes that are checked in the policy.")
  @JsonProperty("keyTemplate")
  @NotNull
  public String getKeyTemplate() {
    return keyTemplate;
  }
  public void setKeyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CustomRuleDTO customRule = (CustomRuleDTO) o;
    return Objects.equals(siddhiQuery, customRule.siddhiQuery) &&
        Objects.equals(keyTemplate, customRule.keyTemplate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siddhiQuery, keyTemplate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CustomRuleDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    siddhiQuery: ").append(toIndentedString(siddhiQuery)).append("\n");
    sb.append("    keyTemplate: ").append(toIndentedString(keyTemplate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

