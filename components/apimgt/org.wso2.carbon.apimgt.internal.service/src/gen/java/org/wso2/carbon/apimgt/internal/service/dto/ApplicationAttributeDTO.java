package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationAttributeDTO   {
  
    private Integer applicationId = null;
    private String name = null;
    private String value = null;
    private String tenant = null;

  /**
   * application ID of the attribute associated with
   **/
  public ApplicationAttributeDTO applicationId(Integer applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "application ID of the attribute associated with")
  @JsonProperty("applicationId")
  public Integer getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * name of the attribute
   **/
  public ApplicationAttributeDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "name of the attribute")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * the value of the attribute
   **/
  public ApplicationAttributeDTO value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "the value of the attribute")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * tenant domain
   **/
  public ApplicationAttributeDTO tenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  
  @ApiModelProperty(example = "wso2.com", value = "tenant domain")
  @JsonProperty("tenant")
  public String getTenant() {
    return tenant;
  }
  public void setTenant(String tenant) {
    this.tenant = tenant;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationAttributeDTO applicationAttribute = (ApplicationAttributeDTO) o;
    return Objects.equals(applicationId, applicationAttribute.applicationId) &&
        Objects.equals(name, applicationAttribute.name) &&
        Objects.equals(value, applicationAttribute.value) &&
        Objects.equals(tenant, applicationAttribute.tenant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationId, name, value, tenant);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationAttributeDTO {\n");
    
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    tenant: ").append(toIndentedString(tenant)).append("\n");
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

