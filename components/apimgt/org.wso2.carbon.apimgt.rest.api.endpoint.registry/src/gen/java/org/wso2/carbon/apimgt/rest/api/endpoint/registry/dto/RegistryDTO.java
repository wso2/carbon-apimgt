package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;

    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class RegistryDTO   {
    private String name = null;
    private String displayName = null;
    private String id = null;
            @XmlType(name="TypeEnum")
            @XmlEnum(String.class)
            public enum TypeEnum {
            
                @XmlEnumValue("wso2") WSO2(String.valueOf("wso2"));
            
            
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
    private String owner = null;

    /**
    **/
    public RegistryDTO name(String name) {
    this.name = name;
    return this;
    }

    
    
    @Schema(required = true, description = "")
    @JsonProperty("name")
            @NotNull
      public String getName() {
    return name;
    }
    public void setName(String name) {
    this.name = name;
    }

    /**
    **/
    public RegistryDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
    }

    
    
    @Schema(example = "Dev Registry", description = "")
    @JsonProperty("displayName")
          public String getDisplayName() {
    return displayName;
    }
    public void setDisplayName(String displayName) {
    this.displayName = displayName;
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
    public RegistryDTO owner(String owner) {
    this.owner = owner;
    return this;
    }

    
    
    @Schema(example = "admin", description = "")
    @JsonProperty("owner")
          public String getOwner() {
    return owner;
    }
    public void setOwner(String owner) {
    this.owner = owner;
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
    Objects.equals(displayName, registry.displayName) &&
    Objects.equals(id, registry.id) &&
    Objects.equals(type, registry.type) &&
    Objects.equals(owner, registry.owner);
}

@Override
public int hashCode() {
return Objects.hash(name, displayName, id, type, owner);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class RegistryDTO {\n");

sb.append("    name: ").append(toIndentedString(name)).append("\n");
sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
sb.append("    id: ").append(toIndentedString(id)).append("\n");
sb.append("    type: ").append(toIndentedString(type)).append("\n");
sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
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