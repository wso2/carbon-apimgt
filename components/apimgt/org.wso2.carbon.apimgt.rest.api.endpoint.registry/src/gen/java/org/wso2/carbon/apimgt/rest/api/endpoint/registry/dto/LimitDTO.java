package org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
    /**
    * Maximum limit of items to return. 
    **/
    import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

import javax.xml.bind.annotation.*;

@Schema(description = "Maximum limit of items to return. ")

public class LimitDTO   {


@Override
public boolean equals(java.lang.Object o) {
if (this == o) {
return true;
}
if (o == null || getClass() != o.getClass()) {
return false;
}
LimitDTO limit = (LimitDTO) o;
    return true;
}

@Override
public int hashCode() {
return Objects.hash();
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append("class LimitDTO {\n");

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