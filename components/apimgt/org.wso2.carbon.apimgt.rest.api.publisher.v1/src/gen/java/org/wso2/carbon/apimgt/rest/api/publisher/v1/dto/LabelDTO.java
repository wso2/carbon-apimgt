package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class LabelDTO   {
  
    private String labelId = null;
    private String name = null;
    private String type = null;
    private List<String> accessUrls = new ArrayList<>();

  /**
   **/
  public LabelDTO labelId(String labelId) {
    this.labelId = labelId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("labelId")
  @NotNull
  public String getLabelId() {
    return labelId;
  }
  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  /**
   **/
  public LabelDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
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
  public LabelDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public LabelDTO accessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("access_urls")
  @NotNull
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
    return Objects.equals(labelId, label.labelId) &&
        Objects.equals(name, label.name) &&
        Objects.equals(type, label.type) &&
        Objects.equals(accessUrls, label.accessUrls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labelId, name, type, accessUrls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelDTO {\n");
    
    sb.append("    labelId: ").append(toIndentedString(labelId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

