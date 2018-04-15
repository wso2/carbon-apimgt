package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Scope_bindingsDTO;
import java.util.Objects;

/**
 * ScopeDTO
 */
public class ScopeDTO   {
  @SerializedName("name")
  private String name = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("bindings")
  private Scope_bindingsDTO bindings = null;

  public ScopeDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * name of Scope 
   * @return name
  **/
  @ApiModelProperty(example = "apim:api_view", value = "name of Scope ")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ScopeDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * description of Scope 
   * @return description
  **/
  @ApiModelProperty(example = "This Scope can used to view Apis", value = "description of Scope ")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ScopeDTO bindings(Scope_bindingsDTO bindings) {
    this.bindings = bindings;
    return this;
  }

   /**
   * Get bindings
   * @return bindings
  **/
  @ApiModelProperty(value = "")
  public Scope_bindingsDTO getBindings() {
    return bindings;
  }

  public void setBindings(Scope_bindingsDTO bindings) {
    this.bindings = bindings;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeDTO scope = (ScopeDTO) o;
    return Objects.equals(this.name, scope.name) &&
        Objects.equals(this.description, scope.description) &&
        Objects.equals(this.bindings, scope.bindings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, bindings);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    bindings: ").append(toIndentedString(bindings)).append("\n");
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

