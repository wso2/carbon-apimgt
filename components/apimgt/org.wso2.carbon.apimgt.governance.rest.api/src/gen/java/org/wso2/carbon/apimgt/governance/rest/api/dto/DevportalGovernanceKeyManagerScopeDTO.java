package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * Key Manager scope for a Devportal Governance template ruleset binding.
 **/
@ApiModel(description = "Key Manager scope for a Devportal Governance template ruleset binding.")
public class DevportalGovernanceKeyManagerScopeDTO {

    private String keyManagerUuid = null;
    private String organization = null;

    public DevportalGovernanceKeyManagerScopeDTO keyManagerUuid(String keyManagerUuid) {

        this.keyManagerUuid = keyManagerUuid;
        return this;
    }

    @ApiModelProperty(example = "6f9dcb8a-20f4-421d-9b45-6b89f3f6e8b4", required = true,
            value = "UUID of the Key Manager.")
    @JsonProperty("keyManagerUuid")
    @NotNull
    public String getKeyManagerUuid() {

        return keyManagerUuid;
    }

    public void setKeyManagerUuid(String keyManagerUuid) {

        this.keyManagerUuid = keyManagerUuid;
    }

    public DevportalGovernanceKeyManagerScopeDTO organization(String organization) {

        this.organization = organization;
        return this;
    }

    @ApiModelProperty(example = "carbon.super", value = "Organization associated with the Key Manager scope.")
    @JsonProperty("organization")
    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DevportalGovernanceKeyManagerScopeDTO that = (DevportalGovernanceKeyManagerScopeDTO) o;
        return Objects.equals(keyManagerUuid, that.keyManagerUuid) &&
                Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {

        return Objects.hash(keyManagerUuid, organization);
    }
}
