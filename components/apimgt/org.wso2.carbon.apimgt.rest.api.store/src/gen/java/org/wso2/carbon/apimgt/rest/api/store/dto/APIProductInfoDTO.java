package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.ScopeInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIProductInfoDTO  {
  
  
  
  private String id = null;
  
  
  private String name = null;
  
  
  private String description = null;
  
  
  private String provider = null;
  
  
  private String thumbnailUri = null;
  
  
  private List<ScopeInfoDTO> scopes = new ArrayList<ScopeInfoDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * If the provider value is not given, the user invoking the API will be used as the provider.\n
   **/
  @ApiModelProperty(value = "If the provider value is not given, the user invoking the API will be used as the provider.\n")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("thumbnailUri")
  public String getThumbnailUri() {
    return thumbnailUri;
  }
  public void setThumbnailUri(String thumbnailUri) {
    this.thumbnailUri = thumbnailUri;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("scopes")
  public List<ScopeInfoDTO> getScopes() {
    return scopes;
  }
  public void setScopes(List<ScopeInfoDTO> scopes) {
    this.scopes = scopes;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductInfoDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("  thumbnailUri: ").append(thumbnailUri).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
