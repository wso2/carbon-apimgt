package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import javax.validation.constraints.*;

/**
 * Adherence status of a policy with ruleset details.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Adherence status of a policy with ruleset details.")

public class PolicyAdherenceWithRulesetsDTO   {
  
    private String policyId = null;
    private String policyName = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        FOLLOWED("FOLLOWED"),
        VIOLATED("VIOLATED");
        private String value;

        StatusEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private StatusEnum status = null;
    private List<RulesetValidationResultDTO> rulesetValidationResults = new ArrayList<RulesetValidationResultDTO>();

  /**
   * UUID of the policy.
   **/
  public PolicyAdherenceWithRulesetsDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the policy.")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   * Name of the policy.
   **/
  public PolicyAdherenceWithRulesetsDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(example = "Policy1", value = "Name of the policy.")
  @JsonProperty("policyName")
  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  /**
   * Status of the policy&#39;s governance compliance.
   **/
  public PolicyAdherenceWithRulesetsDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Status of the policy's governance compliance.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * List of ruleset validation information.
   **/
  public PolicyAdherenceWithRulesetsDTO rulesetValidationResults(List<RulesetValidationResultDTO> rulesetValidationResults) {
    this.rulesetValidationResults = rulesetValidationResults;
    return this;
  }

  
  @ApiModelProperty(value = "List of ruleset validation information.")
      @Valid
  @JsonProperty("rulesetValidationResults")
  public List<RulesetValidationResultDTO> getRulesetValidationResults() {
    return rulesetValidationResults;
  }
  public void setRulesetValidationResults(List<RulesetValidationResultDTO> rulesetValidationResults) {
    this.rulesetValidationResults = rulesetValidationResults;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyAdherenceWithRulesetsDTO policyAdherenceWithRulesets = (PolicyAdherenceWithRulesetsDTO) o;
    return Objects.equals(policyId, policyAdherenceWithRulesets.policyId) &&
        Objects.equals(policyName, policyAdherenceWithRulesets.policyName) &&
        Objects.equals(status, policyAdherenceWithRulesets.status) &&
        Objects.equals(rulesetValidationResults, policyAdherenceWithRulesets.rulesetValidationResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, policyName, status, rulesetValidationResults);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyAdherenceWithRulesetsDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    rulesetValidationResults: ").append(toIndentedString(rulesetValidationResults)).append("\n");
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

