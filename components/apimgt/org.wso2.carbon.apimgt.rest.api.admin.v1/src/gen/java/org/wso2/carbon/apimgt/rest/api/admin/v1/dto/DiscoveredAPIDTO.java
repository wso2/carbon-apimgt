package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.DiscoveredAPIServiceManagedAPIsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DiscoveredAPIDTO   {
  
    private String id = null;
    private String serviceIdentity = null;

    @XmlType(name="EnvKindEnum")
    @XmlEnum(String.class)
    public enum EnvKindEnum {
        K8S("k8s"),
        LEGACY("legacy"),
        UNKNOWN("unknown");
        private String value;

        EnvKindEnum (String v) {
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
        public static EnvKindEnum fromValue(String v) {
            for (EnvKindEnum b : EnvKindEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private EnvKindEnum envKind = null;
    private String namespace = null;
    private String serviceName = null;
    private String samplePod = null;
    private String sampleWorkload = null;
    private String method = null;
    private String normalizedPath = null;
    private List<String> rawPathSamples = new ArrayList<String>();

    @XmlType(name="ClassificationEnum")
    @XmlEnum(String.class)
    public enum ClassificationEnum {
        SHADOW("shadow"),
        DRIFT("drift");
        private String value;

        ClassificationEnum (String v) {
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
        public static ClassificationEnum fromValue(String v) {
            for (ClassificationEnum b : ClassificationEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ClassificationEnum classification = null;
    private Boolean isInternal = null;
    private String firstSeenAt = null;
    private String lastSeenAt = null;
    private Long observationCount = null;
    private Integer distinctClientCount = null;
    private List<String> distinctClientsSample = new ArrayList<String>();
    private List<Integer> statusCodes = new ArrayList<Integer>();
    private BigDecimal avgDurationUs = null;
    private List<String> matchedApimApiIds = new ArrayList<String>();
    private List<DiscoveredAPIServiceManagedAPIsDTO> serviceManagedAPIs = new ArrayList<DiscoveredAPIServiceManagedAPIsDTO>();

  /**
   **/
  public DiscoveredAPIDTO id(String id) {
    this.id = id;
    return this;
  }

  
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
  public DiscoveredAPIDTO serviceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceIdentity")
  public String getServiceIdentity() {
    return serviceIdentity;
  }
  public void setServiceIdentity(String serviceIdentity) {
    this.serviceIdentity = serviceIdentity;
  }

  /**
   **/
  public DiscoveredAPIDTO envKind(EnvKindEnum envKind) {
    this.envKind = envKind;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("envKind")
  public EnvKindEnum getEnvKind() {
    return envKind;
  }
  public void setEnvKind(EnvKindEnum envKind) {
    this.envKind = envKind;
  }

  /**
   **/
  public DiscoveredAPIDTO namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("namespace")
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   **/
  public DiscoveredAPIDTO serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("serviceName")
  public String getServiceName() {
    return serviceName;
  }
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   **/
  public DiscoveredAPIDTO samplePod(String samplePod) {
    this.samplePod = samplePod;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("samplePod")
  public String getSamplePod() {
    return samplePod;
  }
  public void setSamplePod(String samplePod) {
    this.samplePod = samplePod;
  }

  /**
   **/
  public DiscoveredAPIDTO sampleWorkload(String sampleWorkload) {
    this.sampleWorkload = sampleWorkload;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sampleWorkload")
  public String getSampleWorkload() {
    return sampleWorkload;
  }
  public void setSampleWorkload(String sampleWorkload) {
    this.sampleWorkload = sampleWorkload;
  }

  /**
   **/
  public DiscoveredAPIDTO method(String method) {
    this.method = method;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("method")
  public String getMethod() {
    return method;
  }
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   **/
  public DiscoveredAPIDTO normalizedPath(String normalizedPath) {
    this.normalizedPath = normalizedPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("normalizedPath")
  public String getNormalizedPath() {
    return normalizedPath;
  }
  public void setNormalizedPath(String normalizedPath) {
    this.normalizedPath = normalizedPath;
  }

  /**
   **/
  public DiscoveredAPIDTO rawPathSamples(List<String> rawPathSamples) {
    this.rawPathSamples = rawPathSamples;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rawPathSamples")
  public List<String> getRawPathSamples() {
    return rawPathSamples;
  }
  public void setRawPathSamples(List<String> rawPathSamples) {
    this.rawPathSamples = rawPathSamples;
  }

  /**
   **/
  public DiscoveredAPIDTO classification(ClassificationEnum classification) {
    this.classification = classification;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("classification")
  public ClassificationEnum getClassification() {
    return classification;
  }
  public void setClassification(ClassificationEnum classification) {
    this.classification = classification;
  }

  /**
   **/
  public DiscoveredAPIDTO isInternal(Boolean isInternal) {
    this.isInternal = isInternal;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isInternal")
  public Boolean isIsInternal() {
    return isInternal;
  }
  public void setIsInternal(Boolean isInternal) {
    this.isInternal = isInternal;
  }

  /**
   **/
  public DiscoveredAPIDTO firstSeenAt(String firstSeenAt) {
    this.firstSeenAt = firstSeenAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("firstSeenAt")
  public String getFirstSeenAt() {
    return firstSeenAt;
  }
  public void setFirstSeenAt(String firstSeenAt) {
    this.firstSeenAt = firstSeenAt;
  }

  /**
   **/
  public DiscoveredAPIDTO lastSeenAt(String lastSeenAt) {
    this.lastSeenAt = lastSeenAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastSeenAt")
  public String getLastSeenAt() {
    return lastSeenAt;
  }
  public void setLastSeenAt(String lastSeenAt) {
    this.lastSeenAt = lastSeenAt;
  }

  /**
   **/
  public DiscoveredAPIDTO observationCount(Long observationCount) {
    this.observationCount = observationCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("observationCount")
  public Long getObservationCount() {
    return observationCount;
  }
  public void setObservationCount(Long observationCount) {
    this.observationCount = observationCount;
  }

  /**
   **/
  public DiscoveredAPIDTO distinctClientCount(Integer distinctClientCount) {
    this.distinctClientCount = distinctClientCount;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("distinctClientCount")
  public Integer getDistinctClientCount() {
    return distinctClientCount;
  }
  public void setDistinctClientCount(Integer distinctClientCount) {
    this.distinctClientCount = distinctClientCount;
  }

  /**
   **/
  public DiscoveredAPIDTO distinctClientsSample(List<String> distinctClientsSample) {
    this.distinctClientsSample = distinctClientsSample;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("distinctClientsSample")
  public List<String> getDistinctClientsSample() {
    return distinctClientsSample;
  }
  public void setDistinctClientsSample(List<String> distinctClientsSample) {
    this.distinctClientsSample = distinctClientsSample;
  }

  /**
   **/
  public DiscoveredAPIDTO statusCodes(List<Integer> statusCodes) {
    this.statusCodes = statusCodes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("statusCodes")
  public List<Integer> getStatusCodes() {
    return statusCodes;
  }
  public void setStatusCodes(List<Integer> statusCodes) {
    this.statusCodes = statusCodes;
  }

  /**
   **/
  public DiscoveredAPIDTO avgDurationUs(BigDecimal avgDurationUs) {
    this.avgDurationUs = avgDurationUs;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("avgDurationUs")
  public BigDecimal getAvgDurationUs() {
    return avgDurationUs;
  }
  public void setAvgDurationUs(BigDecimal avgDurationUs) {
    this.avgDurationUs = avgDurationUs;
  }

  /**
   **/
  public DiscoveredAPIDTO matchedApimApiIds(List<String> matchedApimApiIds) {
    this.matchedApimApiIds = matchedApimApiIds;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("matchedApimApiIds")
  public List<String> getMatchedApimApiIds() {
    return matchedApimApiIds;
  }
  public void setMatchedApimApiIds(List<String> matchedApimApiIds) {
    this.matchedApimApiIds = matchedApimApiIds;
  }

  /**
   **/
  public DiscoveredAPIDTO serviceManagedAPIs(List<DiscoveredAPIServiceManagedAPIsDTO> serviceManagedAPIs) {
    this.serviceManagedAPIs = serviceManagedAPIs;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("serviceManagedAPIs")
  public List<DiscoveredAPIServiceManagedAPIsDTO> getServiceManagedAPIs() {
    return serviceManagedAPIs;
  }
  public void setServiceManagedAPIs(List<DiscoveredAPIServiceManagedAPIsDTO> serviceManagedAPIs) {
    this.serviceManagedAPIs = serviceManagedAPIs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoveredAPIDTO discoveredAPI = (DiscoveredAPIDTO) o;
    return Objects.equals(id, discoveredAPI.id) &&
        Objects.equals(serviceIdentity, discoveredAPI.serviceIdentity) &&
        Objects.equals(envKind, discoveredAPI.envKind) &&
        Objects.equals(namespace, discoveredAPI.namespace) &&
        Objects.equals(serviceName, discoveredAPI.serviceName) &&
        Objects.equals(samplePod, discoveredAPI.samplePod) &&
        Objects.equals(sampleWorkload, discoveredAPI.sampleWorkload) &&
        Objects.equals(method, discoveredAPI.method) &&
        Objects.equals(normalizedPath, discoveredAPI.normalizedPath) &&
        Objects.equals(rawPathSamples, discoveredAPI.rawPathSamples) &&
        Objects.equals(classification, discoveredAPI.classification) &&
        Objects.equals(isInternal, discoveredAPI.isInternal) &&
        Objects.equals(firstSeenAt, discoveredAPI.firstSeenAt) &&
        Objects.equals(lastSeenAt, discoveredAPI.lastSeenAt) &&
        Objects.equals(observationCount, discoveredAPI.observationCount) &&
        Objects.equals(distinctClientCount, discoveredAPI.distinctClientCount) &&
        Objects.equals(distinctClientsSample, discoveredAPI.distinctClientsSample) &&
        Objects.equals(statusCodes, discoveredAPI.statusCodes) &&
        Objects.equals(avgDurationUs, discoveredAPI.avgDurationUs) &&
        Objects.equals(matchedApimApiIds, discoveredAPI.matchedApimApiIds) &&
        Objects.equals(serviceManagedAPIs, discoveredAPI.serviceManagedAPIs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, serviceIdentity, envKind, namespace, serviceName, samplePod, sampleWorkload, method, normalizedPath, rawPathSamples, classification, isInternal, firstSeenAt, lastSeenAt, observationCount, distinctClientCount, distinctClientsSample, statusCodes, avgDurationUs, matchedApimApiIds, serviceManagedAPIs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoveredAPIDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    serviceIdentity: ").append(toIndentedString(serviceIdentity)).append("\n");
    sb.append("    envKind: ").append(toIndentedString(envKind)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
    sb.append("    samplePod: ").append(toIndentedString(samplePod)).append("\n");
    sb.append("    sampleWorkload: ").append(toIndentedString(sampleWorkload)).append("\n");
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    normalizedPath: ").append(toIndentedString(normalizedPath)).append("\n");
    sb.append("    rawPathSamples: ").append(toIndentedString(rawPathSamples)).append("\n");
    sb.append("    classification: ").append(toIndentedString(classification)).append("\n");
    sb.append("    isInternal: ").append(toIndentedString(isInternal)).append("\n");
    sb.append("    firstSeenAt: ").append(toIndentedString(firstSeenAt)).append("\n");
    sb.append("    lastSeenAt: ").append(toIndentedString(lastSeenAt)).append("\n");
    sb.append("    observationCount: ").append(toIndentedString(observationCount)).append("\n");
    sb.append("    distinctClientCount: ").append(toIndentedString(distinctClientCount)).append("\n");
    sb.append("    distinctClientsSample: ").append(toIndentedString(distinctClientsSample)).append("\n");
    sb.append("    statusCodes: ").append(toIndentedString(statusCodes)).append("\n");
    sb.append("    avgDurationUs: ").append(toIndentedString(avgDurationUs)).append("\n");
    sb.append("    matchedApimApiIds: ").append(toIndentedString(matchedApimApiIds)).append("\n");
    sb.append("    serviceManagedAPIs: ").append(toIndentedString(serviceManagedAPIs)).append("\n");
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

