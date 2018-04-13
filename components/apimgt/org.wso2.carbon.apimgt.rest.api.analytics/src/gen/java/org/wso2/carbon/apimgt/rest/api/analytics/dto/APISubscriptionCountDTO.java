package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * APISubscriptionCountDTO
 */
public class APISubscriptionCountDTO   {
  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("version")
  private String version = null;

  @SerializedName("provider")
  private String provider = null;

  @SerializedName("count")
  private Integer count = null;

  public APISubscriptionCountDTO id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Subscription UUID
   * @return id
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "Subscription UUID")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public APISubscriptionCountDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "CalculatorAPI", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public APISubscriptionCountDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Get version
   * @return version
  **/
  @ApiModelProperty(example = "1.0.0", value = "")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public APISubscriptionCountDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

   /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   * @return provider
  **/
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public APISubscriptionCountDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * No of subscriptions. 
   * @return count
  **/
  @ApiModelProperty(example = "4", value = "No of subscriptions. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISubscriptionCountDTO apISubscriptionCount = (APISubscriptionCountDTO) o;
    return Objects.equals(this.id, apISubscriptionCount.id) &&
        Objects.equals(this.name, apISubscriptionCount.name) &&
        Objects.equals(this.version, apISubscriptionCount.version) &&
        Objects.equals(this.provider, apISubscriptionCount.provider) &&
        Objects.equals(this.count, apISubscriptionCount.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, version, provider, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISubscriptionCountDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

