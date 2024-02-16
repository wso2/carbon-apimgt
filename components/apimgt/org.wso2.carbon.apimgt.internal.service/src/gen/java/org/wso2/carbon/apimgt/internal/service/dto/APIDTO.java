package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.OperationPolicyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.URLMappingDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APIDTO   {
  
    private String uuid = null;
    private Integer apiId = null;
    private String provider = null;
    private String name = null;
    private String version = null;
    private String context = null;
    private String policy = null;
    private String apiType = null;
    private String status = null;
    private String organization = null;
    private Boolean isDefaultVersion = null;
    private List<OperationPolicyDTO> apiPolicies = new ArrayList<>();
    private List<URLMappingDTO> urlMappings = new ArrayList<>();
    private String securityScheme = null;

  /**
   * UUID of API
   **/
  public APIDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of API")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   **/
  public APIDTO apiId(Integer apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public Integer getApiId() {
    return apiId;
  }
  public void setApiId(Integer apiId) {
    this.apiId = apiId;
  }

  /**
   * API Provider name.
   **/
  public APIDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(value = "API Provider name.")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   * Name of the API
   **/
  public APIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the API")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Version of the API.
   **/
  public APIDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "Version of the API.")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Context of the API.
   **/
  public APIDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(value = "Context of the API.")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * API level throttling policy.
   **/
  public APIDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(value = "API level throttling policy.")
  @JsonProperty("policy")
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   * Type of the API.
   **/
  public APIDTO apiType(String apiType) {
    this.apiType = apiType;
    return this;
  }

  
  @ApiModelProperty(example = "APIProduct", value = "Type of the API.")
  @JsonProperty("apiType")
  public String getApiType() {
    return apiType;
  }
  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

  /**
   * Type of the API.
   **/
  public APIDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "APIProduct", value = "Type of the API.")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Organization of the API.
   **/
  public APIDTO organization(String organization) {
    this.organization = organization;
    return this;
  }

  
  @ApiModelProperty(example = "wso2.com", value = "Organization of the API.")
  @JsonProperty("organization")
  public String getOrganization() {
    return organization;
  }
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /**
   * Whether this is the default version of the API.
   **/
  public APIDTO isDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Whether this is the default version of the API.")
  @JsonProperty("isDefaultVersion")
  public Boolean isIsDefaultVersion() {
    return isDefaultVersion;
  }
  public void setIsDefaultVersion(Boolean isDefaultVersion) {
    this.isDefaultVersion = isDefaultVersion;
  }

  /**
   **/
  public APIDTO apiPolicies(List<OperationPolicyDTO> apiPolicies) {
    this.apiPolicies = apiPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiPolicies")
  public List<OperationPolicyDTO> getApiPolicies() {
    return apiPolicies;
  }
  public void setApiPolicies(List<OperationPolicyDTO> apiPolicies) {
    this.apiPolicies = apiPolicies;
  }

  /**
   **/
  public APIDTO urlMappings(List<URLMappingDTO> urlMappings) {
    this.urlMappings = urlMappings;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("urlMappings")
  public List<URLMappingDTO> getUrlMappings() {
    return urlMappings;
  }
  public void setUrlMappings(List<URLMappingDTO> urlMappings) {
    this.urlMappings = urlMappings;
  }

  /**
   * Available authentication methods of the API.
   **/
  public APIDTO securityScheme(String securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(example = "Oauth2,api_key", value = "Available authentication methods of the API.")
  @JsonProperty("securityScheme")
  public String getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(String securityScheme) {
    this.securityScheme = securityScheme;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDTO API = (APIDTO) o;
    return Objects.equals(uuid, API.uuid) &&
        Objects.equals(apiId, API.apiId) &&
        Objects.equals(provider, API.provider) &&
        Objects.equals(name, API.name) &&
        Objects.equals(version, API.version) &&
        Objects.equals(context, API.context) &&
        Objects.equals(policy, API.policy) &&
        Objects.equals(apiType, API.apiType) &&
        Objects.equals(status, API.status) &&
        Objects.equals(organization, API.organization) &&
        Objects.equals(isDefaultVersion, API.isDefaultVersion) &&
        Objects.equals(apiPolicies, API.apiPolicies) &&
        Objects.equals(urlMappings, API.urlMappings) &&
        Objects.equals(securityScheme, API.securityScheme);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, apiId, provider, name, version, context, policy, apiType, status, organization, isDefaultVersion, apiPolicies, urlMappings, securityScheme);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    apiType: ").append(toIndentedString(apiType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
    sb.append("    isDefaultVersion: ").append(toIndentedString(isDefaultVersion)).append("\n");
    sb.append("    apiPolicies: ").append(toIndentedString(apiPolicies)).append("\n");
    sb.append("    urlMappings: ").append(toIndentedString(urlMappings)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
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

