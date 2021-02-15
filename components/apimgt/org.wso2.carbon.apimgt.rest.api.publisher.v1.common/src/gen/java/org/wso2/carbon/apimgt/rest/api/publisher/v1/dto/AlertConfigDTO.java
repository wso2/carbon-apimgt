package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertConfigDTO   {
  
    private String configurationId = null;
    private Map<String, String> _configuration = new HashMap<String, String>();

  /**
   * The alert config subscription id.
   **/
  public AlertConfigDTO configurationId(String configurationId) {
    this.configurationId = configurationId;
    return this;
  }

  
  @ApiModelProperty(example = "UGl6emFTaGFja0FQSSsxLjAuMCtEZWZhdWx0QXBwbGljYXRpb24K", value = "The alert config subscription id.")
  @JsonProperty("configurationId")
  public String getConfigurationId() {
    return configurationId;
  }
  public void setConfigurationId(String configurationId) {
    this.configurationId = configurationId;
  }

  /**
   * The config parameters.
   **/
  public AlertConfigDTO _configuration(Map<String, String> _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(example = "{\"apiName\":\"PizzaShackAPI\",\"apiVersion\":\"1.0.0\",\"applicationName\":\"DefaultApplication\",\"requestConunt\":\"12\"}", value = "The config parameters.")
  @JsonProperty("configuration")
  public Map<String, String> getConfiguration() {
    return _configuration;
  }
  public void setConfiguration(Map<String, String> _configuration) {
    this._configuration = _configuration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertConfigDTO alertConfig = (AlertConfigDTO) o;
    return Objects.equals(configurationId, alertConfig.configurationId) &&
        Objects.equals(_configuration, alertConfig._configuration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(configurationId, _configuration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertConfigDTO {\n");
    
    sb.append("    configurationId: ").append(toIndentedString(configurationId)).append("\n");
    sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
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

