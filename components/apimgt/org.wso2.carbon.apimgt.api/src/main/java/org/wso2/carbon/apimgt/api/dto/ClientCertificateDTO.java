/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.dto;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;

/**
 * DTO object to represent client certificate.
 */
public class ClientCertificateDTO {
    private String alias;
    private String certificate;
    private String uniqueId;
    private String tierName;
    private APIIdentifier apiIdentifier;

    /**
     * To get the identifier of the API related with client certificate.
     *
     * @return API Identifier related with the client certificate.
     */
    public APIIdentifier getApiIdentifier() {
        return apiIdentifier;
    }

    /**
     * To set the identifier of the API related with client certificate.
     *
     * @param apiIdentifier Identifier of the API.
     */
    public void setApiIdentifier(APIIdentifier apiIdentifier) {
        this.apiIdentifier = apiIdentifier;
    }

    /**
     * To get the tier name which the certificate is subscribed to.
     * @return tier name.
     */
    public String getTierName() {
        return tierName;
    }

    /**
     * To set the subscription tier for the current certificate.
     *
     * @param tierName Name of the tier.
     */
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    /**
     * To get the alias of the certificate.
     *
     * @return alias of the certificate.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * To set the alias of the certificate.
     *
     * @param alias Specific alias.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * To get the certificate content.
     *
     * @return certificate content.
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * To set the certificate content.
     *
     * @param certificate certificate content.
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
