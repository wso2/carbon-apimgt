package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.internal.service.dto.GraphQLQueryDTO;
import org.wso2.carbon.apimgt.internal.service.dto.PolicyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class SubscriptionPolicyDTO   {
  
    private Integer id = null;
    private Integer tenantId = null;
    private String tenantDomain = null;
    private String name = null;
    private String quotaType = null;
    private Integer graphQLMaxComplexity = null;
    private Integer graphQLMaxDepth = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private Boolean stopOnQuotaReach = null;
    private ThrottleLimitDTO defaultLimit = null;

  /**
   **/
  public SubscriptionPolicyDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public SubscriptionPolicyDTO tenantId(Integer tenantId) {
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
  public SubscriptionPolicyDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public SubscriptionPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public SubscriptionPolicyDTO quotaType(String quotaType) {
    this.quotaType = quotaType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("quotaType")
  public String getQuotaType() {
    return quotaType;
  }
  public void setQuotaType(String quotaType) {
    this.quotaType = quotaType;
  }

  /**
   * Maximum Complexity of the GraphQL query
   **/
  public SubscriptionPolicyDTO graphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
    return this;
  }

  
  @ApiModelProperty(example = "400", value = "Maximum Complexity of the GraphQL query")
  @JsonProperty("graphQLMaxComplexity")
  public Integer getGraphQLMaxComplexity() {
    return graphQLMaxComplexity;
  }
  public void setGraphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
  }

  /**
   * Maximum Depth of the GraphQL query
   **/
  public SubscriptionPolicyDTO graphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Maximum Depth of the GraphQL query")
  @JsonProperty("graphQLMaxDepth")
  public Integer getGraphQLMaxDepth() {
    return graphQLMaxDepth;
  }
  public void setGraphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
  }

  /**
   **/
  public SubscriptionPolicyDTO rateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rateLimitCount")
  public Integer getRateLimitCount() {
    return rateLimitCount;
  }
  public void setRateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
  }

  /**
   **/
  public SubscriptionPolicyDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  /**
   **/
  public SubscriptionPolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stopOnQuotaReach")
  public Boolean isStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   **/
  public SubscriptionPolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionPolicyDTO subscriptionPolicy = (SubscriptionPolicyDTO) o;
    return Objects.equals(id, subscriptionPolicy.id) &&
        Objects.equals(tenantId, subscriptionPolicy.tenantId) &&
        Objects.equals(tenantDomain, subscriptionPolicy.tenantDomain) &&
        Objects.equals(name, subscriptionPolicy.name) &&
        Objects.equals(quotaType, subscriptionPolicy.quotaType) &&
        Objects.equals(graphQLMaxComplexity, subscriptionPolicy.graphQLMaxComplexity) &&
        Objects.equals(graphQLMaxDepth, subscriptionPolicy.graphQLMaxDepth) &&
        Objects.equals(rateLimitCount, subscriptionPolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionPolicy.rateLimitTimeUnit) &&
        Objects.equals(stopOnQuotaReach, subscriptionPolicy.stopOnQuotaReach) &&
        Objects.equals(defaultLimit, subscriptionPolicy.defaultLimit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, tenantDomain, name, quotaType, graphQLMaxComplexity, graphQLMaxDepth, rateLimitCount, rateLimitTimeUnit, stopOnQuotaReach, defaultLimit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionPolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    quotaType: ").append(toIndentedString(quotaType)).append("\n");
    sb.append("    graphQLMaxComplexity: ").append(toIndentedString(graphQLMaxComplexity)).append("\n");
    sb.append("    graphQLMaxDepth: ").append(toIndentedString(graphQLMaxDepth)).append("\n");
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

