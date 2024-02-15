package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedJWTSubjectEntityDTO   {
  
    private String entityId = null;
    private String entityType = null;
    private Long revocationTime = null;
    private String organization = null;

  /**
   * Subject Id of the revoked JWT(s). Can be user id or client id.
   **/
  public RevokedJWTSubjectEntityDTO entityId(String entityId) {
    this.entityId = entityId;
    return this;
  }

  
  @ApiModelProperty(value = "Subject Id of the revoked JWT(s). Can be user id or client id.")
  @JsonProperty("entity_id")
  public String getEntityId() {
    return entityId;
  }
  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  /**
   * Type of the subject id. User id or client id.
   **/
  public RevokedJWTSubjectEntityDTO entityType(String entityType) {
    this.entityType = entityType;
    return this;
  }

  
  @ApiModelProperty(value = "Type of the subject id. User id or client id.")
  @JsonProperty("entity_type")
  public String getEntityType() {
    return entityType;
  }
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  /**
   * revocation timestamp.
   **/
  public RevokedJWTSubjectEntityDTO revocationTime(Long revocationTime) {
    this.revocationTime = revocationTime;
    return this;
  }

  
  @ApiModelProperty(value = "revocation timestamp.")
  @JsonProperty("revocation_time")
  public Long getRevocationTime() {
    return revocationTime;
  }
  public void setRevocationTime(Long revocationTime) {
    this.revocationTime = revocationTime;
  }

  /**
   * Organization of the revoked subject entity.
   **/
  public RevokedJWTSubjectEntityDTO organization(String organization) {
    this.organization = organization;
    return this;
  }

  
  @ApiModelProperty(value = "Organization of the revoked subject entity.")
  @JsonProperty("organization")
  public String getOrganization() {
    return organization;
  }
  public void setOrganization(String organization) {
    this.organization = organization;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokedJWTSubjectEntityDTO revokedJWTSubjectEntity = (RevokedJWTSubjectEntityDTO) o;
    return Objects.equals(entityId, revokedJWTSubjectEntity.entityId) &&
        Objects.equals(entityType, revokedJWTSubjectEntity.entityType) &&
        Objects.equals(revocationTime, revokedJWTSubjectEntity.revocationTime) &&
        Objects.equals(organization, revokedJWTSubjectEntity.organization);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityId, entityType, revocationTime, organization);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTSubjectEntityDTO {\n");
    
    sb.append("    entityId: ").append(toIndentedString(entityId)).append("\n");
    sb.append("    entityType: ").append(toIndentedString(entityType)).append("\n");
    sb.append("    revocationTime: ").append(toIndentedString(revocationTime)).append("\n");
    sb.append("    organization: ").append(toIndentedString(organization)).append("\n");
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

