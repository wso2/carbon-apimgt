package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class IPLevelDTO   {
  
    private String type = null;
    private String fixedIp = null;
    private String startingIp = null;
    private String endingIp = null;
    private Boolean invert = null;
    private String tenantDomain = null;
    private Integer id = null;

  /**
   **/
  public IPLevelDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "iprange", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * IP address.
   **/
  public IPLevelDTO fixedIp(String fixedIp) {
    this.fixedIp = fixedIp;
    return this;
  }

  
  @ApiModelProperty(value = "IP address.")
  @JsonProperty("fixedIp")
  public String getFixedIp() {
    return fixedIp;
  }
  public void setFixedIp(String fixedIp) {
    this.fixedIp = fixedIp;
  }

  /**
   * Ip Range Starting IP
   **/
  public IPLevelDTO startingIp(String startingIp) {
    this.startingIp = startingIp;
    return this;
  }

  
  @ApiModelProperty(value = "Ip Range Starting IP")
  @JsonProperty("startingIp")
  public String getStartingIp() {
    return startingIp;
  }
  public void setStartingIp(String startingIp) {
    this.startingIp = startingIp;
  }

  /**
   * Ip Range Ending IP.
   **/
  public IPLevelDTO endingIp(String endingIp) {
    this.endingIp = endingIp;
    return this;
  }

  
  @ApiModelProperty(value = "Ip Range Ending IP.")
  @JsonProperty("endingIp")
  public String getEndingIp() {
    return endingIp;
  }
  public void setEndingIp(String endingIp) {
    this.endingIp = endingIp;
  }

  /**
   * Condition is invert.
   **/
  public IPLevelDTO invert(Boolean invert) {
    this.invert = invert;
    return this;
  }

  
  @ApiModelProperty(value = "Condition is invert.")
  @JsonProperty("invert")
  public Boolean isInvert() {
    return invert;
  }
  public void setInvert(Boolean invert) {
    this.invert = invert;
  }

  /**
   **/
  public IPLevelDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public IPLevelDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IPLevelDTO ipLevel = (IPLevelDTO) o;
    return Objects.equals(type, ipLevel.type) &&
        Objects.equals(fixedIp, ipLevel.fixedIp) &&
        Objects.equals(startingIp, ipLevel.startingIp) &&
        Objects.equals(endingIp, ipLevel.endingIp) &&
        Objects.equals(invert, ipLevel.invert) &&
        Objects.equals(tenantDomain, ipLevel.tenantDomain) &&
        Objects.equals(id, ipLevel.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, fixedIp, startingIp, endingIp, invert, tenantDomain, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPLevelDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    fixedIp: ").append(toIndentedString(fixedIp)).append("\n");
    sb.append("    startingIp: ").append(toIndentedString(startingIp)).append("\n");
    sb.append("    endingIp: ").append(toIndentedString(endingIp)).append("\n");
    sb.append("    invert: ").append(toIndentedString(invert)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

