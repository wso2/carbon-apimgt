package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ComplianceEvalInputDTO   {
  

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

    @XmlType(name="GovernableStatesEnum")
    @XmlEnum(String.class)
    public enum GovernableStatesEnum {
        API_CREATE("API_CREATE"),
        API_UPDATE("API_UPDATE"),
        API_DEPLOY("API_DEPLOY"),
        API_PUBLISH("API_PUBLISH");
        private String value;

        GovernableStatesEnum (String v) {
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
        public static GovernableStatesEnum fromValue(String v) {
            for (GovernableStatesEnum b : GovernableStatesEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private List<GovernableStatesEnum> governableStates = new ArrayList<GovernableStatesEnum>();
    private File apiSchema = null;
    private String label = null;

  /**
   * The type of artifact that the ruleset validates.
   **/
  public ComplianceEvalInputDTO artifactType(ArtifactTypeEnum artifactType) {
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
   * List of states at which the governance policy should be enforced.
   **/
  public ComplianceEvalInputDTO governableStates(List<GovernableStatesEnum> governableStates) {
    this.governableStates = governableStates;
    return this;
  }

  
  @ApiModelProperty(value = "List of states at which the governance policy should be enforced.")
  @JsonProperty("governableStates")
  public List<GovernableStatesEnum> getGovernableStates() {
    return governableStates;
  }
  public void setGovernableStates(List<GovernableStatesEnum> governableStates) {
    this.governableStates = governableStates;
  }

  /**
   * ZIP or TXT file containing the API definition to validate
   **/
  public ComplianceEvalInputDTO apiSchema(File apiSchema) {
    this.apiSchema = apiSchema;
    return this;
  }

  
  @ApiModelProperty(value = "ZIP or TXT file containing the API definition to validate")
  @JsonProperty("apiSchema")
  public File getApiSchema() {
    return apiSchema;
  }
  public void setApiSchema(File apiSchema) {
    this.apiSchema = apiSchema;
  }

  /**
   **/
  public ComplianceEvalInputDTO label(String label) {
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
    ComplianceEvalInputDTO complianceEvalInput = (ComplianceEvalInputDTO) o;
    return Objects.equals(artifactType, complianceEvalInput.artifactType) &&
        Objects.equals(governableStates, complianceEvalInput.governableStates) &&
        Objects.equals(apiSchema, complianceEvalInput.apiSchema) &&
        Objects.equals(label, complianceEvalInput.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, governableStates, apiSchema, label);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComplianceEvalInputDTO {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    governableStates: ").append(toIndentedString(governableStates)).append("\n");
    sb.append("    apiSchema: ").append(toIndentedString(apiSchema)).append("\n");
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

