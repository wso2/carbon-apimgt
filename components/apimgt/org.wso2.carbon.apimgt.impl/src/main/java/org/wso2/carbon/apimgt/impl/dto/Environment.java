/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import joptsimple.internal.Strings;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment extends org.wso2.carbon.apimgt.api.model.Environment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type = APIConstants.GATEWAY_ENV_TYPE_HYBRID;
    
    private String serverURL;
    
    private String userName;
    
    private String password;

    private String apiGatewayEndpoint;

    private String websocketGatewayEndpoint;

    private boolean isDefault;

    public static Environment newFromModel(org.wso2.carbon.apimgt.api.model.Environment env){
        Environment object = new Environment();
        object.setId(env.getId());
        object.setUuid(env.getUuid());
        object.setName(env.getName());
        object.setDisplayName(env.getDisplayName());
        object.setDescription(env.getDescription());
        object.setVhosts(env.getVhosts());
        object.setReadOnly(env.isReadOnly());
        return object;
    }

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

    @Override
    public void setName(String name) {
        super.setName(name);
        super.setUuid(name);
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

    public void setVhosts(List<VHost> vhosts) {
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
        super.setVhosts(vhosts);
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
