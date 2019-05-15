package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Representation of a list of client certificates
 **/


@ApiModel(description = "Representation of a list of client certificates")
public class ClientCertificatesDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<ClientCertMetadataDTO> certificates = new ArrayList<ClientCertMetadataDTO>();
  
  
  private PaginationDTO pagination = null;

  
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
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientCertificatesDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  certificates: ").append(certificates).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
