package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class EmailDTO {
    private String email = null;

    /**
     * email value
     **/
    @ApiModelProperty(value = "email value")
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EmailDTO {\n");

        sb.append("  email: ").append(email).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
