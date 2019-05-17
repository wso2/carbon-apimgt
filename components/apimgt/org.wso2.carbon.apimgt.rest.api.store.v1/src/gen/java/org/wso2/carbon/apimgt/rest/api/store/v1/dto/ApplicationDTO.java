package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationDTO  {
  
  
  
  private String applicationId = null;
  
  @NotNull
  private String name = null;
  
  
  private String subscriber = null;
  
  @NotNull
  private String throttlingPolicy = null;
  
  
  private String description = null;
  
  public enum TokenTypeEnum {
     OAUTH,  JWT, 
  };
  
  private TokenTypeEnum tokenType = TokenTypeEnum.OAUTH;
  
  
  private String status = "";
  
  
  private List<String> groups = new ArrayList<String>();
  
  
  private Integer subscriptionCount = null;
  
  
  private List<ApplicationKeyDTO> keys = new ArrayList<ApplicationKeyDTO>();
  
  
  private Map<String, String> attributes = new HashMap<String, String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * If subscriber is not given user invoking the API will be taken as the subscriber.\n
   **/
  @ApiModelProperty(value = "If subscriber is not given user invoking the API will be taken as the subscriber.\n")
  @JsonProperty("subscriber")
  public String getSubscriber() {
    return subscriber;
  }
  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * Type of the access token generated for this application.\n\n**OAUTH:** A UUID based access token which is issued by default.\n**JWT:** A self-contained, signed JWT based access token. **Note:** This can be only used in Microgateway environments.\n
   **/
  @ApiModelProperty(value = "Type of the access token generated for this application.\n\n**OAUTH:** A UUID based access token which is issued by default.\n**JWT:** A self-contained, signed JWT based access token. **Note:** This can be only used in Microgateway environments.\n")
  @JsonProperty("tokenType")
  public TokenTypeEnum getTokenType() {
    return tokenType;
  }
  public void setTokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("groups")
  public List<String> getGroups() {
    return groups;
  }
  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionCount")
  public Integer getSubscriptionCount() {
    return subscriptionCount;
  }
  public void setSubscriptionCount(Integer subscriptionCount) {
    this.subscriptionCount = subscriptionCount;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("keys")
  public List<ApplicationKeyDTO> getKeys() {
    return keys;
  }
  public void setKeys(List<ApplicationKeyDTO> keys) {
    this.keys = keys;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingPolicy: ").append(throttlingPolicy).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  tokenType: ").append(tokenType).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  groups: ").append(groups).append("\n");
    sb.append("  subscriptionCount: ").append(subscriptionCount).append("\n");
    sb.append("  keys: ").append(keys).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
