package org.wso2.carbon.apimgt.rest.api.store.dto;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIEndpointURLsEnvironmentURLsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class APIEndpointURLsDTO  {
  
  
  
  private APIEndpointURLsEnvironmentURLsDTO environmentURLs = null;
  
  
  private String environmentName = null;
  
  
  private String environmentType = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("environmentURLs")
  public APIEndpointURLsEnvironmentURLsDTO getEnvironmentURLs() {
    return environmentURLs;
  }
  public void setEnvironmentURLs(APIEndpointURLsEnvironmentURLsDTO environmentURLs) {
    this.environmentURLs = environmentURLs;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("environmentName")
  public String getEnvironmentName() {
    return environmentName;
  }
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("environmentType")
  public String getEnvironmentType() {
    return environmentType;
  }
  public void setEnvironmentType(String environmentType) {
    this.environmentType = environmentType;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIEndpointURLsDTO {\n");
    
    sb.append("  environmentURLs: ").append(environmentURLs).append("\n");
    sb.append("  environmentName: ").append(environmentName).append("\n");
    sb.append("  environmentType: ").append(environmentType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
