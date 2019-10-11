package org.wso2.carbon.throttle.service.dto;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;

@ApiModel(description = "")
public class RevokedJWTListDTO extends ArrayList<RevokedJWTDTO> {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTListDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
