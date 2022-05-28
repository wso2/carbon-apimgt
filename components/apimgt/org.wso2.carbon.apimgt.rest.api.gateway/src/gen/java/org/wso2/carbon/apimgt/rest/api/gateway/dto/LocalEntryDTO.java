package org.wso2.carbon.apimgt.rest.api.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class LocalEntryDTO   {
  
    private List<String> localEntries = new ArrayList<>();

  /**
   * The local entries which has been deployed in the gateway 
   **/
  public LocalEntryDTO localEntries(List<String> localEntries) {
    this.localEntries = localEntries;
    return this;
  }

  
  @ApiModelProperty(value = "The local entries which has been deployed in the gateway ")
  @JsonProperty("localEntries")
  public List<String> getLocalEntries() {
    return localEntries;
  }
  public void setLocalEntries(List<String> localEntries) {
    this.localEntries = localEntries;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalEntryDTO localEntry = (LocalEntryDTO) o;
    return Objects.equals(localEntries, localEntry.localEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(localEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LocalEntryDTO {\n");
    
    sb.append("    localEntries: ").append(toIndentedString(localEntries)).append("\n");
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

