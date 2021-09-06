package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

import java.util.Objects;

public class ThirdPartyEnvironmentProtocolURIDTO   {

    private String protocol = null;
    private String endpointURI = null;

    /**
     **/
    public ThirdPartyEnvironmentProtocolURIDTO protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }


    @ApiModelProperty(example = "default", required = true, value = "")
    @JsonProperty("protocol")
    @NotNull
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     **/
    public ThirdPartyEnvironmentProtocolURIDTO endpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
        return this;
    }


    @ApiModelProperty(example = "default", required = true, value = "")
    @JsonProperty("endpointURI")
    @NotNull
    public String getEndpointURI() {
        return endpointURI;
    }
    public void setEndpointURI(String endpointURI) {
        this.endpointURI = endpointURI;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdPartyEnvironmentProtocolURIDTO thirdPartyEnvironmentProtocolURI = (ThirdPartyEnvironmentProtocolURIDTO) o;
        return Objects.equals(protocol, thirdPartyEnvironmentProtocolURI.protocol) &&
                Objects.equals(endpointURI, thirdPartyEnvironmentProtocolURI.endpointURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, endpointURI);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ThirdPartyEnvironmentProtocolURIDTO {\n");

        sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
        sb.append("    endpointURI: ").append(toIndentedString(endpointURI)).append("\n");
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
