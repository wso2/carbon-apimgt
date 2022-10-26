package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;



public class APIThreatProtectionPoliciesDTO   {
  
    private List<APIThreatProtectionPoliciesListDTO> list = new ArrayList<APIThreatProtectionPoliciesListDTO>();

  /**
   **/
  public APIThreatProtectionPoliciesDTO list(List<APIThreatProtectionPoliciesListDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<APIThreatProtectionPoliciesListDTO> getList() {
    return list;
  }
  public void setList(List<APIThreatProtectionPoliciesListDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIThreatProtectionPoliciesDTO apIThreatProtectionPolicies = (APIThreatProtectionPoliciesDTO) o;
    return Objects.equals(list, apIThreatProtectionPolicies.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIThreatProtectionPoliciesDTO {\n");
    
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

