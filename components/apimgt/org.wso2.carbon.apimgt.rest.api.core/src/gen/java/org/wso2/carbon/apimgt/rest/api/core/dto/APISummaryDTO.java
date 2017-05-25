package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;
import java.util.Objects;

/**
 * APISummaryDTO
 */
public class APISummaryDTO   {
  @JsonProperty("subscriptions")
  private List<SubscriptionDTO> subscriptions = new ArrayList<SubscriptionDTO>();

  @JsonProperty("resources")
  private List<UriTemplateDTO> resources = new ArrayList<UriTemplateDTO>();

  public APISummaryDTO subscriptions(List<SubscriptionDTO> subscriptions) {
    this.subscriptions = subscriptions;
    return this;
  }

  public APISummaryDTO addSubscriptionsItem(SubscriptionDTO subscriptionsItem) {
    this.subscriptions.add(subscriptionsItem);
    return this;
  }

   /**
   * Get subscriptions
   * @return subscriptions
  **/
  @ApiModelProperty(value = "")
  public List<SubscriptionDTO> getSubscriptions() {
    return subscriptions;
  }

  public void setSubscriptions(List<SubscriptionDTO> subscriptions) {
    this.subscriptions = subscriptions;
  }

  public APISummaryDTO resources(List<UriTemplateDTO> resources) {
    this.resources = resources;
    return this;
  }

  public APISummaryDTO addResourcesItem(UriTemplateDTO resourcesItem) {
    this.resources.add(resourcesItem);
    return this;
  }

   /**
   * Get resources
   * @return resources
  **/
  @ApiModelProperty(value = "")
  public List<UriTemplateDTO> getResources() {
    return resources;
  }

  public void setResources(List<UriTemplateDTO> resources) {
    this.resources = resources;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APISummaryDTO apISummary = (APISummaryDTO) o;
    return Objects.equals(this.subscriptions, apISummary.subscriptions) &&
        Objects.equals(this.resources, apISummary.resources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptions, resources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APISummaryDTO {\n");
    
    sb.append("    subscriptions: ").append(toIndentedString(subscriptions)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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

