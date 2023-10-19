package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTUserDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedConditionsDTO   {
  
    private List<RevokedJWTDTO> revokedJWTList = new ArrayList<>();
    private List<RevokedJWTUserDTO> revokedJWTUserList = new ArrayList<>();
    private List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList = new ArrayList<>();

  /**
   **/
  public RevokedConditionsDTO revokedJWTList(List<RevokedJWTDTO> revokedJWTList) {
    this.revokedJWTList = revokedJWTList;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("revokedJWTList")
  public List<RevokedJWTDTO> getRevokedJWTList() {
    return revokedJWTList;
  }
  public void setRevokedJWTList(List<RevokedJWTDTO> revokedJWTList) {
    this.revokedJWTList = revokedJWTList;
  }

  /**
   **/
  public RevokedConditionsDTO revokedJWTUserList(List<RevokedJWTUserDTO> revokedJWTUserList) {
    this.revokedJWTUserList = revokedJWTUserList;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("revokedJWTUserList")
  public List<RevokedJWTUserDTO> getRevokedJWTUserList() {
    return revokedJWTUserList;
  }
  public void setRevokedJWTUserList(List<RevokedJWTUserDTO> revokedJWTUserList) {
    this.revokedJWTUserList = revokedJWTUserList;
  }

  /**
   **/
  public RevokedConditionsDTO revokedJWTConsumerKeyList(List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList) {
    this.revokedJWTConsumerKeyList = revokedJWTConsumerKeyList;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("revokedJWTConsumerKeyList")
  public List<RevokedJWTConsumerKeyDTO> getRevokedJWTConsumerKeyList() {
    return revokedJWTConsumerKeyList;
  }
  public void setRevokedJWTConsumerKeyList(List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList) {
    this.revokedJWTConsumerKeyList = revokedJWTConsumerKeyList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokedConditionsDTO revokedConditions = (RevokedConditionsDTO) o;
    return Objects.equals(revokedJWTList, revokedConditions.revokedJWTList) &&
        Objects.equals(revokedJWTUserList, revokedConditions.revokedJWTUserList) &&
        Objects.equals(revokedJWTConsumerKeyList, revokedConditions.revokedJWTConsumerKeyList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revokedJWTList, revokedJWTUserList, revokedJWTConsumerKeyList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedConditionsDTO {\n");
    
    sb.append("    revokedJWTList: ").append(toIndentedString(revokedJWTList)).append("\n");
    sb.append("    revokedJWTUserList: ").append(toIndentedString(revokedJWTUserList)).append("\n");
    sb.append("    revokedJWTConsumerKeyList: ").append(toIndentedString(revokedJWTConsumerKeyList)).append("\n");
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

