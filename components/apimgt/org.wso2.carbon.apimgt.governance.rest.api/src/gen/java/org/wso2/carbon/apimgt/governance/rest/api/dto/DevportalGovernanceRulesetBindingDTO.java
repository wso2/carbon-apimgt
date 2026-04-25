package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Ruleset binding configured for a Devportal Governance template.
 **/
@ApiModel(description = "Ruleset binding configured for a Devportal Governance template.")
public class DevportalGovernanceRulesetBindingDTO {

    private String bindingId = null;
    private String rulesetId = null;
    private Integer bindingOrder = null;
    private List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopes =
            new ArrayList<DevportalGovernanceKeyManagerScopeDTO>();

    public DevportalGovernanceRulesetBindingDTO bindingId(String bindingId) {

        this.bindingId = bindingId;
        return this;
    }

    @ApiModelProperty(example = "1e42a46d-73e6-4035-9d0e-cfe92fb1a7c8", value = "UUID of the ruleset binding.")
    @JsonProperty("bindingId")
    public String getBindingId() {

        return bindingId;
    }

    public void setBindingId(String bindingId) {

        this.bindingId = bindingId;
    }

    public DevportalGovernanceRulesetBindingDTO rulesetId(String rulesetId) {

        this.rulesetId = rulesetId;
        return this;
    }

    @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", required = true,
            value = "UUID of the ruleset bound to the template.")
    @JsonProperty("rulesetId")
    @NotNull
    public String getRulesetId() {

        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {

        this.rulesetId = rulesetId;
    }

    public DevportalGovernanceRulesetBindingDTO bindingOrder(Integer bindingOrder) {

        this.bindingOrder = bindingOrder;
        return this;
    }

    @ApiModelProperty(example = "0", value = "Evaluation order for bound rulesets.")
    @JsonProperty("bindingOrder")
    public Integer getBindingOrder() {

        return bindingOrder;
    }

    public void setBindingOrder(Integer bindingOrder) {

        this.bindingOrder = bindingOrder;
    }

    public DevportalGovernanceRulesetBindingDTO keyManagerScopes(
            List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopes) {

        this.keyManagerScopes = keyManagerScopes;
        return this;
    }

    @ApiModelProperty(value = "Key managers that scope this ruleset binding.")
    @JsonProperty("keyManagerScopes")
    @Valid
    public List<DevportalGovernanceKeyManagerScopeDTO> getKeyManagerScopes() {

        return keyManagerScopes;
    }

    public void setKeyManagerScopes(List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopes) {

        this.keyManagerScopes = keyManagerScopes;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevportalGovernanceRulesetBindingDTO that = (DevportalGovernanceRulesetBindingDTO) o;
        return Objects.equals(bindingId, that.bindingId) &&
                Objects.equals(rulesetId, that.rulesetId) &&
                Objects.equals(bindingOrder, that.bindingOrder) &&
                Objects.equals(keyManagerScopes, that.keyManagerScopes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(bindingId, rulesetId, bindingOrder, keyManagerScopes);
    }
}
