package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SettingsDTO  {
  
  
  
  private String tokenUrl = null;
  
  
  private List<EnvironmentDTO> environment = new ArrayList<EnvironmentDTO>();
  
  
  private List<String> scopes = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tokenUrl")
  public String getTokenUrl() {
    return tokenUrl;
  }
  public void setTokenUrl(String tokenUrl) {
    this.tokenUrl = tokenUrl;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("environment")
  public List<EnvironmentDTO> getEnvironment() {
    return environment;
  }
  public void setEnvironment(List<EnvironmentDTO> environment) {
    this.environment = environment;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("  tokenUrl: ").append(tokenUrl).append("\n");
    sb.append("  environment: ").append(environment).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
