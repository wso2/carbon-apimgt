package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ChatMessageDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MarketplaceAssistantRequestDTO   {
  
    private String query = null;
    private List<ChatMessageDTO> history = new ArrayList<ChatMessageDTO>();

  /**
   * natural langugae query given by the user.
   **/
  public MarketplaceAssistantRequestDTO query(String query) {
    this.query = query;
    return this;
  }

  
  @ApiModelProperty(example = "Hi", required = true, value = "natural langugae query given by the user.")
  @JsonProperty("query")
  @NotNull
  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   **/
  public MarketplaceAssistantRequestDTO history(List<ChatMessageDTO> history) {
    this.history = history;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("history")
  @NotNull
  public List<ChatMessageDTO> getHistory() {
    return history;
  }
  public void setHistory(List<ChatMessageDTO> history) {
    this.history = history;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketplaceAssistantRequestDTO marketplaceAssistantRequest = (MarketplaceAssistantRequestDTO) o;
    return Objects.equals(query, marketplaceAssistantRequest.query) &&
        Objects.equals(history, marketplaceAssistantRequest.history);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, history);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketplaceAssistantRequestDTO {\n");
    
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    history: ").append(toIndentedString(history)).append("\n");
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

