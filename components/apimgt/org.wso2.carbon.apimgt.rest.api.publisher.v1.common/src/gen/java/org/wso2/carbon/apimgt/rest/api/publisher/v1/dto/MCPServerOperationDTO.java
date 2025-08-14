package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationMappingDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendOperationMappingDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MCPServerOperationDTO   {
  
    private String id = null;
    private String target = null;

    @XmlType(name="FeatureEnum")
    @XmlEnum(String.class)
    public enum FeatureEnum {
        TOOL("TOOL");
        private String value;

        FeatureEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static FeatureEnum fromValue(String v) {
            for (FeatureEnum b : FeatureEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private FeatureEnum feature = null;
    private String authType = "Any";
    private String throttlingPolicy = null;
    private List<String> scopes = new ArrayList<String>();
    private String payloadSchema = null;
    private String uriMapping = null;
    private String schemaDefinition = null;
    private String description = null;
    private APIOperationPoliciesDTO operationPolicies = null;
    private BackendOperationMappingDTO backendOperationMapping = null;
    private APIOperationMappingDTO apiOperationMapping = null;

  /**
   **/
  public MCPServerOperationDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "postapiresource", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public MCPServerOperationDTO target(String target) {
    this.target = target;
    return this;
  }

  
  @ApiModelProperty(example = "listBooks", value = "")
  @JsonProperty("target")
  public String getTarget() {
    return target;
  }
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Operation type for MCP Server (e.g., TOOL)
   **/
  public MCPServerOperationDTO feature(FeatureEnum feature) {
    this.feature = feature;
    return this;
  }

  
  @ApiModelProperty(value = "Operation type for MCP Server (e.g., TOOL)")
  @JsonProperty("feature")
  public FeatureEnum getFeature() {
    return feature;
  }
  public void setFeature(FeatureEnum feature) {
    this.feature = feature;
  }

  /**
   **/
  public MCPServerOperationDTO authType(String authType) {
    this.authType = authType;
    return this;
  }

  
  @ApiModelProperty(example = "Application & Application User", value = "")
  @JsonProperty("authType")
  public String getAuthType() {
    return authType;
  }
  public void setAuthType(String authType) {
    this.authType = authType;
  }

  /**
   **/
  public MCPServerOperationDTO throttlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("throttlingPolicy")
  public String getThrottlingPolicy() {
    return throttlingPolicy;
  }
  public void setThrottlingPolicy(String throttlingPolicy) {
    this.throttlingPolicy = throttlingPolicy;
  }

  /**
   **/
  public MCPServerOperationDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public MCPServerOperationDTO payloadSchema(String payloadSchema) {
    this.payloadSchema = payloadSchema;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("payloadSchema")
  public String getPayloadSchema() {
    return payloadSchema;
  }
  public void setPayloadSchema(String payloadSchema) {
    this.payloadSchema = payloadSchema;
  }

  /**
   **/
  public MCPServerOperationDTO uriMapping(String uriMapping) {
    this.uriMapping = uriMapping;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("uriMapping")
  public String getUriMapping() {
    return uriMapping;
  }
  public void setUriMapping(String uriMapping) {
    this.uriMapping = uriMapping;
  }

  /**
   **/
  public MCPServerOperationDTO schemaDefinition(String schemaDefinition) {
    this.schemaDefinition = schemaDefinition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("schemaDefinition")
  public String getSchemaDefinition() {
    return schemaDefinition;
  }
  public void setSchemaDefinition(String schemaDefinition) {
    this.schemaDefinition = schemaDefinition;
  }

  /**
   **/
  public MCPServerOperationDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "this is an operation of get orderId", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public MCPServerOperationDTO operationPolicies(APIOperationPoliciesDTO operationPolicies) {
    this.operationPolicies = operationPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("operationPolicies")
  public APIOperationPoliciesDTO getOperationPolicies() {
    return operationPolicies;
  }
  public void setOperationPolicies(APIOperationPoliciesDTO operationPolicies) {
    this.operationPolicies = operationPolicies;
  }

  /**
   **/
  public MCPServerOperationDTO backendOperationMapping(BackendOperationMappingDTO backendOperationMapping) {
    this.backendOperationMapping = backendOperationMapping;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("backendOperationMapping")
  public BackendOperationMappingDTO getBackendOperationMapping() {
    return backendOperationMapping;
  }
  public void setBackendOperationMapping(BackendOperationMappingDTO backendOperationMapping) {
    this.backendOperationMapping = backendOperationMapping;
  }

  /**
   **/
  public MCPServerOperationDTO apiOperationMapping(APIOperationMappingDTO apiOperationMapping) {
    this.apiOperationMapping = apiOperationMapping;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiOperationMapping")
  public APIOperationMappingDTO getApiOperationMapping() {
    return apiOperationMapping;
  }
  public void setApiOperationMapping(APIOperationMappingDTO apiOperationMapping) {
    this.apiOperationMapping = apiOperationMapping;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerOperationDTO mcPServerOperation = (MCPServerOperationDTO) o;
    return Objects.equals(id, mcPServerOperation.id) &&
        Objects.equals(target, mcPServerOperation.target) &&
        Objects.equals(feature, mcPServerOperation.feature) &&
        Objects.equals(authType, mcPServerOperation.authType) &&
        Objects.equals(throttlingPolicy, mcPServerOperation.throttlingPolicy) &&
        Objects.equals(scopes, mcPServerOperation.scopes) &&
        Objects.equals(payloadSchema, mcPServerOperation.payloadSchema) &&
        Objects.equals(uriMapping, mcPServerOperation.uriMapping) &&
        Objects.equals(schemaDefinition, mcPServerOperation.schemaDefinition) &&
        Objects.equals(description, mcPServerOperation.description) &&
        Objects.equals(operationPolicies, mcPServerOperation.operationPolicies) &&
        Objects.equals(backendOperationMapping, mcPServerOperation.backendOperationMapping) &&
        Objects.equals(apiOperationMapping, mcPServerOperation.apiOperationMapping);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, target, feature, authType, throttlingPolicy, scopes, payloadSchema, uriMapping, schemaDefinition, description, operationPolicies, backendOperationMapping, apiOperationMapping);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerOperationDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    feature: ").append(toIndentedString(feature)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
    sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    payloadSchema: ").append(toIndentedString(payloadSchema)).append("\n");
    sb.append("    uriMapping: ").append(toIndentedString(uriMapping)).append("\n");
    sb.append("    schemaDefinition: ").append(toIndentedString(schemaDefinition)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    operationPolicies: ").append(toIndentedString(operationPolicies)).append("\n");
    sb.append("    backendOperationMapping: ").append(toIndentedString(backendOperationMapping)).append("\n");
    sb.append("    apiOperationMapping: ").append(toIndentedString(apiOperationMapping)).append("\n");
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

