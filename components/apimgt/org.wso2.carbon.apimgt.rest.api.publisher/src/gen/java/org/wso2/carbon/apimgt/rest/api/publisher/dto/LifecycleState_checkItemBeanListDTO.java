package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_permissionBeansDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleState_validationBeansDTO;
import java.util.Objects;

/**
 * LifecycleState_checkItemBeanListDTO
 */
public class LifecycleState_checkItemBeanListDTO   {
  @SerializedName("permissionBeans")
  private List<LifecycleState_permissionBeansDTO> permissionBeans = new ArrayList<LifecycleState_permissionBeansDTO>();

  @SerializedName("name")
  private String name = null;

  @SerializedName("validationBeans")
  private List<LifecycleState_validationBeansDTO> validationBeans = new ArrayList<LifecycleState_validationBeansDTO>();

  @SerializedName("targets")
  private List<String> targets = new ArrayList<String>();

  @SerializedName("value")
  private Boolean value = null;

  public LifecycleState_checkItemBeanListDTO permissionBeans(List<LifecycleState_permissionBeansDTO> permissionBeans) {
    this.permissionBeans = permissionBeans;
    return this;
  }

  public LifecycleState_checkItemBeanListDTO addPermissionBeansItem(LifecycleState_permissionBeansDTO permissionBeansItem) {
    this.permissionBeans.add(permissionBeansItem);
    return this;
  }

   /**
   * Get permissionBeans
   * @return permissionBeans
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_permissionBeansDTO> getPermissionBeans() {
    return permissionBeans;
  }

  public void setPermissionBeans(List<LifecycleState_permissionBeansDTO> permissionBeans) {
    this.permissionBeans = permissionBeans;
  }

  public LifecycleState_checkItemBeanListDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Deprecate old versions after publish the API", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LifecycleState_checkItemBeanListDTO validationBeans(List<LifecycleState_validationBeansDTO> validationBeans) {
    this.validationBeans = validationBeans;
    return this;
  }

  public LifecycleState_checkItemBeanListDTO addValidationBeansItem(LifecycleState_validationBeansDTO validationBeansItem) {
    this.validationBeans.add(validationBeansItem);
    return this;
  }

   /**
   * Get validationBeans
   * @return validationBeans
  **/
  @ApiModelProperty(value = "")
  public List<LifecycleState_validationBeansDTO> getValidationBeans() {
    return validationBeans;
  }

  public void setValidationBeans(List<LifecycleState_validationBeansDTO> validationBeans) {
    this.validationBeans = validationBeans;
  }

  public LifecycleState_checkItemBeanListDTO targets(List<String> targets) {
    this.targets = targets;
    return this;
  }

  public LifecycleState_checkItemBeanListDTO addTargetsItem(String targetsItem) {
    this.targets.add(targetsItem);
    return this;
  }

   /**
   * Get targets
   * @return targets
  **/
  @ApiModelProperty(value = "")
  public List<String> getTargets() {
    return targets;
  }

  public void setTargets(List<String> targets) {
    this.targets = targets;
  }

  public LifecycleState_checkItemBeanListDTO value(Boolean value) {
    this.value = value;
    return this;
  }

   /**
   * Get value
   * @return value
  **/
  @ApiModelProperty(example = "false", value = "")
  public Boolean getValue() {
    return value;
  }

  public void setValue(Boolean value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleState_checkItemBeanListDTO lifecycleStateCheckItemBeanList = (LifecycleState_checkItemBeanListDTO) o;
    return Objects.equals(this.permissionBeans, lifecycleStateCheckItemBeanList.permissionBeans) &&
        Objects.equals(this.name, lifecycleStateCheckItemBeanList.name) &&
        Objects.equals(this.validationBeans, lifecycleStateCheckItemBeanList.validationBeans) &&
        Objects.equals(this.targets, lifecycleStateCheckItemBeanList.targets) &&
        Objects.equals(this.value, lifecycleStateCheckItemBeanList.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionBeans, name, validationBeans, targets, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleState_checkItemBeanListDTO {\n");
    
    sb.append("    permissionBeans: ").append(toIndentedString(permissionBeans)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    validationBeans: ").append(toIndentedString(validationBeans)).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

