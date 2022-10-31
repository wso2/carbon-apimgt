package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.APIOperationsDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIAdditionalPropertiesDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIAdditionalPropertiesMapDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIDeploymentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.RuntimeAPIRevisionDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



@Scope(name = "apim:api_create", description="", value ="")
@Scope(name = "apim:api_manage", description="", value ="")
public class RuntimeAPIDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private String context = null;
    private String version = null;

    @XmlType(name="TypeEnum")
    @XmlEnum(String.class)
    public enum TypeEnum {
        HTTP("HTTP"),
        WS("WS"),
        SOAPTOREST("SOAPTOREST"),
        SOAP("SOAP"),
        GRAPHQL("GRAPHQL"),
        WEBSUB("WEBSUB"),
        SSE("SSE"),
        WEBHOOK("WEBHOOK"),
        ASYNC("ASYNC");
        private String value;

        TypeEnum (String v) {
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
        public static TypeEnum fromValue(String v) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private TypeEnum type = TypeEnum.HTTP;
    private List<String> transport = new ArrayList<String>();
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties = new ArrayList<RuntimeAPIAdditionalPropertiesDTO>();
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<String, RuntimeAPIAdditionalPropertiesMapDTO>();
    private String createdTime = null;
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private String lastUpdatedTime = null;
    private List<APIOperationsDTO> operations = new ArrayList<APIOperationsDTO>();
    @Scope(name = "apim:api_publish", description="", value ="")
    @Scope(name = "apim:api_manage", description="", value ="")
    private String apiUsagePolicy = null;
    private RuntimeAPIRevisionDTO revision = null;
    private List<RuntimeAPIDeploymentDTO> deployments = new ArrayList<RuntimeAPIDeploymentDTO>();

  /**
   * UUID of the runtime api 
   **/
  public RuntimeAPIDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the runtime api ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public RuntimeAPIDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPI", required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Pattern(regexp="(^[^~!@#;:%^*()+={}|\\\\<>\"',&$\\[\\]/]*$)") @Size(min=1,max=60)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public RuntimeAPIDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple API for Pizza Shack online pizza delivery store.", value = "")
  @JsonProperty("description")
 @Size(max=32766)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public RuntimeAPIDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizza", required = true, value = "")
  @JsonProperty("context")
  @NotNull
 @Size(min=1,max=232)  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public RuntimeAPIDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", required = true, value = "")
  @JsonProperty("version")
  @NotNull
 @Pattern(regexp="^[^~!@#;:%^*()+={}|\\\\<>\"',&/$\\[\\]\\s+/]+$") @Size(min=1,max=30)  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * The api creation type to be used. Accepted values are HTTP, WS, SOAPTOREST, GRAPHQL, WEBSUB, SSE, WEBHOOK, ASYNC
   **/
  public RuntimeAPIDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "HTTP", value = "The api creation type to be used. Accepted values are HTTP, WS, SOAPTOREST, GRAPHQL, WEBSUB, SSE, WEBHOOK, ASYNC")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * Supported transports for the API (http and/or https). 
   **/
  public RuntimeAPIDTO transport(List<String> transport) {
    this.transport = transport;
    return this;
  }

  
  @ApiModelProperty(example = "[\"http\",\"https\"]", value = "Supported transports for the API (http and/or https). ")
  @JsonProperty("transport")
  public List<String> getTransport() {
    return transport;
  }
  public void setTransport(List<String> transport) {
    this.transport = transport;
  }

  /**
   * Map of custom properties of API
   **/
  public RuntimeAPIDTO additionalProperties(List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "Map of custom properties of API")
      @Valid
  @JsonProperty("additionalProperties")
  public List<RuntimeAPIAdditionalPropertiesDTO> getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(List<RuntimeAPIAdditionalPropertiesDTO> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public RuntimeAPIDTO additionalPropertiesMap(Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalPropertiesMap")
  public Map<String, RuntimeAPIAdditionalPropertiesMapDTO> getAdditionalPropertiesMap() {
    return additionalPropertiesMap;
  }
  public void setAdditionalPropertiesMap(Map<String, RuntimeAPIAdditionalPropertiesMapDTO> additionalPropertiesMap) {
    this.additionalPropertiesMap = additionalPropertiesMap;
  }

  /**
   **/
  public RuntimeAPIDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public RuntimeAPIDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   **/
  public RuntimeAPIDTO operations(List<APIOperationsDTO> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"target\":\"/order/{orderId}\",\"verb\":\"POST\",\"usagePlan\":\"Unlimited\"},{\"target\":\"/menu\",\"verb\":\"GET\",\"usagePlan\":\"Unlimited\"}]", value = "")
      @Valid
  @JsonProperty("operations")
  public List<APIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<APIOperationsDTO> operations) {
    this.operations = operations;
  }

  /**
   * The API level usage policy selected for the particular Runtime API
   **/
  public RuntimeAPIDTO apiUsagePolicy(String apiUsagePolicy) {
    this.apiUsagePolicy = apiUsagePolicy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "The API level usage policy selected for the particular Runtime API")
  @JsonProperty("apiUsagePolicy")
  public String getApiUsagePolicy() {
    return apiUsagePolicy;
  }
  public void setApiUsagePolicy(String apiUsagePolicy) {
    this.apiUsagePolicy = apiUsagePolicy;
  }

  /**
   **/
  public RuntimeAPIDTO revision(RuntimeAPIRevisionDTO revision) {
    this.revision = revision;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("revision")
  public RuntimeAPIRevisionDTO getRevision() {
    return revision;
  }
  public void setRevision(RuntimeAPIRevisionDTO revision) {
    this.revision = revision;
  }

  /**
   **/
  public RuntimeAPIDTO deployments(List<RuntimeAPIDeploymentDTO> deployments) {
    this.deployments = deployments;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"name\":\"US\",\"deployedTime\":\"2022-10-28T06:13:35.024Z\"},{\"name\":\"Europe\",\"deployedTime\":\"2022-10-28T06:13:35.024Z\"}]", value = "")
      @Valid
  @JsonProperty("deployments")
  public List<RuntimeAPIDeploymentDTO> getDeployments() {
    return deployments;
  }
  public void setDeployments(List<RuntimeAPIDeploymentDTO> deployments) {
    this.deployments = deployments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RuntimeAPIDTO runtimeAPI = (RuntimeAPIDTO) o;
    return Objects.equals(id, runtimeAPI.id) &&
        Objects.equals(name, runtimeAPI.name) &&
        Objects.equals(description, runtimeAPI.description) &&
        Objects.equals(context, runtimeAPI.context) &&
        Objects.equals(version, runtimeAPI.version) &&
        Objects.equals(type, runtimeAPI.type) &&
        Objects.equals(transport, runtimeAPI.transport) &&
        Objects.equals(additionalProperties, runtimeAPI.additionalProperties) &&
        Objects.equals(additionalPropertiesMap, runtimeAPI.additionalPropertiesMap) &&
        Objects.equals(createdTime, runtimeAPI.createdTime) &&
        Objects.equals(lastUpdatedTime, runtimeAPI.lastUpdatedTime) &&
        Objects.equals(operations, runtimeAPI.operations) &&
        Objects.equals(apiUsagePolicy, runtimeAPI.apiUsagePolicy) &&
        Objects.equals(revision, runtimeAPI.revision) &&
        Objects.equals(deployments, runtimeAPI.deployments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, context, version, type, transport, additionalProperties, additionalPropertiesMap, createdTime, lastUpdatedTime, operations, apiUsagePolicy, revision, deployments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RuntimeAPIDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    additionalPropertiesMap: ").append(toIndentedString(additionalPropertiesMap)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    apiUsagePolicy: ").append(toIndentedString(apiUsagePolicy)).append("\n");
    sb.append("    revision: ").append(toIndentedString(revision)).append("\n");
    sb.append("    deployments: ").append(toIndentedString(deployments)).append("\n");
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

