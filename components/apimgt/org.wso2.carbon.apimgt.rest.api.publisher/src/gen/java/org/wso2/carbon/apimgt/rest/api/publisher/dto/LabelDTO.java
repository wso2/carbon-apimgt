package org.wso2.carbon.apimgt.rest.api.publisher.dto;


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
  @SerializedName("labelId")
  private String labelId = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("type")
  private String type = null;

  @SerializedName("access_urls")
  private List<String> accessUrls = new ArrayList<String>();

  public LabelDTO labelId(String labelId) {
    this.labelId = labelId;
    return this;
  }

   /**
   * Get labelId
   * @return labelId
  **/
  @ApiModelProperty(required = true, value = "")
  public String getLabelId() {
    return labelId;
  }

  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  public LabelDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LabelDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(required = true, value = "")
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
    return Objects.equals(this.labelId, label.labelId) &&
        Objects.equals(this.name, label.name) &&
        Objects.equals(this.type, label.type) &&
        Objects.equals(this.accessUrls, label.accessUrls);
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

