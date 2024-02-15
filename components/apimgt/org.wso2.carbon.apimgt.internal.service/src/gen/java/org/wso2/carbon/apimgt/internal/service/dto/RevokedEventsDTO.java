package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTSubjectEntityDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedEventsDTO   {
  
    private List<RevokedJWTDTO> revokedJWTList = new ArrayList<>();
    private List<RevokedJWTSubjectEntityDTO> revokedJWTSubjectEntityList = new ArrayList<>();
    private List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList = new ArrayList<>();

  /**
   **/
  public RevokedEventsDTO revokedJWTList(List<RevokedJWTDTO> revokedJWTList) {
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
  public RevokedEventsDTO revokedJWTSubjectEntityList(List<RevokedJWTSubjectEntityDTO> revokedJWTSubjectEntityList) {
    this.revokedJWTSubjectEntityList = revokedJWTSubjectEntityList;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("revokedJWTSubjectEntityList")
  public List<RevokedJWTSubjectEntityDTO> getRevokedJWTSubjectEntityList() {
    return revokedJWTSubjectEntityList;
  }
  public void setRevokedJWTSubjectEntityList(List<RevokedJWTSubjectEntityDTO> revokedJWTSubjectEntityList) {
    this.revokedJWTSubjectEntityList = revokedJWTSubjectEntityList;
  }

  /**
   **/
  public RevokedEventsDTO revokedJWTConsumerKeyList(List<RevokedJWTConsumerKeyDTO> revokedJWTConsumerKeyList) {
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
    RevokedEventsDTO revokedEvents = (RevokedEventsDTO) o;
    return Objects.equals(revokedJWTList, revokedEvents.revokedJWTList) &&
        Objects.equals(revokedJWTSubjectEntityList, revokedEvents.revokedJWTSubjectEntityList) &&
        Objects.equals(revokedJWTConsumerKeyList, revokedEvents.revokedJWTConsumerKeyList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revokedJWTList, revokedJWTSubjectEntityList, revokedJWTConsumerKeyList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedEventsDTO {\n");
    
    sb.append("    revokedJWTList: ").append(toIndentedString(revokedJWTList)).append("\n");
    sb.append("    revokedJWTSubjectEntityList: ").append(toIndentedString(revokedJWTSubjectEntityList)).append("\n");
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

