/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.wso2.carbon.apimgt.api.model.EndpointSecurity;

import java.io.Serializable;

public class EndpointSecurityModel extends EndpointSecurity implements Serializable {

    private String alias;
    private String base64EncodedPassword;


    public String getAlias() {

        return alias;
    }

    public void setAlias(String alias) {

        this.alias = alias;
    }

    public String getBase64EncodedPassword() {

        return base64EncodedPassword;
    }

    public void setBase64EncodedPassword(String base64EncodedPassword) {

        this.base64EncodedPassword = base64EncodedPassword;
    }

}
