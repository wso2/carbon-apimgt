package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;


import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class APICategoryDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private Integer numberOfAPIs = null;

  /**
   **/
  public APICategoryDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APICategoryDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Finance", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APICategoryDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Finance related APIs", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public APICategoryDTO numberOfAPIs(Integer numberOfAPIs) {
    this.numberOfAPIs = numberOfAPIs;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("numberOfAPIs")
  public Integer getNumberOfAPIs() {
    return numberOfAPIs;
  }
  public void setNumberOfAPIs(Integer numberOfAPIs) {
    this.numberOfAPIs = numberOfAPIs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APICategoryDTO apICategory = (APICategoryDTO) o;
    return Objects.equals(id, apICategory.id) &&
        Objects.equals(name, apICategory.name) &&
        Objects.equals(description, apICategory.description) &&
        Objects.equals(numberOfAPIs, apICategory.numberOfAPIs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, numberOfAPIs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICategoryDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    numberOfAPIs: ").append(toIndentedString(numberOfAPIs)).append("\n");
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

