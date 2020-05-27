package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.LifecycleStateAvailableTransitionsDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.LifecycleStateCheckItemsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class LifecycleStateDTO   {
  
    private String state = null;
    private List<LifecycleStateCheckItemsDTO> checkItems = new ArrayList<>();
    private List<LifecycleStateAvailableTransitionsDTO> availableTransitions = new ArrayList<>();

  /**
   **/
  public LifecycleStateDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(example = "Created", value = "")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  /**
   **/
  public LifecycleStateDTO checkItems(List<LifecycleStateCheckItemsDTO> checkItems) {
    this.checkItems = checkItems;
    return this;
  }

  
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
  public LifecycleStateDTO availableTransitions(List<LifecycleStateAvailableTransitionsDTO> availableTransitions) {
    this.availableTransitions = availableTransitions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("availableTransitions")
  public List<LifecycleStateAvailableTransitionsDTO> getAvailableTransitions() {
    return availableTransitions;
  }
  public void setAvailableTransitions(List<LifecycleStateAvailableTransitionsDTO> availableTransitions) {
    this.availableTransitions = availableTransitions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleStateDTO lifecycleState = (LifecycleStateDTO) o;
    return Objects.equals(state, lifecycleState.state) &&
        Objects.equals(checkItems, lifecycleState.checkItems) &&
        Objects.equals(availableTransitions, lifecycleState.availableTransitions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, checkItems, availableTransitions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateDTO {\n");
    
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    checkItems: ").append(toIndentedString(checkItems)).append("\n");
    sb.append("    availableTransitions: ").append(toIndentedString(availableTransitions)).append("\n");
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

