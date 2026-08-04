package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class FederatedAPIImportRequestDTO   {
  
    private String id = null;
    private String displayName = null;
    private String description = null;

  /**
   * Identifier of the API on the federated gateway. Either the gateway&#39;s native API ID or the composite \&quot;name:version\&quot; key of a discovered API. 
   **/
  public FederatedAPIImportRequestDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "abcd1234", required = true, value = "Identifier of the API on the federated gateway. Either the gateway's native API ID or the composite \"name:version\" key of a discovered API. ")
  @JsonProperty("id")
  @NotNull
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Optional display name to assign to the API instead of the discovered one.
   **/
  public FederatedAPIImportRequestDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Customer API", value = "Optional display name to assign to the API instead of the discovered one.")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Optional description to assign to the API instead of the discovered one.
   **/
  public FederatedAPIImportRequestDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Provides customer details.", value = "Optional description to assign to the API instead of the discovered one.")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederatedAPIImportRequestDTO federatedAPIImportRequest = (FederatedAPIImportRequestDTO) o;
    return Objects.equals(id, federatedAPIImportRequest.id) &&
        Objects.equals(displayName, federatedAPIImportRequest.displayName) &&
        Objects.equals(description, federatedAPIImportRequest.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, displayName, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FederatedAPIImportRequestDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

