package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class RemotePlanLookupRequestDTO   {
  
    private String environmentId = null;
    private EnvironmentDTO environment = null;

  /**
   * Persisted environment UUID.
   **/
  public RemotePlanLookupRequestDTO environmentId(String environmentId) {
    this.environmentId = environmentId;
    return this;
  }

  
  @ApiModelProperty(example = "8d263942-a6df-4cc2-a804-7a2525501450", value = "Persisted environment UUID.")
  @JsonProperty("environmentId")
  public String getEnvironmentId() {
    return environmentId;
  }
  public void setEnvironmentId(String environmentId) {
    this.environmentId = environmentId;
  }

  /**
   **/
  public RemotePlanLookupRequestDTO environment(EnvironmentDTO environment) {
    this.environment = environment;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("environment")
  public EnvironmentDTO getEnvironment() {
    return environment;
  }
  public void setEnvironment(EnvironmentDTO environment) {
    this.environment = environment;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RemotePlanLookupRequestDTO remotePlanLookupRequest = (RemotePlanLookupRequestDTO) o;
    return Objects.equals(environmentId, remotePlanLookupRequest.environmentId) &&
        Objects.equals(environment, remotePlanLookupRequest.environment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environmentId, environment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RemotePlanLookupRequestDTO {\n");
    
    sb.append("    environmentId: ").append(toIndentedString(environmentId)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
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

