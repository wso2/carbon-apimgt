package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

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



public class VerifierDTO   {
  
    private String key = null;
    private String md5 = null;

  /**
   **/
  public VerifierDTO key(String key) {
    this.key = key;
    return this;
  }

  
  @ApiModelProperty(example = "petStore-1.0.0", required = true, value = "")
  @JsonProperty("key")
  @NotNull
 @Pattern(regexp="^[^\\*]+$")  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  /**
   **/
  public VerifierDTO md5(String md5) {
    this.md5 = md5;
    return this;
  }

  
  @ApiModelProperty(example = "e27ef979af14c72783b8112dc41c3434c09763ddb230e1a829d5f9134d1abd07", required = true, value = "")
  @JsonProperty("md5")
  @NotNull
  public String getMd5() {
    return md5;
  }
  public void setMd5(String md5) {
    this.md5 = md5;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifierDTO verifier = (VerifierDTO) o;
    return Objects.equals(key, verifier.key) &&
        Objects.equals(md5, verifier.md5);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, md5);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifierDTO {\n");
    
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
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

