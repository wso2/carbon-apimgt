package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SubscriberDTO   {
  
    private Integer tenantId = null;
    private Integer subscriberId = null;

  /**
   **/
  public SubscriberDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
  }

  /**
   **/
  public SubscriberDTO subscriberId(Integer subscriberId) {
    this.subscriberId = subscriberId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscriberId")
  public Integer getSubscriberId() {
    return subscriberId;
  }
  public void setSubscriberId(Integer subscriberId) {
    this.subscriberId = subscriberId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriberDTO subscriber = (SubscriberDTO) o;
    return Objects.equals(tenantId, subscriber.tenantId) &&
        Objects.equals(subscriberId, subscriber.subscriberId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, subscriberId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriberDTO {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    subscriberId: ").append(toIndentedString(subscriberId)).append("\n");
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

