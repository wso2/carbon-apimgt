package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GlobalPolicyAllOfDTO   {
  
    private String siddhiQuery = null;
    private String keyTemplate = null;

  /**
   **/
  public GlobalPolicyAllOfDTO siddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("siddhiQuery")
  public String getSiddhiQuery() {
    return siddhiQuery;
  }
  public void setSiddhiQuery(String siddhiQuery) {
    this.siddhiQuery = siddhiQuery;
  }

  /**
   **/
  public GlobalPolicyAllOfDTO keyTemplate(String keyTemplate) {
    this.keyTemplate = keyTemplate;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyTemplate")
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
    GlobalPolicyAllOfDTO globalPolicyAllOf = (GlobalPolicyAllOfDTO) o;
    return Objects.equals(siddhiQuery, globalPolicyAllOf.siddhiQuery) &&
        Objects.equals(keyTemplate, globalPolicyAllOf.keyTemplate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siddhiQuery, keyTemplate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GlobalPolicyAllOfDTO {\n");
    
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

