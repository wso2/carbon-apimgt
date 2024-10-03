package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMaxTpsTokenBasedThrottlingConfigurationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIMaxTpsDTO   {
  
    private Long production = null;

    @XmlType(name="ProductionTimeUnitEnum")
    @XmlEnum(String.class)
    public enum ProductionTimeUnitEnum {
        SECOND("SECOND"),
        MINUTE("MINUTE"),
        HOUR("HOUR");
        private String value;

        ProductionTimeUnitEnum (String v) {
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
        public static ProductionTimeUnitEnum fromValue(String v) {
            for (ProductionTimeUnitEnum b : ProductionTimeUnitEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ProductionTimeUnitEnum productionTimeUnit = ProductionTimeUnitEnum.SECOND;
    private Long sandbox = null;

    @XmlType(name="SandboxTimeUnitEnum")
    @XmlEnum(String.class)
    public enum SandboxTimeUnitEnum {
        SECOND("SECOND"),
        MINUTE("MINUTE"),
        HOUR("HOUR");
        private String value;

        SandboxTimeUnitEnum (String v) {
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
        public static SandboxTimeUnitEnum fromValue(String v) {
            for (SandboxTimeUnitEnum b : SandboxTimeUnitEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private SandboxTimeUnitEnum sandboxTimeUnit = SandboxTimeUnitEnum.SECOND;
    private APIMaxTpsTokenBasedThrottlingConfigurationDTO tokenBasedThrottlingConfiguration = null;

  /**
   **/
  public APIMaxTpsDTO production(Long production) {
    this.production = production;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "")
  @JsonProperty("production")
  public Long getProduction() {
    return production;
  }
  public void setProduction(Long production) {
    this.production = production;
  }

  /**
   * Time unit for the production.
   **/
  public APIMaxTpsDTO productionTimeUnit(ProductionTimeUnitEnum productionTimeUnit) {
    this.productionTimeUnit = productionTimeUnit;
    return this;
  }

  
  @ApiModelProperty(value = "Time unit for the production.")
  @JsonProperty("productionTimeUnit")
  public ProductionTimeUnitEnum getProductionTimeUnit() {
    return productionTimeUnit;
  }
  public void setProductionTimeUnit(ProductionTimeUnitEnum productionTimeUnit) {
    this.productionTimeUnit = productionTimeUnit;
  }

  /**
   **/
  public APIMaxTpsDTO sandbox(Long sandbox) {
    this.sandbox = sandbox;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "")
  @JsonProperty("sandbox")
  public Long getSandbox() {
    return sandbox;
  }
  public void setSandbox(Long sandbox) {
    this.sandbox = sandbox;
  }

  /**
   * Time unit for the sandbox.
   **/
  public APIMaxTpsDTO sandboxTimeUnit(SandboxTimeUnitEnum sandboxTimeUnit) {
    this.sandboxTimeUnit = sandboxTimeUnit;
    return this;
  }

  
  @ApiModelProperty(value = "Time unit for the sandbox.")
  @JsonProperty("sandboxTimeUnit")
  public SandboxTimeUnitEnum getSandboxTimeUnit() {
    return sandboxTimeUnit;
  }
  public void setSandboxTimeUnit(SandboxTimeUnitEnum sandboxTimeUnit) {
    this.sandboxTimeUnit = sandboxTimeUnit;
  }

  /**
   **/
  public APIMaxTpsDTO tokenBasedThrottlingConfiguration(APIMaxTpsTokenBasedThrottlingConfigurationDTO tokenBasedThrottlingConfiguration) {
    this.tokenBasedThrottlingConfiguration = tokenBasedThrottlingConfiguration;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("tokenBasedThrottlingConfiguration")
  public APIMaxTpsTokenBasedThrottlingConfigurationDTO getTokenBasedThrottlingConfiguration() {
    return tokenBasedThrottlingConfiguration;
  }
  public void setTokenBasedThrottlingConfiguration(APIMaxTpsTokenBasedThrottlingConfigurationDTO tokenBasedThrottlingConfiguration) {
    this.tokenBasedThrottlingConfiguration = tokenBasedThrottlingConfiguration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIMaxTpsDTO apIMaxTps = (APIMaxTpsDTO) o;
    return Objects.equals(production, apIMaxTps.production) &&
        Objects.equals(productionTimeUnit, apIMaxTps.productionTimeUnit) &&
        Objects.equals(sandbox, apIMaxTps.sandbox) &&
        Objects.equals(sandboxTimeUnit, apIMaxTps.sandboxTimeUnit) &&
        Objects.equals(tokenBasedThrottlingConfiguration, apIMaxTps.tokenBasedThrottlingConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(production, productionTimeUnit, sandbox, sandboxTimeUnit, tokenBasedThrottlingConfiguration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMaxTpsDTO {\n");
    
    sb.append("    production: ").append(toIndentedString(production)).append("\n");
    sb.append("    productionTimeUnit: ").append(toIndentedString(productionTimeUnit)).append("\n");
    sb.append("    sandbox: ").append(toIndentedString(sandbox)).append("\n");
    sb.append("    sandboxTimeUnit: ").append(toIndentedString(sandboxTimeUnit)).append("\n");
    sb.append("    tokenBasedThrottlingConfiguration: ").append(toIndentedString(tokenBasedThrottlingConfiguration)).append("\n");
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

