package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Representation of a list of certificates
 **/


@ApiModel(description = "Representation of a list of certificates")
public class CertificatesDTO  {
  
  
  
  private Integer count = null;
  
  
  private List<CertMetadataDTO> certificates = new ArrayList<CertMetadataDTO>();
  
  
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
  public List<CertMetadataDTO> getCertificates() {
    return certificates;
  }
  public void setCertificates(List<CertMetadataDTO> certificates) {
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
    sb.append("class CertificatesDTO {\n");
    
    sb.append("  count: ").append(count).append("\n");
    sb.append("  certificates: ").append(certificates).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
