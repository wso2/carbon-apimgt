package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class FullNameDTO   {
  
    private String firstName = null;
    private String lastName = null;
    private String fullName = null;

  /**
   **/
  public FullNameDTO firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  
  @ApiModelProperty(example = "John", value = "")
  @JsonProperty("firstName")
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   **/
  public FullNameDTO lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  
  @ApiModelProperty(example = "David", value = "")
  @JsonProperty("LastName")
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   **/
  public FullNameDTO fullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  
  @ApiModelProperty(example = "John David", value = "")
  @JsonProperty("fullName")
  public String getFullName() {
    return fullName;
  }
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FullNameDTO fullName = (FullNameDTO) o;
    return Objects.equals(firstName, fullName.firstName) &&
        Objects.equals(lastName, fullName.lastName) &&
        Objects.equals(fullName, fullName.fullName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, fullName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FullNameDTO {\n");
    
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    fullName: ").append(toIndentedString(fullName)).append("\n");
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

