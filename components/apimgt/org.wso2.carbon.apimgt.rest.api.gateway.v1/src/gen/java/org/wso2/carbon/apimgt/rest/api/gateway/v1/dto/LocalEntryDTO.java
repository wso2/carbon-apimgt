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
import com.fasterxml.jackson.annotation.JsonCreator;



public class LocalEntryDTO   {
  
    private List<String> deployedLocalEntries = new ArrayList<>();
    private List<String> unDeployedLocalEntries = new ArrayList<>();

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
  public LocalEntryDTO unDeployedLocalEntries(List<String> unDeployedLocalEntries) {
    this.unDeployedLocalEntries = unDeployedLocalEntries;
    return this;
  }

  
  @ApiModelProperty(value = "The local entries which has not been deployed in the gateway ")
  @JsonProperty("UnDeployedLocalEntries")
  public List<String> getUnDeployedLocalEntries() {
    return unDeployedLocalEntries;
  }
  public void setUnDeployedLocalEntries(List<String> unDeployedLocalEntries) {
    this.unDeployedLocalEntries = unDeployedLocalEntries;
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
        Objects.equals(unDeployedLocalEntries, localEntry.unDeployedLocalEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployedLocalEntries, unDeployedLocalEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LocalEntryDTO {\n");
    
    sb.append("    deployedLocalEntries: ").append(toIndentedString(deployedLocalEntries)).append("\n");
    sb.append("    unDeployedLocalEntries: ").append(toIndentedString(unDeployedLocalEntries)).append("\n");
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

