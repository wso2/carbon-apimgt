package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import javax.validation.constraints.*;

/**
 * Representation of a list of client certificates
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Representation of a list of client certificates")

public class ClientCertificatesDTO   {
  
    private Integer count = null;
    private List<ClientCertMetadataDTO> certificates = new ArrayList<ClientCertMetadataDTO>();
    private PaginationDTO pagination = null;

  /**
   **/
  public ClientCertificatesDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public ClientCertificatesDTO certificates(List<ClientCertMetadataDTO> certificates) {
    this.certificates = certificates;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("certificates")
  public List<ClientCertMetadataDTO> getCertificates() {
    return certificates;
  }
  public void setCertificates(List<ClientCertMetadataDTO> certificates) {
    this.certificates = certificates;
  }

  /**
   **/
  public ClientCertificatesDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientCertificatesDTO clientCertificates = (ClientCertificatesDTO) o;
    return Objects.equals(count, clientCertificates.count) &&
        Objects.equals(certificates, clientCertificates.certificates) &&
        Objects.equals(pagination, clientCertificates.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, certificates, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientCertificatesDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    certificates: ").append(toIndentedString(certificates)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
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

