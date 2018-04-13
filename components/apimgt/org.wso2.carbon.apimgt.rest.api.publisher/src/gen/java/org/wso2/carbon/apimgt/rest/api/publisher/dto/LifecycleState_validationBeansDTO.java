package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * LifecycleState_validationBeansDTO
 */
public class LifecycleState_validationBeansDTO   {
  @SerializedName("classObject")
  private Object classObject = null;

  @SerializedName("targetName")
  private String targetName = null;

  @SerializedName("customMessage")
  private String customMessage = null;

  public LifecycleState_validationBeansDTO classObject(Object classObject) {
    this.classObject = classObject;
    return this;
  }

   /**
   * Get classObject
   * @return classObject
  **/
  @ApiModelProperty(value = "")
  public Object getClassObject() {
    return classObject;
  }

  public void setClassObject(Object classObject) {
    this.classObject = classObject;
  }

  public LifecycleState_validationBeansDTO targetName(String targetName) {
    this.targetName = targetName;
    return this;
  }

   /**
   * Get targetName
   * @return targetName
  **/
  @ApiModelProperty(example = "Published", value = "")
  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public LifecycleState_validationBeansDTO customMessage(String customMessage) {
    this.customMessage = customMessage;
    return this;
  }

   /**
   * Get customMessage
   * @return customMessage
  **/
  @ApiModelProperty(example = "Validation successful", value = "")
  public String getCustomMessage() {
    return customMessage;
  }

  public void setCustomMessage(String customMessage) {
    this.customMessage = customMessage;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleState_validationBeansDTO lifecycleStateValidationBeans = (LifecycleState_validationBeansDTO) o;
    return Objects.equals(this.classObject, lifecycleStateValidationBeans.classObject) &&
        Objects.equals(this.targetName, lifecycleStateValidationBeans.targetName) &&
        Objects.equals(this.customMessage, lifecycleStateValidationBeans.customMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classObject, targetName, customMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleState_validationBeansDTO {\n");
    
    sb.append("    classObject: ").append(toIndentedString(classObject)).append("\n");
    sb.append("    targetName: ").append(toIndentedString(targetName)).append("\n");
    sb.append("    customMessage: ").append(toIndentedString(customMessage)).append("\n");
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

