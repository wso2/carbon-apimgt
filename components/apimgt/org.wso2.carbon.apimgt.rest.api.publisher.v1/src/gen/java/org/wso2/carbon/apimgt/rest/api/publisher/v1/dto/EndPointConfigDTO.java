package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointConfigAttributesDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EndPointConfigDTO  {
  
  
  
  private String url = null;
  
  
  private String timeout = null;
  
  
  private Boolean isPrimary = null;
  
  
  private List<EndPointConfigAttributesDTO> attributes = new ArrayList<EndPointConfigAttributesDTO>();

  
  /**
   * Service url of the endpoint\n
   **/
  @ApiModelProperty(value = "Service url of the endpoint\n")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  
  /**
   * Time out of the endpoint\n
   **/
  @ApiModelProperty(value = "Time out of the endpoint\n")
  @JsonProperty("timeout")
  public String getTimeout() {
    return timeout;
  }
  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  
  /**
   * Defines whether the endpoint is primary when used in fail over.\n
   **/
  @ApiModelProperty(value = "Defines whether the endpoint is primary when used in fail over.\n")
  @JsonProperty("isPrimary")
  public Boolean getIsPrimary() {
    return isPrimary;
  }
  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public List<EndPointConfigAttributesDTO> getAttributes() {
    return attributes;
  }
  public void setAttributes(List<EndPointConfigAttributesDTO> attributes) {
    this.attributes = attributes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointConfigDTO {\n");
    
    sb.append("  url: ").append(url).append("\n");
    sb.append("  timeout: ").append(timeout).append("\n");
    sb.append("  isPrimary: ").append(isPrimary).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
