package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import org.hibernate.validator.constraints.NotEmpty;



public class ThrottlingPolicyPermissionInfoDTO   {
  

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("allow") ALLOW(String.valueOf("allow")), @XmlEnumValue("deny") DENY(String.valueOf("deny"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private TypeEnum type = null;
    private List<String> roles = new ArrayList<>();

  /**
   **/
  public ThrottlingPolicyPermissionInfoDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * roles for this permission
   **/
  public ThrottlingPolicyPermissionInfoDTO roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  
  @ApiModelProperty(example = "[\"manager\",\"developer\"]", value = "roles for this permission")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissionInfo = (ThrottlingPolicyPermissionInfoDTO) o;
    return Objects.equals(type, throttlingPolicyPermissionInfo.type) &&
        Objects.equals(roles, throttlingPolicyPermissionInfo.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlingPolicyPermissionInfoDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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

