package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateAvailableTransitionBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateCheckItemBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateInputBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStatePermissionBeansDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateValidationBeansDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateDTO  {
  
  
  
  private String lcName = null;
  
  
  private String state = null;
  
  
  private String lifecyelId = null;
  
  
  private List<LifecycleStateCheckItemBeanListDTO> checkItemBeanList = new ArrayList<LifecycleStateCheckItemBeanListDTO>();
  
  
  private List<LifecycleStateInputBeanListDTO> inputBeanList = new ArrayList<LifecycleStateInputBeanListDTO>();
  
  
  private List<LifecycleStateValidationBeansDTO> customCodeBeanList = new ArrayList<LifecycleStateValidationBeansDTO>();
  
  
  private List<LifecycleStateAvailableTransitionBeanListDTO> availableTransitionBeanList = new ArrayList<LifecycleStateAvailableTransitionBeanListDTO>();
  
  
  private List<LifecycleStatePermissionBeansDTO> permissionBeanList = new ArrayList<LifecycleStatePermissionBeansDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lcName")
  public String getLcName() {
    return lcName;
  }
  public void setLcName(String lcName) {
    this.lcName = lcName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifecyelId")
  public String getLifecyelId() {
    return lifecyelId;
  }
  public void setLifecyelId(String lifecyelId) {
    this.lifecyelId = lifecyelId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("checkItemBeanList")
  public List<LifecycleStateCheckItemBeanListDTO> getCheckItemBeanList() {
    return checkItemBeanList;
  }
  public void setCheckItemBeanList(List<LifecycleStateCheckItemBeanListDTO> checkItemBeanList) {
    this.checkItemBeanList = checkItemBeanList;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("inputBeanList")
  public List<LifecycleStateInputBeanListDTO> getInputBeanList() {
    return inputBeanList;
  }
  public void setInputBeanList(List<LifecycleStateInputBeanListDTO> inputBeanList) {
    this.inputBeanList = inputBeanList;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("customCodeBeanList")
  public List<LifecycleStateValidationBeansDTO> getCustomCodeBeanList() {
    return customCodeBeanList;
  }
  public void setCustomCodeBeanList(List<LifecycleStateValidationBeansDTO> customCodeBeanList) {
    this.customCodeBeanList = customCodeBeanList;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("availableTransitionBeanList")
  public List<LifecycleStateAvailableTransitionBeanListDTO> getAvailableTransitionBeanList() {
    return availableTransitionBeanList;
  }
  public void setAvailableTransitionBeanList(List<LifecycleStateAvailableTransitionBeanListDTO> availableTransitionBeanList) {
    this.availableTransitionBeanList = availableTransitionBeanList;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("permissionBeanList")
  public List<LifecycleStatePermissionBeansDTO> getPermissionBeanList() {
    return permissionBeanList;
  }
  public void setPermissionBeanList(List<LifecycleStatePermissionBeansDTO> permissionBeanList) {
    this.permissionBeanList = permissionBeanList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateDTO {\n");
    
    sb.append("  lcName: ").append(lcName).append("\n");
    sb.append("  state: ").append(state).append("\n");
    sb.append("  lifecyelId: ").append(lifecyelId).append("\n");
    sb.append("  checkItemBeanList: ").append(checkItemBeanList).append("\n");
    sb.append("  inputBeanList: ").append(inputBeanList).append("\n");
    sb.append("  customCodeBeanList: ").append(customCodeBeanList).append("\n");
    sb.append("  availableTransitionBeanList: ").append(availableTransitionBeanList).append("\n");
    sb.append("  permissionBeanList: ").append(permissionBeanList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
