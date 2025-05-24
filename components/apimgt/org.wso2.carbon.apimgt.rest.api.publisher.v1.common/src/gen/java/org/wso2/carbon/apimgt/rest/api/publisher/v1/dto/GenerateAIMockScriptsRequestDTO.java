package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GenerateAIMockScriptsRequestDTO   {
  
    private String instructions = null;
    private String script = "No Script.";
    private Map<String, Object> modify = new HashMap<String, Object>();
    private Boolean usePreviousScripts = false;

  /**
   * General instructions for generating mock scripts. 
   **/
  public GenerateAIMockScriptsRequestDTO instructions(String instructions) {
    this.instructions = instructions;
    return this;
  }

  
  @ApiModelProperty(example = "Make all the responses upper case.", value = "General instructions for generating mock scripts. ")
  @JsonProperty("instructions")
  public String getInstructions() {
    return instructions;
  }
  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  /**
   * The script content. Optional and used only when sending custom scripts. 
   **/
  public GenerateAIMockScriptsRequestDTO script(String script) {
    this.script = script;
    return this;
  }

  
  @ApiModelProperty(example = "mc.setProperty('CONTENT_TYPE', 'application/json'); var response = [{ id: 1, name: 'Doggo' }]; mc.setPayloadJSON(response); mc.setProperty('HTTP_SC', '200'); ", value = "The script content. Optional and used only when sending custom scripts. ")
  @JsonProperty("script")
  public String getScript() {
    return script;
  }
  public void setScript(String script) {
    this.script = script;
  }

  /**
   * Config to generate mock scripts and DB properties. Required when generating new mock scripts. 
   **/
  public GenerateAIMockScriptsRequestDTO modify(Map<String, Object> modify) {
    this.modify = modify;
    return this;
  }

  
  @ApiModelProperty(value = "Config to generate mock scripts and DB properties. Required when generating new mock scripts. ")
  @JsonProperty("modify")
  public Map<String, Object> getModify() {
    return modify;
  }
  public void setModify(Map<String, Object> modify) {
    this.modify = modify;
  }

  /**
   * Set to true to use previously saved scripts instead of generating new ones. 
   **/
  public GenerateAIMockScriptsRequestDTO usePreviousScripts(Boolean usePreviousScripts) {
    this.usePreviousScripts = usePreviousScripts;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Set to true to use previously saved scripts instead of generating new ones. ")
  @JsonProperty("usePreviousScripts")
  public Boolean isUsePreviousScripts() {
    return usePreviousScripts;
  }
  public void setUsePreviousScripts(Boolean usePreviousScripts) {
    this.usePreviousScripts = usePreviousScripts;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenerateAIMockScriptsRequestDTO generateAIMockScriptsRequest = (GenerateAIMockScriptsRequestDTO) o;
    return Objects.equals(instructions, generateAIMockScriptsRequest.instructions) &&
        Objects.equals(script, generateAIMockScriptsRequest.script) &&
        Objects.equals(modify, generateAIMockScriptsRequest.modify) &&
        Objects.equals(usePreviousScripts, generateAIMockScriptsRequest.usePreviousScripts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instructions, script, modify, usePreviousScripts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenerateAIMockScriptsRequestDTO {\n");
    
    sb.append("    instructions: ").append(toIndentedString(instructions)).append("\n");
    sb.append("    script: ").append(toIndentedString(script)).append("\n");
    sb.append("    modify: ").append(toIndentedString(modify)).append("\n");
    sb.append("    usePreviousScripts: ").append(toIndentedString(usePreviousScripts)).append("\n");
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

