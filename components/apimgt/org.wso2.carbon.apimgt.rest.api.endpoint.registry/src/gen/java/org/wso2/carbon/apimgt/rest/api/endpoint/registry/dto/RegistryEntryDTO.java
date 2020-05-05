package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;

    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class RegistryEntryDTO   {
    private String entryName = null;
    private String serviceUrl = null;
            @XmlType(name="ServiceTypeEnum")
            @XmlEnum(String.class)
            public enum ServiceTypeEnum {
            
                @XmlEnumValue("REST") REST(String.valueOf("REST")), @XmlEnumValue("SOAP_1_1") SOAP_1_1(String.valueOf("SOAP_1_1")), @XmlEnumValue("GQL") GQL(String.valueOf("GQL")), @XmlEnumValue("WS") WS(String.valueOf("WS"));
            
            
            private String value;
            
            ServiceTypeEnum (String v) {
            value = v;
            }
            
            public String value() {
            return value;
            }
            
            @Override
            public String toString() {
            return String.valueOf(value);
            }
            
            public static ServiceTypeEnum fromValue(String v) {
            for (ServiceTypeEnum b : ServiceTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
            return b;
            }
            }
            return null;
            }
            }    private ServiceTypeEnum serviceType = null;
            @XmlType(name="DefinitionTypeEnum")
            @XmlEnum(String.class)
            public enum DefinitionTypeEnum {
            
                @XmlEnumValue("OAS") OAS(String.valueOf("OAS")), @XmlEnumValue("WSDL1") WSDL1(String.valueOf("WSDL1")), @XmlEnumValue("WSDL2") WSDL2(String.valueOf("WSDL2")), @XmlEnumValue("GQL-SDL") GQL_SDL(String.valueOf("GQL-SDL"));
            
            
            private String value;
            
            DefinitionTypeEnum (String v) {
            value = v;
            }
            
            public String value() {
            return value;
            }
            
            @Override
            public String toString() {
            return String.valueOf(value);
            }
            
            public static DefinitionTypeEnum fromValue(String v) {
            for (DefinitionTypeEnum b : DefinitionTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
            return b;
            }
            }
            return null;
            }
            }    private DefinitionTypeEnum definitionType = null;
    private String definitionUrl = null;
    private String metadata = null;

    /**
    **/
    public RegistryEntryDTO entryName(String entryName) {
    this.entryName = entryName;
    return this;
    }

    
    
    @Schema(example = "Pizzashack-Endpoint", description = "")
    @JsonProperty("entryName")
          public String getEntryName() {
    return entryName;
    }
    public void setEntryName(String entryName) {
    this.entryName = entryName;
    }

    /**
    **/
    public RegistryEntryDTO serviceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
    }

    
    
    @Schema(example = "http://localhost/pizzashack", description = "")
    @JsonProperty("service-url")
          public String getServiceUrl() {
    return serviceUrl;
    }
    public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    }

    /**
        * Type of the backend connecting to
    **/
    public RegistryEntryDTO serviceType(ServiceTypeEnum serviceType) {
    this.serviceType = serviceType;
    return this;
    }

    
    
    @Schema(example = "REST", description = "Type of the backend connecting to")
    @JsonProperty("serviceType")
          public ServiceTypeEnum getServiceType() {
    return serviceType;
    }
    public void setServiceType(ServiceTypeEnum serviceType) {
    this.serviceType = serviceType;
    }

    /**
        * The type of the provided API definition
    **/
    public RegistryEntryDTO definitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    return this;
    }

    
    
    @Schema(example = "OAS", description = "The type of the provided API definition")
    @JsonProperty("definitionType")
          public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
    }
    public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    }

    /**
    **/
    public RegistryEntryDTO definitionUrl(String definitionUrl) {
    this.definitionUrl = definitionUrl;
    return this;
    }

    
    
    @Schema(example = "http://localhost/pizzashack?swagger.json", description = "")
    @JsonProperty("definitionUrl")
          public String getDefinitionUrl() {
    return definitionUrl;
    }
    public void setDefinitionUrl(String definitionUrl) {
    this.definitionUrl = definitionUrl;
    }

    /**
    **/
    public RegistryEntryDTO metadata(String metadata) {
    this.metadata = metadata;
    return this;
    }

    
    
    @Schema(example = "{ \"mutualTLS\" : true }", description = "")
    @JsonProperty("metadata")
          public String getMetadata() {
    return metadata;
    }
    public void setMetadata(String metadata) {
    this.metadata = metadata;
    }


@Override
public boolean equals(java.lang.Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
RegistryEntryDTO registryEntry = (RegistryEntryDTO) o;
    return Objects.equals(entryName, registryEntry.entryName) &&
    Objects.equals(serviceUrl, registryEntry.serviceUrl) &&
    Objects.equals(serviceType, registryEntry.serviceType) &&
    Objects.equals(definitionType, registryEntry.definitionType) &&
    Objects.equals(definitionUrl, registryEntry.definitionUrl) &&
    Objects.equals(metadata, registryEntry.metadata);
}

@Override
public int hashCode() {
return Objects.hash(entryName, serviceUrl, serviceType, definitionType, definitionUrl, metadata);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class RegistryEntryDTO {\n");

sb.append("    entryName: ").append(toIndentedString(entryName)).append("\n");
sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
sb.append("    serviceType: ").append(toIndentedString(serviceType)).append("\n");
sb.append("    definitionType: ").append(toIndentedString(definitionType)).append("\n");
sb.append("    definitionUrl: ").append(toIndentedString(definitionUrl)).append("\n");
sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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