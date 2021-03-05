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



public class ServiceSchemaDTO   {
  
    private ServiceDTO serviceMetadata = null;
    private File definitionFile = null;
    private String inlineContent = null;

  /**
   **/
  public ServiceSchemaDTO serviceMetadata(ServiceDTO serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("serviceMetadata")
  @NotNull
  public ServiceDTO getServiceMetadata() {
    return serviceMetadata;
  }
  public void setServiceMetadata(ServiceDTO serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
  }

  /**
   **/
  public ServiceSchemaDTO definitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("definitionFile")
  public File getDefinitionFile() {
    return definitionFile;
  }
  public void setDefinitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
  }

  /**
   * Inline content of the document
   **/
  public ServiceSchemaDTO inlineContent(String inlineContent) {
    this.inlineContent = inlineContent;
    return this;
  }

  
  @ApiModelProperty(value = "Inline content of the document")
  @JsonProperty("inlineContent")
  public String getInlineContent() {
    return inlineContent;
  }
  public void setInlineContent(String inlineContent) {
    this.inlineContent = inlineContent;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceSchemaDTO serviceSchema = (ServiceSchemaDTO) o;
    return Objects.equals(serviceMetadata, serviceSchema.serviceMetadata) &&
        Objects.equals(definitionFile, serviceSchema.definitionFile) &&
        Objects.equals(inlineContent, serviceSchema.inlineContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceMetadata, definitionFile, inlineContent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceSchemaDTO {\n");
    
    sb.append("    serviceMetadata: ").append(toIndentedString(serviceMetadata)).append("\n");
    sb.append("    definitionFile: ").append(toIndentedString(definitionFile)).append("\n");
    sb.append("    inlineContent: ").append(toIndentedString(inlineContent)).append("\n");
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

