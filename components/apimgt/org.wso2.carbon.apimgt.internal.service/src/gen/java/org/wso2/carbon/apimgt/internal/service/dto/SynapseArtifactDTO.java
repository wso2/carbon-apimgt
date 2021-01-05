package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.*;
import java.util.Objects;

public class SynapseArtifactDTO   {
  
    private String apiId = null;
    private String apiName = null;
    private String version = null;
    private String tenantDomain = null;
    private String gatewayLabel = null;
    private String gatewayInstruction = null;
    private String bytesEncodedAsString = null;

  /**
   **/
  public SynapseArtifactDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   * API Provider name.
   **/
  public SynapseArtifactDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(value = "API Provider name.")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * version of the API
   **/
  public SynapseArtifactDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(value = "version of the API")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * tenantDomain of the API
   **/
  public SynapseArtifactDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "tenantDomain of the API")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   * label of the API
   **/
  public SynapseArtifactDTO gatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
    return this;
  }

  
  @ApiModelProperty(value = "label of the API")
  @JsonProperty("gatewayLabel")
  public String getGatewayLabel() {
    return gatewayLabel;
  }
  public void setGatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
  }

  /**
   * Publish/Remove
   **/
  public SynapseArtifactDTO gatewayInstruction(String gatewayInstruction) {
    this.gatewayInstruction = gatewayInstruction;
    return this;
  }

  
  @ApiModelProperty(value = "Publish/Remove")
  @JsonProperty("gatewayInstruction")
  public String getGatewayInstruction() {
    return gatewayInstruction;
  }
  public void setGatewayInstruction(String gatewayInstruction) {
    this.gatewayInstruction = gatewayInstruction;
  }

  /**
   * bytes
   **/
  public SynapseArtifactDTO bytesEncodedAsString(String bytesEncodedAsString) {
    this.bytesEncodedAsString = bytesEncodedAsString;
    return this;
  }

  
  @ApiModelProperty(value = "bytes")
  @JsonProperty("bytesEncodedAsString")
  public String getBytesEncodedAsString() {
    return bytesEncodedAsString;
  }
  public void setBytesEncodedAsString(String bytesEncodedAsString) {
    this.bytesEncodedAsString = bytesEncodedAsString;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SynapseArtifactDTO synapseArtifact = (SynapseArtifactDTO) o;
    return Objects.equals(apiId, synapseArtifact.apiId) &&
        Objects.equals(apiName, synapseArtifact.apiName) &&
        Objects.equals(version, synapseArtifact.version) &&
        Objects.equals(tenantDomain, synapseArtifact.tenantDomain) &&
        Objects.equals(gatewayLabel, synapseArtifact.gatewayLabel) &&
        Objects.equals(gatewayInstruction, synapseArtifact.gatewayInstruction) &&
        Objects.equals(bytesEncodedAsString, synapseArtifact.bytesEncodedAsString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, apiName, version, tenantDomain, gatewayLabel, gatewayInstruction, bytesEncodedAsString);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SynapseArtifactDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    gatewayLabel: ").append(toIndentedString(gatewayLabel)).append("\n");
    sb.append("    gatewayInstruction: ").append(toIndentedString(gatewayInstruction)).append("\n");
    sb.append("    bytesEncodedAsString: ").append(toIndentedString(bytesEncodedAsString)).append("\n");
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

