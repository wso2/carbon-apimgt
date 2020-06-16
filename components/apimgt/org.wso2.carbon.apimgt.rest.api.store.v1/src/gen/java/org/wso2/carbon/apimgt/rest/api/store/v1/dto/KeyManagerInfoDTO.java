package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerApplicationConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class KeyManagerInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String type = null;
    private String displayName = null;
    private String description = null;
    private Boolean enabled = null;
    private List<String> availableGrantTypes = new ArrayList<>();
    private String tokenEndpoint = null;
    private String revokeEndpoint = null;
    private String userInfoEndpoint = null;
    private Boolean enableTokenGeneration = null;
    private Boolean enableTokenEncryption = false;
    private Boolean enableTokenHashing = false;
    private Boolean enableOAuthAppCreation = true;
    private Boolean enableMapOAuthConsumerApps = false;
    private List<KeyManagerApplicationConfigurationDTO> applicationConfiguration = new ArrayList<>();
    private List<Object> additionalProperties = new ArrayList<>();

  /**
   **/
  public KeyManagerInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public KeyManagerInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "WSO2 IS", required = true, value = "")
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
  public KeyManagerInfoDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "IS", required = true, value = "")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * display name of Keymanager 
   **/
  public KeyManagerInfoDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Keymanager1", value = "display name of Keymanager ")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public KeyManagerInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a key manager for Developers", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public KeyManagerInfoDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public KeyManagerInfoDTO availableGrantTypes(List<String> availableGrantTypes) {
    this.availableGrantTypes = availableGrantTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("availableGrantTypes")
  public List<String> getAvailableGrantTypes() {
    return availableGrantTypes;
  }
  public void setAvailableGrantTypes(List<String> availableGrantTypes) {
    this.availableGrantTypes = availableGrantTypes;
  }

  /**
   **/
  public KeyManagerInfoDTO tokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tokenEndpoint")
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }
  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  /**
   **/
  public KeyManagerInfoDTO revokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("revokeEndpoint")
  public String getRevokeEndpoint() {
    return revokeEndpoint;
  }
  public void setRevokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
  }

  /**
   **/
  public KeyManagerInfoDTO userInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("userInfoEndpoint")
  public String getUserInfoEndpoint() {
    return userInfoEndpoint;
  }
  public void setUserInfoEndpoint(String userInfoEndpoint) {
    this.userInfoEndpoint = userInfoEndpoint;
  }

  /**
   **/
  public KeyManagerInfoDTO enableTokenGeneration(Boolean enableTokenGeneration) {
    this.enableTokenGeneration = enableTokenGeneration;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enableTokenGeneration")
  public Boolean isEnableTokenGeneration() {
    return enableTokenGeneration;
  }
  public void setEnableTokenGeneration(Boolean enableTokenGeneration) {
    this.enableTokenGeneration = enableTokenGeneration;
  }

  /**
   **/
  public KeyManagerInfoDTO enableTokenEncryption(Boolean enableTokenEncryption) {
    this.enableTokenEncryption = enableTokenEncryption;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableTokenEncryption")
  public Boolean isEnableTokenEncryption() {
    return enableTokenEncryption;
  }
  public void setEnableTokenEncryption(Boolean enableTokenEncryption) {
    this.enableTokenEncryption = enableTokenEncryption;
  }

  /**
   **/
  public KeyManagerInfoDTO enableTokenHashing(Boolean enableTokenHashing) {
    this.enableTokenHashing = enableTokenHashing;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableTokenHashing")
  public Boolean isEnableTokenHashing() {
    return enableTokenHashing;
  }
  public void setEnableTokenHashing(Boolean enableTokenHashing) {
    this.enableTokenHashing = enableTokenHashing;
  }

  /**
   **/
  public KeyManagerInfoDTO enableOAuthAppCreation(Boolean enableOAuthAppCreation) {
    this.enableOAuthAppCreation = enableOAuthAppCreation;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("enableOAuthAppCreation")
  public Boolean isEnableOAuthAppCreation() {
    return enableOAuthAppCreation;
  }
  public void setEnableOAuthAppCreation(Boolean enableOAuthAppCreation) {
    this.enableOAuthAppCreation = enableOAuthAppCreation;
  }

  /**
   **/
  public KeyManagerInfoDTO enableMapOAuthConsumerApps(Boolean enableMapOAuthConsumerApps) {
    this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("enableMapOAuthConsumerApps")
  public Boolean isEnableMapOAuthConsumerApps() {
    return enableMapOAuthConsumerApps;
  }
  public void setEnableMapOAuthConsumerApps(Boolean enableMapOAuthConsumerApps) {
    this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
  }

  /**
   **/
  public KeyManagerInfoDTO applicationConfiguration(List<KeyManagerApplicationConfigurationDTO> applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationConfiguration")
  public List<KeyManagerApplicationConfigurationDTO> getApplicationConfiguration() {
    return applicationConfiguration;
  }
  public void setApplicationConfiguration(List<KeyManagerApplicationConfigurationDTO> applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
  }

  /**
   **/
  public KeyManagerInfoDTO additionalProperties(List<Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("additionalProperties")
  public List<Object> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerInfoDTO keyManagerInfo = (KeyManagerInfoDTO) o;
    return Objects.equals(id, keyManagerInfo.id) &&
        Objects.equals(name, keyManagerInfo.name) &&
        Objects.equals(type, keyManagerInfo.type) &&
        Objects.equals(displayName, keyManagerInfo.displayName) &&
        Objects.equals(description, keyManagerInfo.description) &&
        Objects.equals(enabled, keyManagerInfo.enabled) &&
        Objects.equals(availableGrantTypes, keyManagerInfo.availableGrantTypes) &&
        Objects.equals(tokenEndpoint, keyManagerInfo.tokenEndpoint) &&
        Objects.equals(revokeEndpoint, keyManagerInfo.revokeEndpoint) &&
        Objects.equals(userInfoEndpoint, keyManagerInfo.userInfoEndpoint) &&
        Objects.equals(enableTokenGeneration, keyManagerInfo.enableTokenGeneration) &&
        Objects.equals(enableTokenEncryption, keyManagerInfo.enableTokenEncryption) &&
        Objects.equals(enableTokenHashing, keyManagerInfo.enableTokenHashing) &&
        Objects.equals(enableOAuthAppCreation, keyManagerInfo.enableOAuthAppCreation) &&
        Objects.equals(enableMapOAuthConsumerApps, keyManagerInfo.enableMapOAuthConsumerApps) &&
        Objects.equals(applicationConfiguration, keyManagerInfo.applicationConfiguration) &&
        Objects.equals(additionalProperties, keyManagerInfo.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, displayName, description, enabled, availableGrantTypes, tokenEndpoint, revokeEndpoint, userInfoEndpoint, enableTokenGeneration, enableTokenEncryption, enableTokenHashing, enableOAuthAppCreation, enableMapOAuthConsumerApps, applicationConfiguration, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    availableGrantTypes: ").append(toIndentedString(availableGrantTypes)).append("\n");
    sb.append("    tokenEndpoint: ").append(toIndentedString(tokenEndpoint)).append("\n");
    sb.append("    revokeEndpoint: ").append(toIndentedString(revokeEndpoint)).append("\n");
    sb.append("    userInfoEndpoint: ").append(toIndentedString(userInfoEndpoint)).append("\n");
    sb.append("    enableTokenGeneration: ").append(toIndentedString(enableTokenGeneration)).append("\n");
    sb.append("    enableTokenEncryption: ").append(toIndentedString(enableTokenEncryption)).append("\n");
    sb.append("    enableTokenHashing: ").append(toIndentedString(enableTokenHashing)).append("\n");
    sb.append("    enableOAuthAppCreation: ").append(toIndentedString(enableOAuthAppCreation)).append("\n");
    sb.append("    enableMapOAuthConsumerApps: ").append(toIndentedString(enableMapOAuthConsumerApps)).append("\n");
    sb.append("    applicationConfiguration: ").append(toIndentedString(applicationConfiguration)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

