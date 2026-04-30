package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DiscoveredAPITopClientsDTO   {
  
    private String identity = null;

    @XmlType(name="KindEnum")
    @XmlEnum(String.class)
    public enum KindEnum {
        K8S("k8s"),
        LEGACY("legacy");
        private String value;

        KindEnum (String v) {
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
        public static KindEnum fromValue(String v) {
            for (KindEnum b : KindEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private KindEnum kind = null;
    private String namespace = null;
    private String workload = null;
    private String ip = null;
    private Integer port = null;
    private Long observations = null;

  /**
   * Stable identity. \&quot;k8s:&lt;namespace&gt;/&lt;workload&gt;\&quot; for K8s pods or \&quot;host:&lt;ip&gt;\&quot; for legacy hosts.
   **/
  public DiscoveredAPITopClientsDTO identity(String identity) {
    this.identity = identity;
    return this;
  }

  
  @ApiModelProperty(value = "Stable identity. \"k8s:<namespace>/<workload>\" for K8s pods or \"host:<ip>\" for legacy hosts.")
  @JsonProperty("identity")
  public String getIdentity() {
    return identity;
  }
  public void setIdentity(String identity) {
    this.identity = identity;
  }

  /**
   **/
  public DiscoveredAPITopClientsDTO kind(KindEnum kind) {
    this.kind = kind;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("kind")
  public KindEnum getKind() {
    return kind;
  }
  public void setKind(KindEnum kind) {
    this.kind = kind;
  }

  /**
   * K8s namespace (only when kind &#x3D; k8s).
   **/
  public DiscoveredAPITopClientsDTO namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  
  @ApiModelProperty(value = "K8s namespace (only when kind = k8s).")
  @JsonProperty("namespace")
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * K8s workload name (only when kind &#x3D; k8s).
   **/
  public DiscoveredAPITopClientsDTO workload(String workload) {
    this.workload = workload;
    return this;
  }

  
  @ApiModelProperty(value = "K8s workload name (only when kind = k8s).")
  @JsonProperty("workload")
  public String getWorkload() {
    return workload;
  }
  public void setWorkload(String workload) {
    this.workload = workload;
  }

  /**
   * Caller IP address.
   **/
  public DiscoveredAPITopClientsDTO ip(String ip) {
    this.ip = ip;
    return this;
  }

  
  @ApiModelProperty(value = "Caller IP address.")
  @JsonProperty("ip")
  public String getIp() {
    return ip;
  }
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Sample source port. Source ports rotate per connection so this is illustrative only.
   **/
  public DiscoveredAPITopClientsDTO port(Integer port) {
    this.port = port;
    return this;
  }

  
  @ApiModelProperty(value = "Sample source port. Source ports rotate per connection so this is illustrative only.")
  @JsonProperty("port")
  public Integer getPort() {
    return port;
  }
  public void setPort(Integer port) {
    this.port = port;
  }

  /**
   * Observation count for this caller in the latest cycle window.
   **/
  public DiscoveredAPITopClientsDTO observations(Long observations) {
    this.observations = observations;
    return this;
  }

  
  @ApiModelProperty(value = "Observation count for this caller in the latest cycle window.")
  @JsonProperty("observations")
  public Long getObservations() {
    return observations;
  }
  public void setObservations(Long observations) {
    this.observations = observations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoveredAPITopClientsDTO discoveredAPITopClients = (DiscoveredAPITopClientsDTO) o;
    return Objects.equals(identity, discoveredAPITopClients.identity) &&
        Objects.equals(kind, discoveredAPITopClients.kind) &&
        Objects.equals(namespace, discoveredAPITopClients.namespace) &&
        Objects.equals(workload, discoveredAPITopClients.workload) &&
        Objects.equals(ip, discoveredAPITopClients.ip) &&
        Objects.equals(port, discoveredAPITopClients.port) &&
        Objects.equals(observations, discoveredAPITopClients.observations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identity, kind, namespace, workload, ip, port, observations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoveredAPITopClientsDTO {\n");
    
    sb.append("    identity: ").append(toIndentedString(identity)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    workload: ").append(toIndentedString(workload)).append("\n");
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    observations: ").append(toIndentedString(observations)).append("\n");
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

