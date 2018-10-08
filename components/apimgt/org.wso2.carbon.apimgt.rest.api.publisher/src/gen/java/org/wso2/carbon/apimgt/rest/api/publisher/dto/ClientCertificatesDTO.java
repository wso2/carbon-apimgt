package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertMetadataDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Representation of a list of client certificates
 **/


@ApiModel(description = "Representation of a list of client certificates")
public class ClientCertificatesDTO  {
  
  
  
  private Integer count = null;
  
  
  private String next = null;
  
  
  private String previous = null;
  
  
  private List<ClientCertMetadataDTO> certificates = new ArrayList<ClientCertMetadataDTO>();
  
  
  private APIListPaginationDTO pagination = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("certificates")
  public List<ClientCertMetadataDTO> getCertificates() {
    return certificates;
  }
  public void setCertificates(List<ClientCertMetadataDTO> certificates) {
    this.certificates = certificates;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("pagination")
  public APIListPaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(APIListPaginationDTO pagination) {
    this.pagination = pagination;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientCertificatesDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  next: ").append(next).append("\n");
    sb.append("  previous: ").append(previous).append("\n");
    sb.append("  certificates: ").append(certificates).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
