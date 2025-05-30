package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class PolicyEvalInputDTO   {
  

          @XmlType(name="ArtifactTypeEnum")
    @XmlEnum(String.class)
    public enum ArtifactTypeEnum {
        REST_API("REST_API"),
        ASYNC_API("ASYNC_API");
        private String value;

        ArtifactTypeEnum (String v) {
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
        public static ArtifactTypeEnum fromValue(String v) {
            for (ArtifactTypeEnum b : ArtifactTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private ArtifactTypeEnum artifactType = null;

          @XmlType(name="RuleCategoryEnum")
    @XmlEnum(String.class)
    public enum RuleCategoryEnum {
        SPECTRAL("SPECTRAL");
        private String value;

        RuleCategoryEnum (String v) {
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
        public static RuleCategoryEnum fromValue(String v) {
            for (RuleCategoryEnum b : RuleCategoryEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleCategoryEnum ruleCategory = RuleCategoryEnum.SPECTRAL;

          @XmlType(name="RuleTypeEnum")
    @XmlEnum(String.class)
    public enum RuleTypeEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        API_DOCUMENTATION("API_DOCUMENTATION");
        private String value;

        RuleTypeEnum (String v) {
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
        public static RuleTypeEnum fromValue(String v) {
            for (RuleTypeEnum b : RuleTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleTypeEnum ruleType = null;
    private File file = null;
    private String label = null;

  /**
   * The type of artifact that the ruleset validates.
   **/
  public PolicyEvalInputDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(example = "REST_API", value = "The type of artifact that the ruleset validates.")
  @JsonProperty("artifactType")
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * Category the rules included in ruleset.
   **/
  public PolicyEvalInputDTO ruleCategory(RuleCategoryEnum ruleCategory) {
    this.ruleCategory = ruleCategory;
    return this;
  }

  
  @ApiModelProperty(example = "SPECTRAL", value = "Category the rules included in ruleset.")
  @JsonProperty("ruleCategory")
  public RuleCategoryEnum getRuleCategory() {
    return ruleCategory;
  }
  public void setRuleCategory(RuleCategoryEnum ruleCategory) {
    this.ruleCategory = ruleCategory;
  }

  /**
   * Context or area to which the ruleset applies.
   **/
  public PolicyEvalInputDTO ruleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", value = "Context or area to which the ruleset applies.")
  @JsonProperty("ruleType")
  public RuleTypeEnum getRuleType() {
    return ruleType;
  }
  public void setRuleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
  }

  /**
   * ZIP or TXT file
   **/
  public PolicyEvalInputDTO file(File file) {
    this.file = file;
    return this;
  }

  
  @ApiModelProperty(value = "ZIP or TXT file")
  @JsonProperty("file")
  public File getFile() {
    return file;
  }
  public void setFile(File file) {
    this.file = file;
  }

  /**
   **/
  public PolicyEvalInputDTO label(String label) {
    this.label = label;
    return this;
  }

  
  @ApiModelProperty(example = "Finance", value = "")
  @JsonProperty("label")
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyEvalInputDTO policyEvalInput = (PolicyEvalInputDTO) o;
    return Objects.equals(artifactType, policyEvalInput.artifactType) &&
        Objects.equals(ruleCategory, policyEvalInput.ruleCategory) &&
        Objects.equals(ruleType, policyEvalInput.ruleType) &&
        Objects.equals(file, policyEvalInput.file) &&
        Objects.equals(label, policyEvalInput.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, ruleCategory, ruleType, file, label);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyEvalInputDTO {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    ruleCategory: ").append(toIndentedString(ruleCategory)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
    sb.append("    file: ").append(toIndentedString(file)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
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

