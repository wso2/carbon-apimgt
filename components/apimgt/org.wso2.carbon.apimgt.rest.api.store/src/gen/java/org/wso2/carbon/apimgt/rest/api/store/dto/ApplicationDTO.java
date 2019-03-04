package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;

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
  private String throttlingTier = null;
  
  
  private String callbackUrl = null;
  
  
  private String description = null;
  
  public enum TokenTypeEnum {
     OAUTH,  JWT, 
  };
  
  private TokenTypeEnum tokenType = TokenTypeEnum.OAUTH;
  
  
  private String status = "";
  
  
  private String groupId = null;
  
  
  private String owner = null;
  
  private List<ApplicationKeyDTO> keys = new ArrayList<ApplicationKeyDTO>();
  
  
  private Object attributes = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for ApplicationDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a ApplicationDTO
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
  @JsonProperty("throttlingTier")
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
  }
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
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
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
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
  public Object getAttributes() {
    return attributes;
  }
  public void setAttributes(Object attributes) {
    this.attributes = attributes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  callbackUrl: ").append(callbackUrl).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  tokenType: ").append(tokenType).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  groupId: ").append(groupId).append("\n");
    sb.append("  owner: ").append(owner).append("\n");
    sb.append("  keys: ").append(keys).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
