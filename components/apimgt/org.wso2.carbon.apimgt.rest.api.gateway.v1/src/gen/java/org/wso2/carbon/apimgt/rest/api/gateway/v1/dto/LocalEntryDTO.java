package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class LocalEntryDTO   {
  
    private List<String> deployedLocalEntries = new ArrayList<>();
    private List<String> notdeployedLocalEntries = new ArrayList<>();

  /**
   * The local entries which has been deployed in the gateway 
   **/
  public LocalEntryDTO deployedLocalEntries(List<String> deployedLocalEntries) {
    this.deployedLocalEntries = deployedLocalEntries;
    return this;
  }

  
  @ApiModelProperty(value = "The local entries which has been deployed in the gateway ")
  @JsonProperty("deployedLocalEntries")
  public List<String> getDeployedLocalEntries() {
    return deployedLocalEntries;
  }
  public void setDeployedLocalEntries(List<String> deployedLocalEntries) {
    this.deployedLocalEntries = deployedLocalEntries;
  }

  /**
   * The local entries which has not been deployed in the gateway 
   **/
  public LocalEntryDTO notdeployedLocalEntries(List<String> notdeployedLocalEntries) {
    this.notdeployedLocalEntries = notdeployedLocalEntries;
    return this;
  }

  
  @ApiModelProperty(value = "The local entries which has not been deployed in the gateway ")
  @JsonProperty("notdeployedLocalEntries")
  public List<String> getNotdeployedLocalEntries() {
    return notdeployedLocalEntries;
  }
  public void setNotdeployedLocalEntries(List<String> notdeployedLocalEntries) {
    this.notdeployedLocalEntries = notdeployedLocalEntries;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalEntryDTO localEntry = (LocalEntryDTO) o;
    return Objects.equals(deployedLocalEntries, localEntry.deployedLocalEntries) &&
        Objects.equals(notdeployedLocalEntries, localEntry.notdeployedLocalEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployedLocalEntries, notdeployedLocalEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LocalEntryDTO {\n");
    
    sb.append("    deployedLocalEntries: ").append(toIndentedString(deployedLocalEntries)).append("\n");
    sb.append("    notdeployedLocalEntries: ").append(toIndentedString(notdeployedLocalEntries)).append("\n");
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

