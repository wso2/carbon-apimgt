package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class ExtendedSubscriptionDTO   {
  
    private String subscriptionId = null;
    private ApplicationDTO applicationInfo = null;
    private String policy = null;

@XmlType(name="SubscriptionStatusEnum")
@XmlEnum(String.class)
public enum SubscriptionStatusEnum {

    @XmlEnumValue("BLOCKED") BLOCKED(String.valueOf("BLOCKED")), @XmlEnumValue("PROD_ONLY_BLOCKED") PROD_ONLY_BLOCKED(String.valueOf("PROD_ONLY_BLOCKED")), @XmlEnumValue("UNBLOCKED") UNBLOCKED(String.valueOf("UNBLOCKED")), @XmlEnumValue("ON_HOLD") ON_HOLD(String.valueOf("ON_HOLD")), @XmlEnumValue("REJECTED") REJECTED(String.valueOf("REJECTED"));


    private String value;

    SubscriptionStatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SubscriptionStatusEnum fromValue(String v) {
        for (SubscriptionStatusEnum b : SubscriptionStatusEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private SubscriptionStatusEnum subscriptionStatus = null;
    private String workflowId = null;

  /**
   **/
  public ExtendedSubscriptionDTO subscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", required = true, value = "")
  @JsonProperty("subscriptionId")
  @NotNull
  public String getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  /**
   **/
  public ExtendedSubscriptionDTO applicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("applicationInfo")
  @NotNull
  public ApplicationDTO getApplicationInfo() {
    return applicationInfo;
  }
  public void setApplicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
  }

  /**
   **/
  public ExtendedSubscriptionDTO policy(String policy) {
    this.policy = policy;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", required = true, value = "")
  @JsonProperty("policy")
  @NotNull
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  /**
   **/
  public ExtendedSubscriptionDTO subscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
    return this;
  }

  
  @ApiModelProperty(example = "BLOCKED", required = true, value = "")
  @JsonProperty("subscriptionStatus")
  @NotNull
  public SubscriptionStatusEnum getSubscriptionStatus() {
    return subscriptionStatus;
  }
  public void setSubscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  /**
   **/
  public ExtendedSubscriptionDTO workflowId(String workflowId) {
    this.workflowId = workflowId;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("workflowId")
  public String getWorkflowId() {
    return workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExtendedSubscriptionDTO extendedSubscription = (ExtendedSubscriptionDTO) o;
    return Objects.equals(subscriptionId, extendedSubscription.subscriptionId) &&
        Objects.equals(applicationInfo, extendedSubscription.applicationInfo) &&
        Objects.equals(policy, extendedSubscription.policy) &&
        Objects.equals(subscriptionStatus, extendedSubscription.subscriptionStatus) &&
        Objects.equals(workflowId, extendedSubscription.workflowId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionId, applicationInfo, policy, subscriptionStatus, workflowId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExtendedSubscriptionDTO {\n");
    
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    applicationInfo: ").append(toIndentedString(applicationInfo)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    subscriptionStatus: ").append(toIndentedString(subscriptionStatus)).append("\n");
    sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
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

