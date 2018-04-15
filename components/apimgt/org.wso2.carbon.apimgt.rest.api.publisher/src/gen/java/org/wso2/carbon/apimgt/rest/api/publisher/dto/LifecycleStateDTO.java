package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_availableTransitionBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_checkItemBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_inputBeanListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_permissionBeansDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_validationBeansDTO;
import java.util.Objects;

/**
 * LifecycleStateDTO
 */
public class LifecycleStateDTO   {
  @SerializedName("lcName")
  private String lcName = null;

  @SerializedName("state")
  private String state = null;

  @SerializedName("lifecyelId")
  private String lifecyelId = null;

  @SerializedName("checkItemBeanList")
  private List<LifecycleState_checkItemBeanListDTO> checkItemBeanList = new ArrayList<LifecycleState_checkItemBeanListDTO>();

  @SerializedName("inputBeanList")
  private List<LifecycleState_inputBeanListDTO> inputBeanList = new ArrayList<LifecycleState_inputBeanListDTO>();

  @SerializedName("customCodeBeanList")
  private List<LifecycleState_validationBeansDTO> customCodeBeanList = new ArrayList<LifecycleState_validationBeansDTO>();

  @SerializedName("availableTransitionBeanList")
  private List<LifecycleState_availableTransitionBeanListDTO> availableTransitionBeanList = new ArrayList<LifecycleState_availableTransitionBeanListDTO>();

  @SerializedName("permissionBeanList")
  private List<LifecycleState_permissionBeansDTO> permissionBeanList = new ArrayList<LifecycleState_permissionBeansDTO>();

  public LifecycleStateDTO lcName(String lcName) {
    this.lcName = lcName;
    return this;
  }

   /**
   * Get lcName
   * @return lcName
  **/
  @ApiModelProperty(example = "API Lifecycle", value = "")
  public String getLcName() {
    return lcName;
  }

  public void setLcName(String lcName) {
    this.lcName = lcName;
  }

  public LifecycleStateDTO state(String state) {
    this.state = state;
    return this;
  }

   /**
   * Get state
   * @return state
  **/
  @ApiModelProperty(example = "Created", value = "")
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public LifecycleStateDTO lifecyelId(String lifecyelId) {
    this.lifecyelId = lifecyelId;
    return this;
  }

   /**
   * Get lifecyelId
   * @return lifecyelId
  **/
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  public String getLifecyelId() {
    return lifecyelId;
  }

  public void setLifecyelId(String lifecyelId) {
    this.lifecyelId = lifecyelId;
  }

  public LifecycleStateDTO checkItemBeanList(List<LifecycleState_checkItemBeanListDTO> checkItemBeanList) {
    this.checkItemBeanList = checkItemBeanList;
    return this;
  }

  public LifecycleStateDTO addCheckItemBeanListItem(LifecycleState_checkItemBeanListDTO checkItemBeanListItem) {
    this.checkItemBeanList.add(checkItemBeanListItem);
    return this;
  }

