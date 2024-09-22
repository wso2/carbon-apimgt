package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIProductInfoDTO   {
  
    private String id = null;
    private String name = null;
    private String context = null;
    private String description = null;
    private String provider = null;
    private String version = null;
    private Boolean hasThumbnail = null;
    private String state = null;
    private List<String> securityScheme = new ArrayList<String>();
    private String gatewayVendor = null;
    private List<String> audiences = new ArrayList<String>();
    private Boolean monetizedInfo = null;
    private String businessOwner = null;
    private String businessOwnerEmail = null;
    private String technicalOwner = null;
    private String technicalOwnerEmail = null;
    private Boolean egress = false;

  /**
   * UUID of the api product 
   **/
  public APIProductInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the api product ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the API Product
   **/
  public APIProductInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PizzaShackAPIProduct", value = "Name of the API Product")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIProductInfoDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizzaproduct", value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   * A brief description about the API
   **/
  public APIProductInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "This is a simple API for Pizza Shack online pizza delivery store", value = "A brief description about the API")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * If the provider value is not given, the user invoking the API will be used as the provider. 
   **/
  public APIProductInfoDTO provider(String provider) {
    this.provider = provider;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "If the provider value is not given, the user invoking the API will be used as the provider. ")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  /**
   **/
  public APIProductInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public APIProductInfoDTO hasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("hasThumbnail")
  public Boolean isHasThumbnail() {
    return hasThumbnail;
  }
  public void setHasThumbnail(Boolean hasThumbnail) {
    this.hasThumbnail = hasThumbnail;
  }

  /**
   * State of the API product. Only published API products are visible on the Developer Portal 
   **/
  public APIProductInfoDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(value = "State of the API product. Only published API products are visible on the Developer Portal ")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If it is not set OAuth2 will be set as the security for the current API. 
   **/
  public APIProductInfoDTO securityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
    return this;
  }

  
  @ApiModelProperty(example = "[\"oauth2\"]", value = "Types of API security, the current API secured with. It can be either OAuth2 or mutual SSL or both. If it is not set OAuth2 will be set as the security for the current API. ")
  @JsonProperty("securityScheme")
  public List<String> getSecurityScheme() {
    return securityScheme;
  }
  public void setSecurityScheme(List<String> securityScheme) {
    this.securityScheme = securityScheme;
  }

  /**
   **/
  public APIProductInfoDTO gatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "")
  @JsonProperty("gatewayVendor")
  public String getGatewayVendor() {
    return gatewayVendor;
  }
  public void setGatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
  }

  /**
   * The audiences of the API product for jwt validation. Accepted values are any String values
   **/
  public APIProductInfoDTO audiences(List<String> audiences) {
    this.audiences = audiences;
    return this;
  }

  
  @ApiModelProperty(value = "The audiences of the API product for jwt validation. Accepted values are any String values")
  @JsonProperty("audiences")
  public List<String> getAudiences() {
    return audiences;
  }
  public void setAudiences(List<String> audiences) {
    this.audiences = audiences;
  }

  /**
   **/
  public APIProductInfoDTO monetizedInfo(Boolean monetizedInfo) {
    this.monetizedInfo = monetizedInfo;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("monetizedInfo")
  public Boolean isMonetizedInfo() {
    return monetizedInfo;
  }
  public void setMonetizedInfo(Boolean monetizedInfo) {
    this.monetizedInfo = monetizedInfo;
  }

  /**
   **/
  public APIProductInfoDTO businessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
    return this;
  }

  
  @ApiModelProperty(example = "Business Owner", value = "")
  @JsonProperty("businessOwner")
  public String getBusinessOwner() {
    return businessOwner;
  }
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
  }

  /**
   **/
  public APIProductInfoDTO businessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "businessowner@abc.com", value = "")
  @JsonProperty("businessOwnerEmail")
  public String getBusinessOwnerEmail() {
    return businessOwnerEmail;
  }
  public void setBusinessOwnerEmail(String businessOwnerEmail) {
    this.businessOwnerEmail = businessOwnerEmail;
  }

  /**
   **/
  public APIProductInfoDTO technicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
    return this;
  }

  
  @ApiModelProperty(example = "Technical Owner", value = "")
  @JsonProperty("TechnicalOwner")
  public String getTechnicalOwner() {
    return technicalOwner;
  }
  public void setTechnicalOwner(String technicalOwner) {
    this.technicalOwner = technicalOwner;
  }

  /**
   **/
  public APIProductInfoDTO technicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
    return this;
  }

  
  @ApiModelProperty(example = "technicalowner@abc.com", value = "")
  @JsonProperty("TechnicalOwnerEmail")
  public String getTechnicalOwnerEmail() {
    return technicalOwnerEmail;
  }
  public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
    this.technicalOwnerEmail = technicalOwnerEmail;
  }

  /**
   * Whether the APIProduct is EGRESS or not
   **/
  public APIProductInfoDTO egress(Boolean egress) {
    this.egress = egress;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Whether the APIProduct is EGRESS or not")
  @JsonProperty("egress")
  public Boolean isEgress() {
    return egress;
  }
  public void setEgress(Boolean egress) {
    this.egress = egress;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductInfoDTO apIProductInfo = (APIProductInfoDTO) o;
    return Objects.equals(id, apIProductInfo.id) &&
        Objects.equals(name, apIProductInfo.name) &&
        Objects.equals(context, apIProductInfo.context) &&
        Objects.equals(description, apIProductInfo.description) &&
        Objects.equals(provider, apIProductInfo.provider) &&
        Objects.equals(version, apIProductInfo.version) &&
        Objects.equals(hasThumbnail, apIProductInfo.hasThumbnail) &&
        Objects.equals(state, apIProductInfo.state) &&
        Objects.equals(securityScheme, apIProductInfo.securityScheme) &&
        Objects.equals(gatewayVendor, apIProductInfo.gatewayVendor) &&
        Objects.equals(audiences, apIProductInfo.audiences) &&
        Objects.equals(monetizedInfo, apIProductInfo.monetizedInfo) &&
        Objects.equals(businessOwner, apIProductInfo.businessOwner) &&
        Objects.equals(businessOwnerEmail, apIProductInfo.businessOwnerEmail) &&
        Objects.equals(technicalOwner, apIProductInfo.technicalOwner) &&
        Objects.equals(technicalOwnerEmail, apIProductInfo.technicalOwnerEmail) &&
        Objects.equals(egress, apIProductInfo.egress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, context, description, provider, version, hasThumbnail, state, securityScheme, gatewayVendor, audiences, monetizedInfo, businessOwner, businessOwnerEmail, technicalOwner, technicalOwnerEmail, egress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    hasThumbnail: ").append(toIndentedString(hasThumbnail)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    securityScheme: ").append(toIndentedString(securityScheme)).append("\n");
    sb.append("    gatewayVendor: ").append(toIndentedString(gatewayVendor)).append("\n");
    sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
    sb.append("    monetizedInfo: ").append(toIndentedString(monetizedInfo)).append("\n");
    sb.append("    businessOwner: ").append(toIndentedString(businessOwner)).append("\n");
    sb.append("    businessOwnerEmail: ").append(toIndentedString(businessOwnerEmail)).append("\n");
    sb.append("    technicalOwner: ").append(toIndentedString(technicalOwner)).append("\n");
    sb.append("    technicalOwnerEmail: ").append(toIndentedString(technicalOwnerEmail)).append("\n");
    sb.append("    egress: ").append(toIndentedString(egress)).append("\n");
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

