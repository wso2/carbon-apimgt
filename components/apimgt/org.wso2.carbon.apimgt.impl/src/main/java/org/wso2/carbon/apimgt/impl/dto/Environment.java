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

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.Serializable;

public class Environment implements Serializable {
    
    private String type = APIConstants.GATEWAY_ENV_TYPE_HYBRID;
    
    private String name;
    
    private String serverURL;
    
    private String userName;
    
    private String password;

    private String apiGatewayEndpoint;

    private String websocketGatewayEndpoint;

    private String description;

    private boolean isDefault;

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

    private boolean showInConsole;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        if (!name.equals(that.getName())) return false;
        if (!type.equals(that.getType())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        return  31 * result + name.hashCode();
    }
}
