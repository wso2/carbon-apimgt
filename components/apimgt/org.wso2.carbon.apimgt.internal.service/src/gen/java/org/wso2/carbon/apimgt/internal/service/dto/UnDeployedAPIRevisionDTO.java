package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class UnDeployedAPIRevisionDTO   {
  
    private String apiUUID = null;
    private String revisionUUID = null;
    private String environment = null;

  /**
   **/
  public UnDeployedAPIRevisionDTO apiUUID(String apiUUID) {
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
  public UnDeployedAPIRevisionDTO revisionUUID(String revisionUUID) {
    this.revisionUUID = revisionUUID;
    return this;
  }

  
  @ApiModelProperty(example = "c26b2b9b-4632-4ca4-b6f3-521c8863990c", value = "")
  @JsonProperty("revisionUUID")
 @Size(min=0,max=255)  public String getRevisionUUID() {
    return revisionUUID;
  }
  public void setRevisionUUID(String revisionUUID) {
    this.revisionUUID = revisionUUID;
  }

  /**
   **/
  public UnDeployedAPIRevisionDTO environment(String environment) {
    this.environment = environment;
    return this;
  }

  
  @ApiModelProperty(example = "Default", value = "")
  @JsonProperty("environment")
 @Size(min=0,max=255)  public String getEnvironment() {
    return environment;
  }
  public void setEnvironment(String environment) {
    this.environment = environment;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UnDeployedAPIRevisionDTO unDeployedAPIRevision = (UnDeployedAPIRevisionDTO) o;
    return Objects.equals(apiUUID, unDeployedAPIRevision.apiUUID) &&
        Objects.equals(revisionUUID, unDeployedAPIRevision.revisionUUID) &&
        Objects.equals(environment, unDeployedAPIRevision.environment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiUUID, revisionUUID, environment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UnDeployedAPIRevisionDTO {\n");
    
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    revisionUUID: ").append(toIndentedString(revisionUUID)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
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

