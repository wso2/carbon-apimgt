/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.keymanager;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.List;

/**
 *The Data-holder for OAuth Applications in the Key Manager Service
 */
public class OAuthApplication {

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("clientSecret")
    private String clientSecret;

    @JsonProperty("redirectURIs")
    private List<URL> redirectURIs;

    @JsonProperty("grantTypes")
    private List<String> grantTypes;

    public OAuthApplication() {
        this.clientName = "";
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public List<URL> getRedirectURIs() {
        return redirectURIs;
    }

    public void setRedirectURIs(List<URL> redirectURIs) {
        this.redirectURIs = redirectURIs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OAuthApplication that = (OAuthApplication) o;

        if (!clientName.equals(that.clientName)) {
            return false;
        }
        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) {
            return false;
        }
        if (clientSecret != null ? !clientSecret.equals(that.clientSecret) : that.clientSecret != null) {
            return false;
        }
        if (redirectURIs != null ? !redirectURIs.equals(that.redirectURIs) : that.redirectURIs != null) {
            return false;
        }
        return grantTypes != null ? grantTypes.equals(that.grantTypes) : that.grantTypes == null;

    }

    @Override
    public int hashCode() {
        return clientName.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OAuthApplication {\n");

        sb.append("    client_name: ").append(toIndentedString(clientName)).append("\n");
        sb.append("    client_id: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    client_secret: ").append(toIndentedString(clientSecret)).append("\n");
        sb.append("    redirect_uris: ").append(toIndentedString(redirectURIs)).append("\n");
        sb.append("    grant_types: ").append(toIndentedString(grantTypes)).append("\n");
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
