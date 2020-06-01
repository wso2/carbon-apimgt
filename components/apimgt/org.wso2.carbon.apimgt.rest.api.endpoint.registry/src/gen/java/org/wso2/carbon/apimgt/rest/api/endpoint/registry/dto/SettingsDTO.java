package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class SettingsDTO   {
    private List<String> scopes = new ArrayList<>();

    /**
    **/
    public SettingsDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
    }

    
    
    @Schema(description = "")
    @JsonProperty("scopes")
          public List<String> getScopes() {
    return scopes;
    }
    public void setScopes(List<String> scopes) {
    this.scopes = scopes;
    }


@Override
public boolean equals(java.lang.Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
SettingsDTO settings = (SettingsDTO) o;
    return Objects.equals(scopes, settings.scopes);
}

@Override
public int hashCode() {
return Objects.hash(scopes);
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class SettingsDTO {\n");

sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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