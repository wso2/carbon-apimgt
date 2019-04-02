package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.BaseAPIDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIDTO extends BaseAPIDTO {
  
  
  
  private String lifeCycleStatus = null;
  
  
  private Boolean isDefaultVersion = null;
  
  
  private List<String> tags = new ArrayList<String>();
  
  
  private List<String> policies = new ArrayList<String>();
  
  
  private String wsdlUri = null;
  
  
  private Object businessInformation = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isDefaultVersion")
  public Boolean getIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("wsdlUri")
  public String getWsdlUri() {
    return wsdlUri;
  }
  public void setWsdlUri(String wsdlUri) {
    this.wsdlUri = wsdlUri;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("businessInformation")
  public Object getBusinessInformation() {
    return businessInformation;
  }
  public void setBusinessInformation(Object businessInformation) {
    this.businessInformation = businessInformation;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("  isDefaultVersion: ").append(isDefaultVersion).append("\n");
    sb.append("  tags: ").append(tags).append("\n");
    sb.append("  policies: ").append(policies).append("\n");
    sb.append("  wsdlUri: ").append(wsdlUri).append("\n");
    sb.append("  businessInformation: ").append(businessInformation).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
