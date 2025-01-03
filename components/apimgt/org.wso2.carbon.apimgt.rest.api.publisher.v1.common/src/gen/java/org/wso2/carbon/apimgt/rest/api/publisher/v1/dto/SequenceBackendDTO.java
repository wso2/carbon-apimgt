package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class SequenceBackendDTO   {
  
    private String sequenceId = null;
    private String sequenceName = null;
    private String sequenceType = null;
    private File sequence = null;

  /**
   **/
  public SequenceBackendDTO sequenceId(String sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  
  @ApiModelProperty(example = "943d3002-000c-42d3-a1b9-d6559f8a4d49", value = "")
  @JsonProperty("sequenceId")
  public String getSequenceId() {
    return sequenceId;
  }
  public void setSequenceId(String sequenceId) {
    this.sequenceId = sequenceId;
  }

  /**
   **/
  public SequenceBackendDTO sequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
    return this;
  }

  
  @ApiModelProperty(example = "943d3002-000c-42d3-a1b9-d6559f8a4d49-SANDBOX", value = "")
  @JsonProperty("sequenceName")
  public String getSequenceName() {
    return sequenceName;
  }
  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  /**
   **/
  public SequenceBackendDTO sequenceType(String sequenceType) {
    this.sequenceType = sequenceType;
    return this;
  }

  
  @ApiModelProperty(example = "SANDBOX", value = "")
  @JsonProperty("sequenceType")
  public String getSequenceType() {
    return sequenceType;
  }
  public void setSequenceType(String sequenceType) {
    this.sequenceType = sequenceType;
  }

  /**
   **/
  public SequenceBackendDTO sequence(File sequence) {
    this.sequence = sequence;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sequence")
  public File getSequence() {
    return sequence;
  }
  public void setSequence(File sequence) {
    this.sequence = sequence;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SequenceBackendDTO sequenceBackend = (SequenceBackendDTO) o;
    return Objects.equals(sequenceId, sequenceBackend.sequenceId) &&
        Objects.equals(sequenceName, sequenceBackend.sequenceName) &&
        Objects.equals(sequenceType, sequenceBackend.sequenceType) &&
        Objects.equals(sequence, sequenceBackend.sequence);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequenceId, sequenceName, sequenceType, sequence);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SequenceBackendDTO {\n");
    
    sb.append("    sequenceId: ").append(toIndentedString(sequenceId)).append("\n");
    sb.append("    sequenceName: ").append(toIndentedString(sequenceName)).append("\n");
    sb.append("    sequenceType: ").append(toIndentedString(sequenceType)).append("\n");
    sb.append("    sequence: ").append(toIndentedString(sequence)).append("\n");
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

