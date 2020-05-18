package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import javax.validation.constraints.*;

    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class RegistryEntrySchemaDTO   {
    private RegistryEntryDTO registryEntry = null;
    private File definitionFile = null;

    /**
    **/
    public RegistryEntrySchemaDTO registryEntry(RegistryEntryDTO registryEntry) {
    this.registryEntry = registryEntry;
    return this;
    }

    
    
    @Schema(required = true, description = "")
    @JsonProperty("registryEntry")
            @NotNull
      public RegistryEntryDTO getRegistryEntry() {
    return registryEntry;
    }
    public void setRegistryEntry(RegistryEntryDTO registryEntry) {
    this.registryEntry = registryEntry;
    }

    /**
    **/
    public RegistryEntrySchemaDTO definitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
    return this;
    }

    
    
    @Schema(required = true, description = "")
    @JsonProperty("definitionFile")
            @NotNull
      public File getDefinitionFile() {
    return definitionFile;
    }
    public void setDefinitionFile(File definitionFile) {
    this.definitionFile = definitionFile;
    }


@Override
public boolean equals(java.lang.Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
RegistryEntrySchemaDTO registryEntrySchema = (RegistryEntrySchemaDTO) o;
    return Objects.equals(registryEntry, registryEntrySchema.registryEntry) &&
    Objects.equals(definitionFile, registryEntrySchema.definitionFile);
}

@Override
public int hashCode() {
return Objects.hash(registryEntry, definitionFile);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class RegistryEntrySchemaDTO {\n");

sb.append("    registryEntry: ").append(toIndentedString(registryEntry)).append("\n");
sb.append("    definitionFile: ").append(toIndentedString(definitionFile)).append("\n");
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