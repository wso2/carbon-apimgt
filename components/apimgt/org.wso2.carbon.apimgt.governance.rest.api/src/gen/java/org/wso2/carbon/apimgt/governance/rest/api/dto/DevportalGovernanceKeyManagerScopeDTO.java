package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Key Manager scope for a Devportal Governance template ruleset binding.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Key Manager scope for a Devportal Governance template ruleset binding.")

public class DevportalGovernanceKeyManagerScopeDTO   {

    private String keyManagerUuid = null;
    private String organization = null;

  /**
   * UUID of the Key Manager.
   **/
  public DevportalGovernanceKeyManagerScopeDTO keyManagerUuid(String keyManagerUuid) {
    this.keyManagerUuid = keyManagerUuid;
    return this;
  }


  @ApiModelProperty(example = "6f9dcb8a-20f4-421d-9b45-6b89f3f6e8b4", required = true, value = "UUID of the Key Manager.")
  @JsonProperty("keyManagerUuid")
  @NotNull
  public String getKeyManagerUuid() {
    return keyManagerUuid;
  }
  public void setKeyManagerUuid(String keyManagerUuid) {
    this.keyManagerUuid = keyManagerUuid;
  }

  /**
   * Organization associated with the Key Manager scope.
   **/
  public DevportalGovernanceKeyManagerScopeDTO organization(String organization) {
    this.organization = organization;
    return this;
  }


  @ApiModelProperty(example = "carbon.super", value = "Organization associated with the Key Manager scope.")
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
    DevportalGovernanceKeyManagerScopeDTO devportalGovernanceKeyManagerScope = (DevportalGovernanceKeyManagerScopeDTO) o;
    return Objects.equals(keyManagerUuid, devportalGovernanceKeyManagerScope.keyManagerUuid) &&
        Objects.equals(organization, devportalGovernanceKeyManagerScope.organization);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyManagerUuid, organization);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DevportalGovernanceKeyManagerScopeDTO {\n");

    sb.append("    keyManagerUuid: ").append(toIndentedString(keyManagerUuid)).append("\n");
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
