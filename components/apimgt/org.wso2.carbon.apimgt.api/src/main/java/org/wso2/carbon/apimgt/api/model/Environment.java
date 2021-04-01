/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an Environment.
 */
public class Environment implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type = APIConstants.GATEWAY_ENV_TYPE_HYBRID;
    private String serverURL;
    private String userName;
    private String password;
    private String apiGatewayEndpoint;
    private String websocketGatewayEndpoint;
    private boolean isDefault;

    // New fields added with dynamic environments
    private Integer id;
    private String uuid;
    private String name;
    private String displayName;
    private String description;
    private boolean isReadOnly;
    private List<VHost> vhosts = new ArrayList<>();

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getWebsocketGatewayEndpoint() {
        return websocketGatewayEndpoint;
    }

    public void setWebsocketGatewayEndpoint(String websocketGatewayEndpoint) {
        this.websocketGatewayEndpoint = websocketGatewayEndpoint;
    }

    public boolean isShowInConsole() {
        return showInConsole;
    }

    public void setShowInConsole(boolean showInConsole) {
        this.showInConsole = showInConsole;
    }

    private boolean showInConsole = true;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiGatewayEndpoint() {
        return apiGatewayEndpoint;
    }

    public void setApiGatewayEndpoint(String apiGatewayEndpoint) {
        this.apiGatewayEndpoint = apiGatewayEndpoint;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (StringUtils.isEmpty(this.uuid)) {
            this.uuid = name;
        }
        if (StringUtils.isEmpty(this.displayName)) {
            this.displayName = name;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public List<VHost> getVhosts() {
        return vhosts;
    }

    public void setVhosts(List<VHost> vhosts) {
        this.vhosts = vhosts;
        // set gateway endpoint if it is empty
        if (StringUtils.isEmpty(apiGatewayEndpoint) && StringUtils.isEmpty(websocketGatewayEndpoint) && !vhosts.isEmpty()) {
            VHost vhost = vhosts.get(0);
            String endpointFormat = "%s%s:%s%s"; // {protocol}://{host}:{port}/{context}

            String httpContext = StringUtils.isEmpty(vhost.getHttpContext()) ? "" : "/" + vhost.getHttpContext();
            String gwHttpEndpoint = String.format(endpointFormat, APIConstants.HTTP_PROTOCOL_URL_PREFIX,
                    vhost.getHost(), vhost.getHttpPort(), httpContext);
            String gwHttpsEndpoint = String.format(endpointFormat, APIConstants.HTTPS_PROTOCOL_URL_PREFIX,
                    vhost.getHost(), vhost.getHttpsPort(), httpContext);
            apiGatewayEndpoint = gwHttpsEndpoint + "," + gwHttpEndpoint;

            String gwWsEndpoint = String.format(endpointFormat, APIConstants.WS_PROTOCOL_URL_PREFIX,
                    vhost.getHost(), vhost.getWsPort(), "");
            String gwWssEndpoint = String.format(endpointFormat, APIConstants.WSS_PROTOCOL_URL_PREFIX,
                    vhost.getHost(), vhost.getWssPort(), "");
            websocketGatewayEndpoint = gwWssEndpoint + "," + gwWsEndpoint;
        }
    }

    public void setEndpointsAsVhost() throws APIManagementException {
        String[] endpoints = (apiGatewayEndpoint + "," + websocketGatewayEndpoint).split(",", 4);
        getVhosts().add(VHost.fromEndpointUrls(endpoints));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        if (!getName().equals(that.getName())) return false;
        if (!type.equals(that.getType())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        return  31 * result + getName().hashCode();
    }
}
