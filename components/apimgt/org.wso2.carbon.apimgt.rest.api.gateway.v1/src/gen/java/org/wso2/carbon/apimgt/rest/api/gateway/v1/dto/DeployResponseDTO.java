package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class DeployResponseDTO   {
  

@XmlType(name="DeployStatusEnum")
@XmlEnum(String.class)
public enum DeployStatusEnum {

    @XmlEnumValue("DEPLOYED") DEPLOYED(String.valueOf("DEPLOYED")), @XmlEnumValue("ERROR") ERROR(String.valueOf("ERROR"));


    private String value;

    DeployStatusEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DeployStatusEnum fromValue(String v) {
        for (DeployStatusEnum b : DeployStatusEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private DeployStatusEnum deployStatus = null;
    private String jsonPayload = null;

  /**
   * This attribute declares whether deployment task is successfull or failed. 
   **/
  public DeployResponseDTO deployStatus(DeployStatusEnum deployStatus) {
    this.deployStatus = deployStatus;
    return this;
  }

  
  @ApiModelProperty(example = "DEPLOYED", required = true, value = "This attribute declares whether deployment task is successfull or failed. ")
  @JsonProperty("deployStatus")
  @NotNull
  public DeployStatusEnum getDeployStatus() {
    return deployStatus;
  }
  public void setDeployStatus(DeployStatusEnum deployStatus) {
    this.deployStatus = deployStatus;
  }

  /**
   * Attributes that returned after the api deployment 
   **/
  public DeployResponseDTO jsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
    return this;
  }

  
  @ApiModelProperty(value = "Attributes that returned after the api deployment ")
  @JsonProperty("jsonPayload")
  public String getJsonPayload() {
    return jsonPayload;
  }
  public void setJsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployResponseDTO deployResponse = (DeployResponseDTO) o;
    return Objects.equals(deployStatus, deployResponse.deployStatus) &&
        Objects.equals(jsonPayload, deployResponse.jsonPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployStatus, jsonPayload);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployResponseDTO {\n");
    
    sb.append("    deployStatus: ").append(toIndentedString(deployStatus)).append("\n");
    sb.append("    jsonPayload: ").append(toIndentedString(jsonPayload)).append("\n");
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

