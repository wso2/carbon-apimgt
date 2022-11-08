package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SolaceTopicsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AdditionalSubscriptionInfoSolaceTopicsObjectDTO   {
  
    private SolaceTopicsDTO defaultSyntax = null;
    private SolaceTopicsDTO mqttSyntax = null;

  /**
   **/
  public AdditionalSubscriptionInfoSolaceTopicsObjectDTO defaultSyntax(SolaceTopicsDTO defaultSyntax) {
    this.defaultSyntax = defaultSyntax;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("defaultSyntax")
  public SolaceTopicsDTO getDefaultSyntax() {
    return defaultSyntax;
  }
  public void setDefaultSyntax(SolaceTopicsDTO defaultSyntax) {
    this.defaultSyntax = defaultSyntax;
  }

  /**
   **/
  public AdditionalSubscriptionInfoSolaceTopicsObjectDTO mqttSyntax(SolaceTopicsDTO mqttSyntax) {
    this.mqttSyntax = mqttSyntax;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("mqttSyntax")
  public SolaceTopicsDTO getMqttSyntax() {
    return mqttSyntax;
  }
  public void setMqttSyntax(SolaceTopicsDTO mqttSyntax) {
    this.mqttSyntax = mqttSyntax;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalSubscriptionInfoSolaceTopicsObjectDTO additionalSubscriptionInfoSolaceTopicsObject = (AdditionalSubscriptionInfoSolaceTopicsObjectDTO) o;
    return Objects.equals(defaultSyntax, additionalSubscriptionInfoSolaceTopicsObject.defaultSyntax) &&
        Objects.equals(mqttSyntax, additionalSubscriptionInfoSolaceTopicsObject.mqttSyntax);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultSyntax, mqttSyntax);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalSubscriptionInfoSolaceTopicsObjectDTO {\n");
    
    sb.append("    defaultSyntax: ").append(toIndentedString(defaultSyntax)).append("\n");
    sb.append("    mqttSyntax: ").append(toIndentedString(mqttSyntax)).append("\n");
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

