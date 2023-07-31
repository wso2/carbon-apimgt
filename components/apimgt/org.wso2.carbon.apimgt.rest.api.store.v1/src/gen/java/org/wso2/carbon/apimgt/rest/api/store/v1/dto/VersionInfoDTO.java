package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MajorRangeVersionInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class VersionInfoDTO   {
  
    private String version = null;
    private MajorRangeVersionInfoDTO majorRange = null;

  /**
   * API version
   **/
  public VersionInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "v1.2.3", required = true, value = "API version")
  @JsonProperty("version")
  @NotNull
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public VersionInfoDTO majorRange(MajorRangeVersionInfoDTO majorRange) {
    this.majorRange = majorRange;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("majorRange")
  @NotNull
  public MajorRangeVersionInfoDTO getMajorRange() {
    return majorRange;
  }
  public void setMajorRange(MajorRangeVersionInfoDTO majorRange) {
    this.majorRange = majorRange;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VersionInfoDTO versionInfo = (VersionInfoDTO) o;
    return Objects.equals(version, versionInfo.version) &&
        Objects.equals(majorRange, versionInfo.majorRange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, majorRange);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VersionInfoDTO {\n");
    
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    majorRange: ").append(toIndentedString(majorRange)).append("\n");
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

