package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ContentPublishStatusDTO   {
  

    @XmlType(name="ACTIONEnum")
    @XmlEnum(String.class)
    public enum ACTIONEnum {
        PUBLISH("PUBLISH"),
        UNPUBLISH("UNPUBLISH");
        private String value;

        ACTIONEnum (String v) {
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
        public static ACTIONEnum fromValue(String v) {
            for (ACTIONEnum b : ACTIONEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ACTIONEnum ACTION = null;

  /**
   **/
  public ContentPublishStatusDTO ACTION(ACTIONEnum ACTION) {
    this.ACTION = ACTION;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ACTION")
  public ACTIONEnum getACTION() {
    return ACTION;
  }
  public void setACTION(ACTIONEnum ACTION) {
    this.ACTION = ACTION;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentPublishStatusDTO contentPublishStatus = (ContentPublishStatusDTO) o;
    return Objects.equals(ACTION, contentPublishStatus.ACTION);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ACTION);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContentPublishStatusDTO {\n");
    
    sb.append("    ACTION: ").append(toIndentedString(ACTION)).append("\n");
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

