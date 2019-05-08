package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIThreatProtectionPoliciesListDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIThreatProtectionPoliciesDTO  {
  
  
  
  private List<APIThreatProtectionPoliciesListDTO> list = new ArrayList<APIThreatProtectionPoliciesListDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<APIThreatProtectionPoliciesListDTO> getList() {
    return list;
  }
  public void setList(List<APIThreatProtectionPoliciesListDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIThreatProtectionPoliciesDTO {\n");
    
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
