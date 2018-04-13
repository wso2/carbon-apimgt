package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * LifecycleState_inputBeanListDTO
 */
public class LifecycleState_inputBeanListDTO   {
  @SerializedName("name")
  private String name = null;

  @SerializedName("isRequired")
  private Boolean isRequired = null;

  @SerializedName("label")
  private String label = null;

  @SerializedName("placeHolder")
  private String placeHolder = null;

  @SerializedName("tooltip")
  private String tooltip = null;

  @SerializedName("regex")
  private String regex = null;

  @SerializedName("values")
  private String values = null;

  @SerializedName("forTarget")
  private String forTarget = null;

  public LifecycleState_inputBeanListDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Gateways to be published", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LifecycleState_inputBeanListDTO isRequired(Boolean isRequired) {
    this.isRequired = isRequired;
    return this;
  }

   /**
   * Get isRequired
   * @return isRequired
  **/
  @ApiModelProperty(example = "true", value = "")
  public Boolean getIsRequired() {
    return isRequired;
  }

  public void setIsRequired(Boolean isRequired) {
    this.isRequired = isRequired;
  }

  public LifecycleState_inputBeanListDTO label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @ApiModelProperty(value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public LifecycleState_inputBeanListDTO placeHolder(String placeHolder) {
    this.placeHolder = placeHolder;
    return this;
  }

   /**
   * Get placeHolder
   * @return placeHolder
  **/
  @ApiModelProperty(value = "")
  public String getPlaceHolder() {
    return placeHolder;
  }

  public void setPlaceHolder(String placeHolder) {
    this.placeHolder = placeHolder;
  }

  public LifecycleState_inputBeanListDTO tooltip(String tooltip) {
    this.tooltip = tooltip;
    return this;
  }

   /**
   * Get tooltip
   * @return tooltip
  **/
  @ApiModelProperty(value = "")
  public String getTooltip() {
    return tooltip;
  }

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  public LifecycleState_inputBeanListDTO regex(String regex) {
    this.regex = regex;
    return this;
  }

   /**
   * Get regex
   * @return regex
  **/
  @ApiModelProperty(value = "")
  public String getRegex() {
    return regex;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public LifecycleState_inputBeanListDTO values(String values) {
    this.values = values;
    return this;
  }

   /**
   * Get values
   * @return values
  **/
  @ApiModelProperty(value = "")
  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  public LifecycleState_inputBeanListDTO forTarget(String forTarget) {
    this.forTarget = forTarget;
    return this;
  }

   /**
   * Get forTarget
   * @return forTarget
  **/
  @ApiModelProperty(example = "Created", value = "")
  public String getForTarget() {
    return forTarget;
  }

  public void setForTarget(String forTarget) {
    this.forTarget = forTarget;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleState_inputBeanListDTO lifecycleStateInputBeanList = (LifecycleState_inputBeanListDTO) o;
    return Objects.equals(this.name, lifecycleStateInputBeanList.name) &&
        Objects.equals(this.isRequired, lifecycleStateInputBeanList.isRequired) &&
        Objects.equals(this.label, lifecycleStateInputBeanList.label) &&
        Objects.equals(this.placeHolder, lifecycleStateInputBeanList.placeHolder) &&
        Objects.equals(this.tooltip, lifecycleStateInputBeanList.tooltip) &&
        Objects.equals(this.regex, lifecycleStateInputBeanList.regex) &&
        Objects.equals(this.values, lifecycleStateInputBeanList.values) &&
        Objects.equals(this.forTarget, lifecycleStateInputBeanList.forTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, isRequired, label, placeHolder, tooltip, regex, values, forTarget);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleState_inputBeanListDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    isRequired: ").append(toIndentedString(isRequired)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    placeHolder: ").append(toIndentedString(placeHolder)).append("\n");
    sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
    sb.append("    regex: ").append(toIndentedString(regex)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
    sb.append("    forTarget: ").append(toIndentedString(forTarget)).append("\n");
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

