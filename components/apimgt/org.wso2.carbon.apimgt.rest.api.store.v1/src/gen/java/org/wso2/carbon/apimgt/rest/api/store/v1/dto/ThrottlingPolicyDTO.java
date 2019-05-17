package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class ThrottlingPolicyDTO {

    @NotNull
    private String name = null;

    private String description = null;

    public enum PolicyLevelEnum {
        application, subscription,
    }

    ;

    private PolicyLevelEnum policyLevel = null;

    private Map<String, String> attributes = new HashMap<String, String>();

    @NotNull
    private Long requestCount = null;

    @NotNull
    private Long unitTime = null;

    public enum TierPlanEnum {
        FREE, COMMERCIAL,
    }

    ;
    @NotNull
    private TierPlanEnum tierPlan = null;

    @NotNull
    private Boolean stopOnQuotaReach = null;

    private ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissions = null;

    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("name")
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("policyLevel")
    public PolicyLevelEnum getPolicyLevel() {

        return policyLevel;
    }

    public void setPolicyLevel(PolicyLevelEnum policyLevel) {

        this.policyLevel = policyLevel;
    }

    /**
     * Custom attributes added to the throttling policy\n
     **/
    @ApiModelProperty(value = "Custom attributes added to the throttling policy\n")
    @JsonProperty("attributes")
    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {

        this.attributes = attributes;
    }

    /**
     * Maximum number of requests which can be sent within a provided unit time\n
     **/
    @ApiModelProperty(required = true, value = "Maximum number of requests which can be sent within a provided unit time\n")
    @JsonProperty("requestCount")
    public Long getRequestCount() {

        return requestCount;
    }

    public void setRequestCount(Long requestCount) {

        this.requestCount = requestCount;
    }

    /**
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("unitTime")
    public Long getUnitTime() {

        return unitTime;
    }

    public void setUnitTime(Long unitTime) {

        this.unitTime = unitTime;
    }

    /**
     * This attribute declares whether this tier is available under commercial or free\n
     **/
    @ApiModelProperty(required = true, value = "This attribute declares whether this tier is available under commercial or free\n")
    @JsonProperty("tierPlan")
    public TierPlanEnum getTierPlan() {

        return tierPlan;
    }

    public void setTierPlan(TierPlanEnum tierPlan) {

        this.tierPlan = tierPlan;
    }

    /**
     * If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n
     **/
    @ApiModelProperty(required = true, value = "If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n")
    @JsonProperty("stopOnQuotaReach")
    public Boolean getStopOnQuotaReach() {

        return stopOnQuotaReach;
    }

    public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {

        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("throttlingPolicyPermissions")
    public ThrottlingPolicyPermissionInfoDTO getThrottlingPolicyPermissions() {

        return throttlingPolicyPermissions;
    }

    public void setThrottlingPolicyPermissions(ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissions) {

        this.throttlingPolicyPermissions = throttlingPolicyPermissions;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ThrottlingPolicyDTO {\n");

        sb.append("  name: ").append(name).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  policyLevel: ").append(policyLevel).append("\n");
        sb.append("  attributes: ").append(attributes).append("\n");
        sb.append("  requestCount: ").append(requestCount).append("\n");
        sb.append("  unitTime: ").append(unitTime).append("\n");
        sb.append("  tierPlan: ").append(tierPlan).append("\n");
        sb.append("  stopOnQuotaReach: ").append(stopOnQuotaReach).append("\n");
        sb.append("  throttlingPolicyPermissions: ").append(throttlingPolicyPermissions).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
