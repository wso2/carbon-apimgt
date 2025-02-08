package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Action to be taken when a governance policy is violated.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Action to be taken when a governance policy is violated.")

public class ActionDTO   {
  

          @XmlType(name="StateEnum")
    @XmlEnum(String.class)
    public enum StateEnum {
        API_CREATE("API_CREATE"),
        API_UPDATE("API_UPDATE"),
        API_DEPLOY("API_DEPLOY"),
        API_PUBLISH("API_PUBLISH");
        private String value;

        StateEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static StateEnum fromValue(String v) {
            for (StateEnum b : StateEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private StateEnum state = null;

          @XmlType(name="RuleSeverityEnum")
    @XmlEnum(String.class)
    public enum RuleSeverityEnum {
        ERROR("ERROR"),
        WARN("WARN"),
        INFO("INFO");
        private String value;

        RuleSeverityEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static RuleSeverityEnum fromValue(String v) {
            for (RuleSeverityEnum b : RuleSeverityEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleSeverityEnum ruleSeverity = null;

          @XmlType(name="TypeEnum")
    @XmlEnum(String.class)
    public enum TypeEnum {
        BLOCK("BLOCK"),
        NOTIFY("NOTIFY");
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

        @JsonCreator
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

  /**
   * The state of the artifact to which the action is linked to.
   **/
  public ActionDTO state(StateEnum state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEPLOY", value = "The state of the artifact to which the action is linked to.")
  @JsonProperty("state")
  public StateEnum getState() {
    return state;
  }
  public void setState(StateEnum state) {
    this.state = state;
  }

  /**
   * The severity of the rule to which the action is linked to.
   **/
  public ActionDTO ruleSeverity(RuleSeverityEnum ruleSeverity) {
    this.ruleSeverity = ruleSeverity;
    return this;
  }

  
  @ApiModelProperty(example = "ERROR", value = "The severity of the rule to which the action is linked to.")
  @JsonProperty("ruleSeverity")
  public RuleSeverityEnum getRuleSeverity() {
    return ruleSeverity;
  }
  public void setRuleSeverity(RuleSeverityEnum ruleSeverity) {
    this.ruleSeverity = ruleSeverity;
  }

  /**
   * The type of action to be taken when a governance policy is violated in the given state withe given rule severity.
   **/
  public ActionDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "BLOCK", value = "The type of action to be taken when a governance policy is violated in the given state withe given rule severity.")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActionDTO action = (ActionDTO) o;
    return Objects.equals(state, action.state) &&
        Objects.equals(ruleSeverity, action.ruleSeverity) &&
        Objects.equals(type, action.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, ruleSeverity, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActionDTO {\n");
    
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    ruleSeverity: ").append(toIndentedString(ruleSeverity)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

