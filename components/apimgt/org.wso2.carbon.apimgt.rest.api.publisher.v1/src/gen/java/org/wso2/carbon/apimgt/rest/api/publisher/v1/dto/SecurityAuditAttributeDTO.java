package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SecurityAuditAttributeDTO   {
  
    private String apiToken = null;
    private String collectionId = null;

  /**
   **/
  public SecurityAuditAttributeDTO apiToken(String apiToken) {
    this.apiToken = apiToken;
    return this;
  }

  
  @ApiModelProperty(example = "b1267ytf-b7gc-4aee-924d-ece81241efec", value = "")
  @JsonProperty("apiToken")
  public String getApiToken() {
    return apiToken;
  }
  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
  }

  /**
   **/
  public SecurityAuditAttributeDTO collectionId(String collectionId) {
    this.collectionId = collectionId;
    return this;
  }

  
  @ApiModelProperty(example = "456ef957-5a79-449f-83y3-9027945d3c60", value = "")
  @JsonProperty("collectionId")
  public String getCollectionId() {
    return collectionId;
  }
  public void setCollectionId(String collectionId) {
    this.collectionId = collectionId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityAuditAttributeDTO securityAuditAttribute = (SecurityAuditAttributeDTO) o;
    return Objects.equals(apiToken, securityAuditAttribute.apiToken) &&
        Objects.equals(collectionId, securityAuditAttribute.collectionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiToken, collectionId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityAuditAttributeDTO {\n");
    
    sb.append("    apiToken: ").append(toIndentedString(apiToken)).append("\n");
    sb.append("    collectionId: ").append(toIndentedString(collectionId)).append("\n");
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

