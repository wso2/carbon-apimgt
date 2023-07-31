package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MajorRangeVersionInfoDTO   {
  
    private Boolean isLatest = null;
    private String latestVersion = null;
    private String latestVersionAPIId = null;

  /**
   * Flag to indicate whether the API is the latest version in its major version range
   **/
  public MajorRangeVersionInfoDTO isLatest(Boolean isLatest) {
    this.isLatest = isLatest;
    return this;
  }

  
  @ApiModelProperty(example = "false", required = true, value = "Flag to indicate whether the API is the latest version in its major version range")
  @JsonProperty("isLatest")
  @NotNull
  public Boolean isIsLatest() {
    return isLatest;
  }
  public void setIsLatest(Boolean isLatest) {
    this.isLatest = isLatest;
  }

  /**
   * Latest API&#39;s version in API&#39;s major version range
   **/
  public MajorRangeVersionInfoDTO latestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
    return this;
  }

  
  @ApiModelProperty(example = "v1.3.1", required = true, value = "Latest API's version in API's major version range")
  @JsonProperty("latestVersion")
  @NotNull
  public String getLatestVersion() {
    return latestVersion;
  }
  public void setLatestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  /**
   * Latest API&#39;s identifier in API&#39;s major version range
   **/
  public MajorRangeVersionInfoDTO latestVersionAPIId(String latestVersionAPIId) {
    this.latestVersionAPIId = latestVersionAPIId;
    return this;
  }

  
  @ApiModelProperty(example = "64c37428722fa140f5457628", required = true, value = "Latest API's identifier in API's major version range")
  @JsonProperty("latestVersionAPIId")
  @NotNull
  public String getLatestVersionAPIId() {
    return latestVersionAPIId;
  }
  public void setLatestVersionAPIId(String latestVersionAPIId) {
    this.latestVersionAPIId = latestVersionAPIId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MajorRangeVersionInfoDTO majorRangeVersionInfo = (MajorRangeVersionInfoDTO) o;
    return Objects.equals(isLatest, majorRangeVersionInfo.isLatest) &&
        Objects.equals(latestVersion, majorRangeVersionInfo.latestVersion) &&
        Objects.equals(latestVersionAPIId, majorRangeVersionInfo.latestVersionAPIId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isLatest, latestVersion, latestVersionAPIId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MajorRangeVersionInfoDTO {\n");
    
    sb.append("    isLatest: ").append(toIndentedString(isLatest)).append("\n");
    sb.append("    latestVersion: ").append(toIndentedString(latestVersion)).append("\n");
    sb.append("    latestVersionAPIId: ").append(toIndentedString(latestVersionAPIId)).append("\n");
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

