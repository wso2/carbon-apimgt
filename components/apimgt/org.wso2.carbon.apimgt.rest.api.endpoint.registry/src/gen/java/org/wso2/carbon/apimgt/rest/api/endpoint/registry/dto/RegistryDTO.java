package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;

    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class RegistryDTO   {
    private String name = null;
    private String id = null;
            @XmlType(name="TypeEnum")
            @XmlEnum(String.class)
            public enum TypeEnum {
            
                @XmlEnumValue("wso2") WSO2(String.valueOf("wso2")), @XmlEnumValue("k8") K8(String.valueOf("k8")), @XmlEnumValue("etcd") ETCD(String.valueOf("etcd"));
            
            
            private String value;
            
            TypeEnum (String v) {
            value = v;
            }
            
            public String value() {
            return value;
            }
            
            @Override
            public String toString() {
            return String.valueOf(value);
            }
            
            public static TypeEnum fromValue(String v) {
            for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
            return b;
            }
            }
            return null;
            }
            }    private TypeEnum type = null;
            @XmlType(name="ModeEnum")
            @XmlEnum(String.class)
            public enum ModeEnum {
            
                @XmlEnumValue("Read_Only") ONLY(String.valueOf("Read_Only")), @XmlEnumValue("Read_Write") WRITE(String.valueOf("Read_Write"));
            
            
            private String value;
            
            ModeEnum (String v) {
            value = v;
            }
            
            public String value() {
            return value;
            }
            
            @Override
            public String toString() {
            return String.valueOf(value);
            }
            
            public static ModeEnum fromValue(String v) {
            for (ModeEnum b : ModeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
            return b;
            }
            }
            return null;
            }
            }    private ModeEnum mode = null;

    /**
    **/
    public RegistryDTO name(String name) {
    this.name = name;
    return this;
    }

    
    
    @Schema(example = "Dev Registry", description = "")
    @JsonProperty("name")
          public String getName() {
    return name;
    }
    public void setName(String name) {
    this.name = name;
    }

    /**
    **/
    public RegistryDTO id(String id) {
    this.id = id;
    return this;
    }

    
    
    @Schema(example = "01234567-0123-0123-0123-012345678901", description = "")
    @JsonProperty("id")
          public String getId() {
    return id;
    }
    public void setId(String id) {
    this.id = id;
    }

    /**
    **/
    public RegistryDTO type(TypeEnum type) {
    this.type = type;
    return this;
    }

    
    
    @Schema(example = "wso2", description = "")
    @JsonProperty("type")
          public TypeEnum getType() {
    return type;
    }
    public void setType(TypeEnum type) {
    this.type = type;
    }

    /**
    **/
    public RegistryDTO mode(ModeEnum mode) {
    this.mode = mode;
    return this;
    }

    
    
    @Schema(example = "Read_Only", description = "")
    @JsonProperty("mode")
          public ModeEnum getMode() {
    return mode;
    }
    public void setMode(ModeEnum mode) {
    this.mode = mode;
    }


@Override
public boolean equals(java.lang.Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
RegistryDTO registry = (RegistryDTO) o;
    return Objects.equals(name, registry.name) &&
    Objects.equals(id, registry.id) &&
    Objects.equals(type, registry.type) &&
    Objects.equals(mode, registry.mode);
}

@Override
public int hashCode() {
return Objects.hash(name, id, type, mode);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class RegistryDTO {\n");

sb.append("    name: ").append(toIndentedString(name)).append("\n");
sb.append("    id: ").append(toIndentedString(id)).append("\n");
sb.append("    type: ").append(toIndentedString(type)).append("\n");
sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
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