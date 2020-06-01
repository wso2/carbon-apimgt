package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class LabelDTO   {
  
    private String id = null;
    private String name = null;
    private String description = null;
    private List<String> accessUrls = new ArrayList<>();

  /**
   **/
  public LabelDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "This is not mandatory.Auto generate by code", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public LabelDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Public", required = true, value = "")
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
  public LabelDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Label to use for public Gateway", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public LabelDTO accessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accessUrls")
  public List<String> getAccessUrls() {
    return accessUrls;
  }
  public void setAccessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelDTO label = (LabelDTO) o;
    return Objects.equals(id, label.id) &&
        Objects.equals(name, label.name) &&
        Objects.equals(description, label.description) &&
        Objects.equals(accessUrls, label.accessUrls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, accessUrls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    accessUrls: ").append(toIndentedString(accessUrls)).append("\n");
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

