package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SequencesDTO   {
  
    private List<String> deployedSequences = new ArrayList<>();
    private List<String> notdeployedSequences = new ArrayList<>();

  /**
   * The sequences which has been deployed in the gateway 
   **/
  public SequencesDTO deployedSequences(List<String> deployedSequences) {
    this.deployedSequences = deployedSequences;
    return this;
  }

  
  @ApiModelProperty(value = "The sequences which has been deployed in the gateway ")
  @JsonProperty("deployedSequences")
  public List<String> getDeployedSequences() {
    return deployedSequences;
  }
  public void setDeployedSequences(List<String> deployedSequences) {
    this.deployedSequences = deployedSequences;
  }

  /**
   * The sequences which has not been deployed in the gateway 
   **/
  public SequencesDTO notdeployedSequences(List<String> notdeployedSequences) {
    this.notdeployedSequences = notdeployedSequences;
    return this;
  }

  
  @ApiModelProperty(value = "The sequences which has not been deployed in the gateway ")
  @JsonProperty("notdeployedSequences")
  public List<String> getNotdeployedSequences() {
    return notdeployedSequences;
  }
  public void setNotdeployedSequences(List<String> notdeployedSequences) {
    this.notdeployedSequences = notdeployedSequences;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SequencesDTO sequences = (SequencesDTO) o;
    return Objects.equals(deployedSequences, sequences.deployedSequences) &&
        Objects.equals(notdeployedSequences, sequences.notdeployedSequences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployedSequences, notdeployedSequences);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SequencesDTO {\n");
    
    sb.append("    deployedSequences: ").append(toIndentedString(deployedSequences)).append("\n");
    sb.append("    notdeployedSequences: ").append(toIndentedString(notdeployedSequences)).append("\n");
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

