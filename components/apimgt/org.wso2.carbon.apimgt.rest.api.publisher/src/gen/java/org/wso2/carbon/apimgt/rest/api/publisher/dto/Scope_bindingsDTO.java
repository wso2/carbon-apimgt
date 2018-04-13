package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Scope_bindingsDTO
 */
public class Scope_bindingsDTO   {
  @SerializedName("type")
  private String type = null;

  @SerializedName("values")
  private List<String> values = new ArrayList<String>();

  public Scope_bindingsDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Type of binding role / permission 
   * @return type
  **/
  @ApiModelProperty(value = "Type of binding role / permission ")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Scope_bindingsDTO values(List<String> values) {
    this.values = values;
    return this;
  }

  public Scope_bindingsDTO addValuesItem(String valuesItem) {
    this.values.add(valuesItem);
    return this;
  }

   /**
   * Get values
   * @return values
  **/
  @ApiModelProperty(value = "")
  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Scope_bindingsDTO scopeBindings = (Scope_bindingsDTO) o;
    return Objects.equals(this.type, scopeBindings.type) &&
        Objects.equals(this.values, scopeBindings.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Scope_bindingsDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
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

