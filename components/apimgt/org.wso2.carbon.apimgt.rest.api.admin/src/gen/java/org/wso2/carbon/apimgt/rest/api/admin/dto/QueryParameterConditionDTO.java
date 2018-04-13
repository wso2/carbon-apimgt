package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * QueryParameterConditionDTO
 */
public class QueryParameterConditionDTO   {
  @SerializedName("parameterName")
  private String parameterName = null;

  @SerializedName("parameterValue")
  private String parameterValue = null;

  public QueryParameterConditionDTO parameterName(String parameterName) {
    this.parameterName = parameterName;
    return this;
  }

   /**
   * Get parameterName
   * @return parameterName
  **/
  @ApiModelProperty(required = true, value = "")
  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public QueryParameterConditionDTO parameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
    return this;
  }

   /**
   * Get parameterValue
   * @return parameterValue
  **/
  @ApiModelProperty(required = true, value = "")
  public String getParameterValue() {
    return parameterValue;
  }

  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryParameterConditionDTO queryParameterCondition = (QueryParameterConditionDTO) o;
    return Objects.equals(this.parameterName, queryParameterCondition.parameterName) &&
        Objects.equals(this.parameterValue, queryParameterCondition.parameterValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameterName, parameterValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryParameterConditionDTO {\n");
    
    sb.append("    parameterName: ").append(toIndentedString(parameterName)).append("\n");
    sb.append("    parameterValue: ").append(toIndentedString(parameterValue)).append("\n");
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

