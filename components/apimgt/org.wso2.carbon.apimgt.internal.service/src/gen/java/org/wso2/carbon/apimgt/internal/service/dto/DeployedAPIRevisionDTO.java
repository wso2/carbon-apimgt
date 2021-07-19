package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedEnvInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class DeployedAPIRevisionDTO   {
  
    private String apiID = null;
    private Integer revisionID = null;
    private List<DeployedEnvInfoDTO> envInfo = new ArrayList<>();

  /**
   **/
  public DeployedAPIRevisionDTO apiID(String apiID) {
    this.apiID = apiID;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("apiID")
 @Size(min=0,max=255)  public String getApiID() {
    return apiID;
  }
  public void setApiID(String apiID) {
    this.apiID = apiID;
  }

  /**
   **/
  public DeployedAPIRevisionDTO revisionID(Integer revisionID) {
    this.revisionID = revisionID;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("revisionID")
  public Integer getRevisionID() {
    return revisionID;
  }
  public void setRevisionID(Integer revisionID) {
    this.revisionID = revisionID;
  }

  /**
   **/
  public DeployedAPIRevisionDTO envInfo(List<DeployedEnvInfoDTO> envInfo) {
    this.envInfo = envInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
    return Objects.equals(apiID, deployedAPIRevision.apiID) &&
        Objects.equals(revisionID, deployedAPIRevision.revisionID) &&
        Objects.equals(envInfo, deployedAPIRevision.envInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiID, revisionID, envInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployedAPIRevisionDTO {\n");
    
    sb.append("    apiID: ").append(toIndentedString(apiID)).append("\n");
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

