package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CreateServiceRequestDTO   {
  
    private ServiceDTO catalogEntry = null;
    private File definitionFile = null;

  /**
   **/
  public CreateServiceRequestDTO catalogEntry(ServiceDTO catalogEntry) {
    this.catalogEntry = catalogEntry;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("catalogEntry")
  @NotNull
  public ServiceDTO getCatalogEntry() {
    return catalogEntry;
  }
  public void setCatalogEntry(ServiceDTO catalogEntry) {
    this.catalogEntry = catalogEntry;
  }

  /**
   **/
  public CreateServiceRequestDTO definitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("definitionFile")
  @NotNull
  public File getDefinitionFile() {
    return definitionFile;
  }
  public void setDefinitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateServiceRequestDTO createServiceRequest = (CreateServiceRequestDTO) o;
    return Objects.equals(catalogEntry, createServiceRequest.catalogEntry) &&
        Objects.equals(definitionFile, createServiceRequest.definitionFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(catalogEntry, definitionFile);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateServiceRequestDTO {\n");
    
    sb.append("    catalogEntry: ").append(toIndentedString(catalogEntry)).append("\n");
    sb.append("    definitionFile: ").append(toIndentedString(definitionFile)).append("\n");
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

