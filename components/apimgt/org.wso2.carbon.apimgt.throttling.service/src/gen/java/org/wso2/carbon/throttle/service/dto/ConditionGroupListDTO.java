package org.wso2.carbon.throttle.service.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.throttle.service.dto.ConditionGroupDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConditionGroupListDTO extends ArrayList<ConditionGroupDTO> {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionGroupListDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
