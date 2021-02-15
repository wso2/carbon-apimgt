package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertConfigDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AlertDTO   {
  
    private Integer id = null;
    private String name = null;
    private List<AlertConfigDTO> _configuration = new ArrayList<AlertConfigDTO>();

  /**
   * The alert Id
   **/
  public AlertDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "The alert Id")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * The name of the alert.
   **/
  public AlertDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "AbnormalRequestsPerMin", value = "The name of the alert.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AlertDTO _configuration(List<AlertConfigDTO> _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("configuration")
  public List<AlertConfigDTO> getConfiguration() {
    return _configuration;
  }
  public void setConfiguration(List<AlertConfigDTO> _configuration) {
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
    AlertDTO alert = (AlertDTO) o;
    return Objects.equals(id, alert.id) &&
        Objects.equals(name, alert.name) &&
        Objects.equals(_configuration, alert._configuration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, _configuration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AlertDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

