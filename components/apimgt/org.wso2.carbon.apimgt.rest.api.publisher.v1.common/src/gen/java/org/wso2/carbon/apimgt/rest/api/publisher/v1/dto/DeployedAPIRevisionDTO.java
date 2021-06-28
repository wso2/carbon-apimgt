package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeployedEnvInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DeployedAPIRevisionDTO   {
  
    private String apiUUID = null;
    private String revisionID = null;
    private List<DeployedEnvInfoDTO> envInfo = new ArrayList<DeployedEnvInfoDTO>();

  /**
   **/
  public DeployedAPIRevisionDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("apiUUID")
 @Size(min=0,max=255)  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public DeployedAPIRevisionDTO revisionID(String revisionID) {
    this.revisionID = revisionID;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("revisionID")
  public String getRevisionID() {
    return revisionID;
  }
  public void setRevisionID(String revisionID) {
    this.revisionID = revisionID;
  }

  /**
   **/
  public DeployedAPIRevisionDTO envInfo(List<DeployedEnvInfoDTO> envInfo) {
    this.envInfo = envInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("envInfo")
  public List<DeployedEnvInfoDTO> getEnvInfo() {
    return envInfo;
  }
  public void setEnvInfo(List<DeployedEnvInfoDTO> envInfo) {
    this.envInfo = envInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployedAPIRevisionDTO deployedAPIRevision = (DeployedAPIRevisionDTO) o;
    return Objects.equals(apiUUID, deployedAPIRevision.apiUUID) &&
        Objects.equals(revisionID, deployedAPIRevision.revisionID) &&
        Objects.equals(envInfo, deployedAPIRevision.envInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, revisionID, envInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployedAPIRevisionDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    revisionID: ").append(toIndentedString(revisionID)).append("\n");
    sb.append("    envInfo: ").append(toIndentedString(envInfo)).append("\n");
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

