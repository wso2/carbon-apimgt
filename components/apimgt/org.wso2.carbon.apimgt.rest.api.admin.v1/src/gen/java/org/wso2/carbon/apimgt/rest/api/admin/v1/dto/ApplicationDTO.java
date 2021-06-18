package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationDTO   {
  
    private String applicationId = null;
    private String name = null;
    private String throttlingPolicy = null;
    private String description = null;

@XmlType(name="TokenTypeEnum")
@XmlEnum(String.class)
public enum TokenTypeEnum {

    @XmlEnumValue("OAUTH") OAUTH(String.valueOf("OAUTH")), @XmlEnumValue("JWT") JWT(String.valueOf("JWT"));


    private String value;

    TokenTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TokenTypeEnum fromValue(String v) {
        for (TokenTypeEnum b : TokenTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private TokenTypeEnum tokenType = TokenTypeEnum.JWT;
    private String status = "";
    private List<String> groups = new ArrayList<>();
    private Integer subscriptionCount = null;
    private Map<String, String> attributes = new HashMap<>();
    private List<ScopeInfoDTO> subscriptionScopes = new ArrayList<>();
    private String owner = null;

  /**
   **/
  public ApplicationDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public ApplicationDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "CalculatorApp", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApplicationDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  @JsonProperty("throttlingPolicy")
  @NotNull
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   **/
  public ApplicationDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Sample calculator application", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Type of the access token generated for this application. **OAUTH:** A UUID based access token which is issued by default. **JWT:** A self-contained, signed JWT based access token. **Note:** This can be only used in Microgateway environments. 
   **/
  public ApplicationDTO tokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  
  @ApiModelProperty(example = "JWT", value = "Type of the access token generated for this application. **OAUTH:** A UUID based access token which is issued by default. **JWT:** A self-contained, signed JWT based access token. **Note:** This can be only used in Microgateway environments. ")
  @JsonProperty("tokenType")
  public TokenTypeEnum getTokenType() {
    return tokenType;
  }
  public void setTokenType(TokenTypeEnum tokenType) {
    this.tokenType = tokenType;
  }

  /**
   **/
  public ApplicationDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public ApplicationDTO groups(List<String> groups) {
    this.groups = groups;
    return this;
  }

  
  @ApiModelProperty(example = "\"\"", value = "")
  @JsonProperty("groups")
  public List<String> getGroups() {
    return groups;
  }
  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  /**
   **/
  public ApplicationDTO subscriptionCount(Integer subscriptionCount) {
    this.subscriptionCount = subscriptionCount;
    return this;
  }

  
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
  public ApplicationDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(example = "\"External Reference ID, Billing Tier\"", value = "")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   **/
  public ApplicationDTO subscriptionScopes(List<ScopeInfoDTO> subscriptionScopes) {
    this.subscriptionScopes = subscriptionScopes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
  @JsonProperty("subscriptionScopes")
  public List<ScopeInfoDTO> getSubscriptionScopes() {
    return subscriptionScopes;
  }
  public void setSubscriptionScopes(List<ScopeInfoDTO> subscriptionScopes) {
    this.subscriptionScopes = subscriptionScopes;
  }

  /**
   * Application created user 
   **/
  public ApplicationDTO owner(String owner) {
    this.owner = owner;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "Application created user ")
  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationDTO application = (ApplicationDTO) o;
    return Objects.equals(applicationId, application.applicationId) &&
        Objects.equals(name, application.name) &&
        Objects.equals(throttlingPolicy, application.throttlingPolicy) &&
        Objects.equals(description, application.description) &&
        Objects.equals(tokenType, application.tokenType) &&
        Objects.equals(status, application.status) &&
        Objects.equals(groups, application.groups) &&
        Objects.equals(subscriptionCount, application.subscriptionCount) &&
        Objects.equals(attributes, application.attributes) &&
        Objects.equals(subscriptionScopes, application.subscriptionScopes) &&
        Objects.equals(owner, application.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, throttlingPolicy, description, tokenType, status, groups, subscriptionCount, attributes, subscriptionScopes, owner);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    groups: ").append(toIndentedString(groups)).append("\n");
    sb.append("    subscriptionCount: ").append(toIndentedString(subscriptionCount)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    subscriptionScopes: ").append(toIndentedString(subscriptionScopes)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
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

