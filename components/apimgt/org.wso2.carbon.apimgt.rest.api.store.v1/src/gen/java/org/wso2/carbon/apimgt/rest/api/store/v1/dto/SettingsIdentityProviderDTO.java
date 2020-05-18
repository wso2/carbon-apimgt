package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SettingsIdentityProviderDTO   {
  
    private Boolean external = false;

  /**
   **/
  public SettingsIdentityProviderDTO external(Boolean external) {
    this.external = external;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("external")
  public Boolean isExternal() {
    return external;
  }
  public void setExternal(Boolean external) {
    this.external = external;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsIdentityProviderDTO settingsIdentityProvider = (SettingsIdentityProviderDTO) o;
    return Objects.equals(external, settingsIdentityProvider.external);
  }

  @Override
  public int hashCode() {
    return Objects.hash(external);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsIdentityProviderDTO {\n");
    
    sb.append("    external: ").append(toIndentedString(external)).append("\n");
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

