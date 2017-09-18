package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ThrottlePolicyDTO
 */
public class ThrottlePolicyDTO   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("policyName")
  private String policyName = null;

  @JsonProperty("displayName")
  private String displayName = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("isDeployed")
  private Boolean isDeployed = false;

  @JsonProperty("type")
  private String type = null;

  public ThrottlePolicyDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ThrottlePolicyDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

   /**
   * Get policyName
   * @return policyName
  **/
  @ApiModelProperty(required = true, value = "")
  public String getPolicyName() {
    return policyName;
  }

  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  public ThrottlePolicyDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

   /**
   * Get displayName
   * @return displayName
  **/
  @ApiModelProperty(value = "")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public ThrottlePolicyDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ThrottlePolicyDTO isDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
    return this;
  }

   /**
   * Get isDeployed
   * @return isDeployed
  **/
  @ApiModelProperty(value = "")
  public Boolean getIsDeployed() {
    return isDeployed;
  }

  public void setIsDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
  }

  public ThrottlePolicyDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * ApplicationThrottlePolicy, SubscriptionThrottlePolicy and AdvancedThrottlePolicy are the supported values. 
   * @return type
  **/
  @ApiModelProperty(required = true, value = "ApplicationThrottlePolicy, SubscriptionThrottlePolicy and AdvancedThrottlePolicy are the supported values. ")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottlePolicyDTO throttlePolicy = (ThrottlePolicyDTO) o;
    return Objects.equals(this.id, throttlePolicy.id) &&
        Objects.equals(this.policyName, throttlePolicy.policyName) &&
        Objects.equals(this.displayName, throttlePolicy.displayName) &&
        Objects.equals(this.description, throttlePolicy.description) &&
        Objects.equals(this.isDeployed, throttlePolicy.isDeployed) &&
        Objects.equals(this.type, throttlePolicy.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, policyName, displayName, description, isDeployed, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlePolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isDeployed: ").append(toIndentedString(isDeployed)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

