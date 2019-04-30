package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ThreatProtectionPolicyListDTO  {
  
  
  
  private List<ThreatProtectionPolicyDTO> list = new ArrayList<ThreatProtectionPolicyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<ThreatProtectionPolicyDTO> getList() {
    return list;
  }
  public void setList(List<ThreatProtectionPolicyDTO> list) {
    this.list = list;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThreatProtectionPolicyListDTO {\n");
    
    sb.append("  list: ").append(list).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
