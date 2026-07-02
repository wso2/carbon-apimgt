package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * An active API-scoped key for a platform gateway, returned during bulk sync.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@ApiModel(description = "An active API-scoped key for a platform gateway, returned during bulk sync.")

@JsonIgnoreProperties(ignoreUnknown = true)

public class PlatformGatewayAPIKeyDTO   {
  
    private String uuid = null;
    private String name = null;
    private String maskedApiKey = null;
    private Map<String, String> apiKeyHashes = new HashMap<>();
    private String artifactUuid = null;
    private String status = null;
    private String createdAt = null;
    private String createdBy = null;
    private String updatedAt = null;
    private String expiresAt = null;
    private String etag = null;
    private String source = null;
    private String externalRefId = null;
    private String issuer = null;

  /**
   * Unique identifier of the API key.
   **/
  public PlatformGatewayAPIKeyDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(value = "Unique identifier of the API key.")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Human-readable name of the API key.
   **/
  public PlatformGatewayAPIKeyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Human-readable name of the API key.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Masked representation of the API key (plaintext is not stored).
   **/
  public PlatformGatewayAPIKeyDTO maskedApiKey(String maskedApiKey) {
    this.maskedApiKey = maskedApiKey;
    return this;
  }

  
  @ApiModelProperty(value = "Masked representation of the API key (plaintext is not stored).")
  @JsonProperty("maskedApiKey")
  public String getMaskedApiKey() {
    return maskedApiKey;
  }
  public void setMaskedApiKey(String maskedApiKey) {
    this.maskedApiKey = maskedApiKey;
  }

  /**
   * Map of hash algorithm to hash value, e.g. {\&quot;sha256\&quot;:\&quot;&lt;hex&gt;\&quot;}.
   **/
  public PlatformGatewayAPIKeyDTO apiKeyHashes(Map<String, String> apiKeyHashes) {
    this.apiKeyHashes = apiKeyHashes;
    return this;
  }

  
  @ApiModelProperty(value = "Map of hash algorithm to hash value, e.g. {\"sha256\":\"<hex>\"}.")
  @JsonProperty("apiKeyHashes")
  public Map<String, String> getApiKeyHashes() {
    return apiKeyHashes;
  }
  public void setApiKeyHashes(Map<String, String> apiKeyHashes) {
    this.apiKeyHashes = apiKeyHashes;
  }

  /**
   * UUID of the API this key is scoped to.
   **/
  public PlatformGatewayAPIKeyDTO artifactUuid(String artifactUuid) {
    this.artifactUuid = artifactUuid;
    return this;
  }

  
  @ApiModelProperty(value = "UUID of the API this key is scoped to.")
  @JsonProperty("artifactUuid")
  public String getArtifactUuid() {
    return artifactUuid;
  }
  public void setArtifactUuid(String artifactUuid) {
    this.artifactUuid = artifactUuid;
  }

  /**
   * Key status, e.g. ACTIVE.
   **/
  public PlatformGatewayAPIKeyDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Key status, e.g. ACTIVE.")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * ISO-8601 creation timestamp.
   **/
  public PlatformGatewayAPIKeyDTO createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "ISO-8601 creation timestamp.")
  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Username that created this key.
   **/
  public PlatformGatewayAPIKeyDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(value = "Username that created this key.")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * ISO-8601 last-updated timestamp.
   **/
  public PlatformGatewayAPIKeyDTO updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "ISO-8601 last-updated timestamp.")
  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * ISO-8601 expiry timestamp. Absent if the key never expires.
   **/
  public PlatformGatewayAPIKeyDTO expiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  
  @ApiModelProperty(value = "ISO-8601 expiry timestamp. Absent if the key never expires.")
  @JsonProperty("expiresAt")
  public String getExpiresAt() {
    return expiresAt;
  }
  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   * Opaque version tag for cache validation.
   **/
  public PlatformGatewayAPIKeyDTO etag(String etag) {
    this.etag = etag;
    return this;
  }

  
  @ApiModelProperty(value = "Opaque version tag for cache validation.")
  @JsonProperty("etag")
  public String getEtag() {
    return etag;
  }
  public void setEtag(String etag) {
    this.etag = etag;
  }

  /**
   * Origin of the key, e.g. &#39;external&#39;.
   **/
  public PlatformGatewayAPIKeyDTO source(String source) {
    this.source = source;
    return this;
  }

  
  @ApiModelProperty(value = "Origin of the key, e.g. 'external'.")
  @JsonProperty("source")
  public String getSource() {
    return source;
  }
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * External reference identifier.
   **/
  public PlatformGatewayAPIKeyDTO externalRefId(String externalRefId) {
    this.externalRefId = externalRefId;
    return this;
  }

  
  @ApiModelProperty(value = "External reference identifier.")
  @JsonProperty("externalRefId")
  public String getExternalRefId() {
    return externalRefId;
  }
  public void setExternalRefId(String externalRefId) {
    this.externalRefId = externalRefId;
  }

  /**
   * Issuer identifier.
   **/
  public PlatformGatewayAPIKeyDTO issuer(String issuer) {
    this.issuer = issuer;
    return this;
  }

  
  @ApiModelProperty(value = "Issuer identifier.")
  @JsonProperty("issuer")
  public String getIssuer() {
    return issuer;
  }
  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlatformGatewayAPIKeyDTO platformGatewayAPIKey = (PlatformGatewayAPIKeyDTO) o;
    return Objects.equals(uuid, platformGatewayAPIKey.uuid) &&
        Objects.equals(name, platformGatewayAPIKey.name) &&
        Objects.equals(maskedApiKey, platformGatewayAPIKey.maskedApiKey) &&
        Objects.equals(apiKeyHashes, platformGatewayAPIKey.apiKeyHashes) &&
        Objects.equals(artifactUuid, platformGatewayAPIKey.artifactUuid) &&
        Objects.equals(status, platformGatewayAPIKey.status) &&
        Objects.equals(createdAt, platformGatewayAPIKey.createdAt) &&
        Objects.equals(createdBy, platformGatewayAPIKey.createdBy) &&
        Objects.equals(updatedAt, platformGatewayAPIKey.updatedAt) &&
        Objects.equals(expiresAt, platformGatewayAPIKey.expiresAt) &&
        Objects.equals(etag, platformGatewayAPIKey.etag) &&
        Objects.equals(source, platformGatewayAPIKey.source) &&
        Objects.equals(externalRefId, platformGatewayAPIKey.externalRefId) &&
        Objects.equals(issuer, platformGatewayAPIKey.issuer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, maskedApiKey, apiKeyHashes, artifactUuid, status, createdAt, createdBy, updatedAt, expiresAt, etag, source, externalRefId, issuer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformGatewayAPIKeyDTO {\n");
    
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    maskedApiKey: ").append(toIndentedString(maskedApiKey)).append("\n");
    sb.append("    apiKeyHashes: ").append(toIndentedString(apiKeyHashes)).append("\n");
    sb.append("    artifactUuid: ").append(toIndentedString(artifactUuid)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    externalRefId: ").append(toIndentedString(externalRefId)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
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

