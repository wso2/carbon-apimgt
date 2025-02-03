package org.wso2.carbon.apimgt.api.dto;

import com.google.gson.annotations.SerializedName;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;

/**
 * DTO object to represent endpoints.
 */
public class EndpointConfigDTO {

    @SerializedName("endpoint_type")
    private String endpointType;

    @SerializedName("sandbox_endpoints")
    private EndpointUrl sandboxEndpoints;

    @SerializedName("production_endpoints")
    private EndpointUrl productionEndpoints;

    @SerializedName("endpoint_security")
    private EndpointSecurityConfig endpointSecurity;

    public String getEndpointType() {

        return endpointType;
    }

    public void setEndpointType(String endpointType) {

        this.endpointType = endpointType;
    }

    public EndpointUrl getSandboxEndpoints() {

        return sandboxEndpoints;
    }

    public void setSandboxEndpoints(EndpointUrl sandboxEndpoints) {

        this.sandboxEndpoints = sandboxEndpoints;
    }

    public EndpointUrl getProductionEndpoints() {

        return productionEndpoints;
    }

    public void setProductionEndpoints(EndpointUrl productionEndpoints) {

        this.productionEndpoints = productionEndpoints;
    }

    public EndpointSecurityConfig getEndpointSecurity() {

        return endpointSecurity;
    }

    public void setEndpointSecurity(EndpointSecurityConfig endpointSecurity) {

        this.endpointSecurity = endpointSecurity;
    }

    /**
     * Inner class to represent endpoint URLs.
     */
    public static class EndpointUrl {

        @SerializedName("url")
        private String url;

        public String getUrl() {

            return url;
        }

        public void setUrl(String url) {

            this.url = url;
        }
    }

    /**
     * Inner class to represent endpoint security configurations.
     */
    public static class EndpointSecurityConfig {

        @SerializedName("production")
        private EndpointSecurity production;

        @SerializedName("sandbox")
        private EndpointSecurity sandbox;

        public EndpointSecurity getProduction() {

            return production;
        }

        public void setProduction(EndpointSecurity production) {

            this.production = production;
        }

        public EndpointSecurity getSandbox() {

            return sandbox;
        }

        public void setSandbox(EndpointSecurity sandbox) {

            this.sandbox = sandbox;
        }
    }
}