   /**
   * Get checkItemBeanList
   * @return checkItemBeanList
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_checkItemBeanListDTO> getCheckItemBeanList() {
    return checkItemBeanList;
  }

  public void setCheckItemBeanList(List<LifecycleState_checkItemBeanListDTO> checkItemBeanList) {
    this.checkItemBeanList = checkItemBeanList;
  }

  public LifecycleStateDTO inputBeanList(List<LifecycleState_inputBeanListDTO> inputBeanList) {
    this.inputBeanList = inputBeanList;
    return this;
  }

  public LifecycleStateDTO addInputBeanListItem(LifecycleState_inputBeanListDTO inputBeanListItem) {
    this.inputBeanList.add(inputBeanListItem);
    return this;
  }

   /**
   * Get inputBeanList
   * @return inputBeanList
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_inputBeanListDTO> getInputBeanList() {
    return inputBeanList;
  }

  public void setInputBeanList(List<LifecycleState_inputBeanListDTO> inputBeanList) {
    this.inputBeanList = inputBeanList;
  }

  public LifecycleStateDTO customCodeBeanList(List<LifecycleState_validationBeansDTO> customCodeBeanList) {
    this.customCodeBeanList = customCodeBeanList;
    return this;
  }

  public LifecycleStateDTO addCustomCodeBeanListItem(LifecycleState_validationBeansDTO customCodeBeanListItem) {
    this.customCodeBeanList.add(customCodeBeanListItem);
    return this;
  }

   /**
   * Get customCodeBeanList
   * @return customCodeBeanList
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_validationBeansDTO> getCustomCodeBeanList() {
    return customCodeBeanList;
  }

  public void setCustomCodeBeanList(List<LifecycleState_validationBeansDTO> customCodeBeanList) {
    this.customCodeBeanList = customCodeBeanList;
  }

  public LifecycleStateDTO availableTransitionBeanList(List<LifecycleState_availableTransitionBeanListDTO> availableTransitionBeanList) {
    this.availableTransitionBeanList = availableTransitionBeanList;
    return this;
  }

  public LifecycleStateDTO addAvailableTransitionBeanListItem(LifecycleState_availableTransitionBeanListDTO availableTransitionBeanListItem) {
    this.availableTransitionBeanList.add(availableTransitionBeanListItem);
    return this;
  }

   /**
   * Get availableTransitionBeanList
   * @return availableTransitionBeanList
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_availableTransitionBeanListDTO> getAvailableTransitionBeanList() {
    return availableTransitionBeanList;
  }

  public void setAvailableTransitionBeanList(List<LifecycleState_availableTransitionBeanListDTO> availableTransitionBeanList) {
    this.availableTransitionBeanList = availableTransitionBeanList;
  }

  public LifecycleStateDTO permissionBeanList(List<LifecycleState_permissionBeansDTO> permissionBeanList) {
    this.permissionBeanList = permissionBeanList;
    return this;
  }

  public LifecycleStateDTO addPermissionBeanListItem(LifecycleState_permissionBeansDTO permissionBeanListItem) {
    this.permissionBeanList.add(permissionBeanListItem);
    return this;
  }

   /**
   * Get permissionBeanList
   * @return permissionBeanList
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_permissionBeansDTO> getPermissionBeanList() {
    return permissionBeanList;
  }

  public void setPermissionBeanList(List<LifecycleState_permissionBeansDTO> permissionBeanList) {
    this.permissionBeanList = permissionBeanList;
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
    return Objects.equals(this.lcName, lifecycleState.lcName) &&
        Objects.equals(this.state, lifecycleState.state) &&
        Objects.equals(this.lifecyelId, lifecycleState.lifecyelId) &&
        Objects.equals(this.checkItemBeanList, lifecycleState.checkItemBeanList) &&
        Objects.equals(this.inputBeanList, lifecycleState.inputBeanList) &&
        Objects.equals(this.customCodeBeanList, lifecycleState.customCodeBeanList) &&
        Objects.equals(this.availableTransitionBeanList, lifecycleState.availableTransitionBeanList) &&
        Objects.equals(this.permissionBeanList, lifecycleState.permissionBeanList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lcName, state, lifecyelId, checkItemBeanList, inputBeanList, customCodeBeanList, availableTransitionBeanList, permissionBeanList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateDTO {\n");
    
    sb.append("    lcName: ").append(toIndentedString(lcName)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    lifecyelId: ").append(toIndentedString(lifecyelId)).append("\n");
    sb.append("    checkItemBeanList: ").append(toIndentedString(checkItemBeanList)).append("\n");
    sb.append("    inputBeanList: ").append(toIndentedString(inputBeanList)).append("\n");
    sb.append("    customCodeBeanList: ").append(toIndentedString(customCodeBeanList)).append("\n");
    sb.append("    availableTransitionBeanList: ").append(toIndentedString(availableTransitionBeanList)).append("\n");
    sb.append("    permissionBeanList: ").append(toIndentedString(permissionBeanList)).append("\n");
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

