package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Outcome of an import/update request for a set of federated APIs.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "Outcome of an import/update request for a set of federated APIs.")


public class FederatedAPIImportResponseDTO   {
  
    private String status = null;
    private List<String> failedIds = new ArrayList<String>();

  /**
   * Human readable summary of the outcome.
   **/
  public FederatedAPIImportResponseDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Human readable summary of the outcome.")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Identifiers of the APIs that could not be imported or updated.
   **/
  public FederatedAPIImportResponseDTO failedIds(List<String> failedIds) {
    this.failedIds = failedIds;
    return this;
  }

  
  @ApiModelProperty(value = "Identifiers of the APIs that could not be imported or updated.")
  @JsonProperty("failedIds")
  public List<String> getFailedIds() {
    return failedIds;
  }
  public void setFailedIds(List<String> failedIds) {
    this.failedIds = failedIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FederatedAPIImportResponseDTO federatedAPIImportResponse = (FederatedAPIImportResponseDTO) o;
    return Objects.equals(status, federatedAPIImportResponse.status) &&
        Objects.equals(failedIds, federatedAPIImportResponse.failedIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, failedIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FederatedAPIImportResponseDTO {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    failedIds: ").append(toIndentedString(failedIds)).append("\n");
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

