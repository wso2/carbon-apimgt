package org.wso2.carbon.apimgt.rest.api.core.dto;


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
  @SerializedName("name")
  private String name = null;

  @SerializedName("access_urls")
  private List<String> accessUrls = new ArrayList<String>();

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
    return Objects.equals(this.name, label.name) &&
        Objects.equals(this.accessUrls, label.accessUrls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, accessUrls);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

