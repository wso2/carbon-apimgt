package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LabelDTO
 */
public class LabelDTO   {
  @SerializedName("labelUUID")
  private String labelUUID = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("accessUrls")
  private List<String> accessUrls = new ArrayList<String>();

  public LabelDTO labelUUID(String labelUUID) {
    this.labelUUID = labelUUID;
    return this;
  }

   /**
   * Get labelUUID
   * @return labelUUID
  **/
  @ApiModelProperty(value = "")
  public String getLabelUUID() {
    return labelUUID;
  }

  public void setLabelUUID(String labelUUID) {
    this.labelUUID = labelUUID;
  }

  public LabelDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Public", required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LabelDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(example = "Label to use for public APIs", value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LabelDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * the  type of the label example: \"Gateway\" 
   * @return type
  **/
  @ApiModelProperty(required = true, value = "the  type of the label example: \"Gateway\" ")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public LabelDTO accessUrls(List<String> accessUrls) {
    this.accessUrls = accessUrls;
    return this;
  }

  public LabelDTO addAccessUrlsItem(String accessUrlsItem) {
    this.accessUrls.add(accessUrlsItem);
    return this;
  }

   /**
   * Get accessUrls
   * @return accessUrls
  **/
  @ApiModelProperty(value = "")
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
    return Objects.equals(this.labelUUID, label.labelUUID) &&
        Objects.equals(this.name, label.name) &&
        Objects.equals(this.description, label.description) &&
        Objects.equals(this.type, label.type) &&
        Objects.equals(this.accessUrls, label.accessUrls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labelUUID, name, description, type, accessUrls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelDTO {\n");
    
    sb.append("    labelUUID: ").append(toIndentedString(labelUUID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

