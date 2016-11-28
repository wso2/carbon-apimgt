package org.wso2.carbon.apimgt.rest.api.store.dto;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIEnvironmentURLsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIEndpointURLsDTO  {
  
  
  
  private APIEnvironmentURLsDTO environmentURLs = null;
  
  
  private String environmentName = null;
  
  
  private String environmentType = null;


  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for APIEndpointURLsDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a APIEndpointURLsDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("environmentURLs")
  public APIEnvironmentURLsDTO getEnvironmentURLs() {
    return environmentURLs;
  }
  public void setEnvironmentURLs(APIEnvironmentURLsDTO environmentURLs) {
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
