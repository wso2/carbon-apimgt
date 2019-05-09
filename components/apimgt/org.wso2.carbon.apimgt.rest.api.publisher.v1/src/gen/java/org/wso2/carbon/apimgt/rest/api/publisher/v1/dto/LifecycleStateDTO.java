package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateAvailableTransitionsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateCheckItemsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateDTO  {
  
  
  
  private String state = null;
  
  
  private List<LifecycleStateCheckItemsDTO> checkItems = new ArrayList<LifecycleStateCheckItemsDTO>();
  
  
  private List<LifecycleStateAvailableTransitionsDTO> availableTransitions = new ArrayList<LifecycleStateAvailableTransitionsDTO>();

  
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
  @JsonProperty("checkItems")
  public List<LifecycleStateCheckItemsDTO> getCheckItems() {
    return checkItems;
  }
  public void setCheckItems(List<LifecycleStateCheckItemsDTO> checkItems) {
    this.checkItems = checkItems;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("availableTransitions")
  public List<LifecycleStateAvailableTransitionsDTO> getAvailableTransitions() {
    return availableTransitions;
  }
  public void setAvailableTransitions(List<LifecycleStateAvailableTransitionsDTO> availableTransitions) {
    this.availableTransitions = availableTransitions;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateDTO {\n");
    
    sb.append("  state: ").append(state).append("\n");
    sb.append("  checkItems: ").append(checkItems).append("\n");
    sb.append("  availableTransitions: ").append(availableTransitions).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
