package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceKeyManagerScopeDTO;
import javax.validation.constraints.*;

/**
 * Ruleset binding configured for a Devportal Governance template.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Ruleset binding configured for a Devportal Governance template.")

public class DevportalGovernanceRulesetBindingDTO   {

    private String bindingId = null;
    private String rulesetId = null;
    private String rulesetName = null;
    private String rulesetDescription = null;
    private String documentationLink = null;
    private String ruleType = null;
    private String artifactType = null;
    private Integer bindingOrder = 0;
    private List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopes = new ArrayList<DevportalGovernanceKeyManagerScopeDTO>();

  /**
   * UUID of the ruleset binding.
   **/
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

  /**
   * UUID of the ruleset bound to the template.
   **/
  public DevportalGovernanceRulesetBindingDTO rulesetId(String rulesetId) {
    this.rulesetId = rulesetId;
    return this;
  }


  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", required = true, value = "UUID of the ruleset bound to the template.")
  @JsonProperty("rulesetId")
  @NotNull
  public String getRulesetId() {
    return rulesetId;
  }
  public void setRulesetId(String rulesetId) {
    this.rulesetId = rulesetId;
  }

  /**
   * Display name of the bound ruleset.
   **/
  public DevportalGovernanceRulesetBindingDTO rulesetName(String rulesetName) {
    this.rulesetName = rulesetName;
    return this;
  }


  @ApiModelProperty(example = "Partner Application Rules", value = "Display name of the bound ruleset.")
  @JsonProperty("rulesetName")
  public String getRulesetName() {
    return rulesetName;
  }
  public void setRulesetName(String rulesetName) {
    this.rulesetName = rulesetName;
  }

  /**
   * Description of the bound ruleset.
   **/
  public DevportalGovernanceRulesetBindingDTO rulesetDescription(String rulesetDescription) {
    this.rulesetDescription = rulesetDescription;
    return this;
  }


  @ApiModelProperty(example = "Rules applied during application creation.", value = "Description of the bound ruleset.")
  @JsonProperty("rulesetDescription")
  public String getRulesetDescription() {
    return rulesetDescription;
  }
  public void setRulesetDescription(String rulesetDescription) {
    this.rulesetDescription = rulesetDescription;
  }

  /**
   * Documentation link of the bound ruleset.
   **/
  public DevportalGovernanceRulesetBindingDTO documentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
    return this;
  }


  @ApiModelProperty(example = "https://docs.example.com/governance/rules", value = "Documentation link of the bound ruleset.")
  @JsonProperty("documentationLink")
  public String getDocumentationLink() {
    return documentationLink;
  }
  public void setDocumentationLink(String documentationLink) {
    this.documentationLink = documentationLink;
  }

  /**
   * Rule type of the bound ruleset.
   **/
  public DevportalGovernanceRulesetBindingDTO ruleType(String ruleType) {
    this.ruleType = ruleType;
    return this;
  }


  @ApiModelProperty(example = "APP_INFO", value = "Rule type of the bound ruleset.")
  @JsonProperty("ruleType")
  public String getRuleType() {
    return ruleType;
  }
  public void setRuleType(String ruleType) {
    this.ruleType = ruleType;
  }

  /**
   * Artifact type of the bound ruleset.
   **/
  public DevportalGovernanceRulesetBindingDTO artifactType(String artifactType) {
    this.artifactType = artifactType;
    return this;
  }


  @ApiModelProperty(example = "APPLICATION", value = "Artifact type of the bound ruleset.")
  @JsonProperty("artifactType")
  public String getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(String artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * Evaluation order for bound rulesets.
   **/
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

  /**
   * Key managers that scope this ruleset binding.
   **/
  public DevportalGovernanceRulesetBindingDTO keyManagerScopes(List<DevportalGovernanceKeyManagerScopeDTO> keyManagerScopes) {
    this.keyManagerScopes = keyManagerScopes;
    return this;
  }


  @ApiModelProperty(value = "Key managers that scope this ruleset binding.")
      @Valid
  @JsonProperty("keyManagerScopes")
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
    DevportalGovernanceRulesetBindingDTO devportalGovernanceRulesetBinding = (DevportalGovernanceRulesetBindingDTO) o;
    return Objects.equals(bindingId, devportalGovernanceRulesetBinding.bindingId) &&
        Objects.equals(rulesetId, devportalGovernanceRulesetBinding.rulesetId) &&
        Objects.equals(rulesetName, devportalGovernanceRulesetBinding.rulesetName) &&
        Objects.equals(rulesetDescription, devportalGovernanceRulesetBinding.rulesetDescription) &&
        Objects.equals(documentationLink, devportalGovernanceRulesetBinding.documentationLink) &&
        Objects.equals(ruleType, devportalGovernanceRulesetBinding.ruleType) &&
        Objects.equals(artifactType, devportalGovernanceRulesetBinding.artifactType) &&
        Objects.equals(bindingOrder, devportalGovernanceRulesetBinding.bindingOrder) &&
        Objects.equals(keyManagerScopes, devportalGovernanceRulesetBinding.keyManagerScopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bindingId, rulesetId, rulesetName, rulesetDescription, documentationLink, ruleType, artifactType, bindingOrder, keyManagerScopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DevportalGovernanceRulesetBindingDTO {\n");

    sb.append("    bindingId: ").append(toIndentedString(bindingId)).append("\n");
    sb.append("    rulesetId: ").append(toIndentedString(rulesetId)).append("\n");
    sb.append("    rulesetName: ").append(toIndentedString(rulesetName)).append("\n");
    sb.append("    rulesetDescription: ").append(toIndentedString(rulesetDescription)).append("\n");
    sb.append("    documentationLink: ").append(toIndentedString(documentationLink)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    bindingOrder: ").append(toIndentedString(bindingOrder)).append("\n");
    sb.append("    keyManagerScopes: ").append(toIndentedString(keyManagerScopes)).append("\n");
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
