package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.EndpointsDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.LocalEntryDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.SequencesDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APIArtifactDTO   {
  
    private Integer count = null;
    private EndpointsDTO endpoints = null;
    private LocalEntryDTO localEntries = null;
    private SequencesDTO sequences = null;

  /**
   * Number of Artifacts Returned 
   **/
  public APIArtifactDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Artifacts Returned ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public APIArtifactDTO endpoints(EndpointsDTO endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("endpoints")
  public EndpointsDTO getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(EndpointsDTO endpoints) {
    this.endpoints = endpoints;
  }

  /**
   **/
  public APIArtifactDTO localEntries(LocalEntryDTO localEntries) {
    this.localEntries = localEntries;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("localEntries")
  public LocalEntryDTO getLocalEntries() {
    return localEntries;
  }
  public void setLocalEntries(LocalEntryDTO localEntries) {
    this.localEntries = localEntries;
  }

  /**
   **/
  public APIArtifactDTO sequences(SequencesDTO sequences) {
    this.sequences = sequences;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sequences")
  public SequencesDTO getSequences() {
    return sequences;
  }
  public void setSequences(SequencesDTO sequences) {
    this.sequences = sequences;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIArtifactDTO apIArtifact = (APIArtifactDTO) o;
    return Objects.equals(count, apIArtifact.count) &&
        Objects.equals(endpoints, apIArtifact.endpoints) &&
        Objects.equals(localEntries, apIArtifact.localEntries) &&
        Objects.equals(sequences, apIArtifact.sequences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, endpoints, localEntries, sequences);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIArtifactDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
    sb.append("    localEntries: ").append(toIndentedString(localEntries)).append("\n");
    sb.append("    sequences: ").append(toIndentedString(sequences)).append("\n");
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

