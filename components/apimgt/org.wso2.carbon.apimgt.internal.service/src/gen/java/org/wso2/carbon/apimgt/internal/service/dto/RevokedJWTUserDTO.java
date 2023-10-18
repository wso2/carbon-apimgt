package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedJWTUserDTO   {
  
    private String subjectId = null;
    private String subjectIdType = null;
    private Long revocationTime = null;
    private String organization = null;

  /**
   * Subject Id of the revoked JWT(s). Can be user id or client id.
   **/
  public RevokedJWTUserDTO subjectId(String subjectId) {
    this.subjectId = subjectId;
    return this;
  }

  
  @ApiModelProperty(value = "Subject Id of the revoked JWT(s). Can be user id or client id.")
  @JsonProperty("subject_id")
  public String getSubjectId() {
    return subjectId;
  }
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * Type of the subject id. User id or client id.
   **/
  public RevokedJWTUserDTO subjectIdType(String subjectIdType) {
    this.subjectIdType = subjectIdType;
    return this;
  }

  
  @ApiModelProperty(value = "Type of the subject id. User id or client id.")
  @JsonProperty("subject_id_type")
  public String getSubjectIdType() {
    return subjectIdType;
  }
  public void setSubjectIdType(String subjectIdType) {
    this.subjectIdType = subjectIdType;
  }

  /**
   * revocation timestamp.
   **/
  public RevokedJWTUserDTO revocationTime(Long revocationTime) {
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
   * Organization of the revoked user.
   **/
  public RevokedJWTUserDTO organization(String organization) {
    this.organization = organization;
    return this;
  }

  
  @ApiModelProperty(value = "Organization of the revoked user.")
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
    RevokedJWTUserDTO revokedJWTUser = (RevokedJWTUserDTO) o;
    return Objects.equals(subjectId, revokedJWTUser.subjectId) &&
        Objects.equals(subjectIdType, revokedJWTUser.subjectIdType) &&
        Objects.equals(revocationTime, revokedJWTUser.revocationTime) &&
        Objects.equals(organization, revokedJWTUser.organization);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectId, subjectIdType, revocationTime, organization);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTUserDTO {\n");
    
    sb.append("    subjectId: ").append(toIndentedString(subjectId)).append("\n");
    sb.append("    subjectIdType: ").append(toIndentedString(subjectIdType)).append("\n");
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

