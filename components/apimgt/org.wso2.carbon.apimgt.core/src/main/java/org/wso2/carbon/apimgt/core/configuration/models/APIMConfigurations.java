package org.wso2.carbon.apimgt.core.configuration.models;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.io.File;

/**
 * Class to hold APIM configuration parameters and generate yaml file
 */

@Configuration(namespace = "wso2.carbon.apimgt", description = "APIM Configuration Parameters")
public class APIMConfigurations {
    private String carbonClientId = "carbon";
    @Element(description = "server version")
    private String carbonVirtualHostName = "carbon";
    @Element(description = "topic server host")
    private String topicServerHost = "localhost";
    @Element(description = "topic server port")
    private String topicServerPort = "5672";
    @Element(description = "publisher topic name")
    private String publisherTopic = "PublisherTopic";
    @Element(description = "store topic name")
    private String storeTopic = "StoreTopic";
    @Element(description = "username for topic")
    private String username = "admin";
    @Element(description = "password for topic")
    private String password = "admin";
    @Element(description = "password for topic")
    private boolean reverseProxyEnabled = false;
    @Element(description = "hostname")
    private String hostname = "localhost";
    @Element(description = "context for publisher")
    private String publisherContext = "/api/am/publisher/v1.0";
    @Element(description = "context for store")
    private String storeContext = "/api/am/store/v1.0";
    @Element(description = "context for admin")
    private String adminContext = "/api/am/admin/v1.0";

    @Element(description = "package name for gateway configs")
    private String gatewayPackageName = "deployment.org.wso2.apim";

    @Element(description = "package name path for gateway configs")
    private String gatewayPackageNamePath =
            "deployment" + File.separator + "org" + File.separator + "wso2" + File.separator + "apim";

    @Element(description = "label extractor")
    private String labelExtractorImplClass = "org.wso2.carbon.apimgt.core.impl.DefaultLabelExtractorImpl";

    @Element(description = "Key Manager Configurations")
    private KeyMgtConfigurations keyManagerConfigs = new KeyMgtConfigurations();

    @Element(description = "Identity Provider Configurations")
    private IdentityProviderConfigurations identityProviderConfigs = new IdentityProviderConfigurations();

    @Element(description = "comment moderator role")
    private String commentModeratorRole = "comment-moderator";

    @Element(description = "comment text max length")
    private int commentMaxLength = 30;

    @Element(description = "rating upper limit")
    private int ratingMaxValue = 5;

    public String getHostname() {
        return hostname;
    }

    public boolean isReverseProxyEnabled() {
        return reverseProxyEnabled;
    }

    public String getLabelExtractorImplClass() {
        return labelExtractorImplClass;
    }

    public String getPublisherContext() {
        return publisherContext;
    }

    public String getStoreContext() {
        return storeContext;
    }

    public String getAdminContext() {
        return adminContext;
    }

    public String getCarbonClientId() {
        return carbonClientId;
    }

    public String getCarbonVirtualHostName() {
        return carbonVirtualHostName;
    }

    public String getTopicServerHost() {
        return topicServerHost;
    }

    public String getTopicServerPort() {
        return topicServerPort;
    }

    public String getPublisherTopic() {
        return publisherTopic;
    }

    public String getStoreTopic() {
        return storeTopic;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getGatewayPackageName() {
        return gatewayPackageName;
    }

    public String getGatewayPackageNamePath() {
        return gatewayPackageNamePath;
    }

    public KeyMgtConfigurations getKeyManagerConfigs() {
        return keyManagerConfigs;
    }

    public IdentityProviderConfigurations getIdentityProviderConfigs() {
        return identityProviderConfigs;
    }

    public String getCommentModeratorRole() {
        return commentModeratorRole;
    }

    public int getRatingMaxValue() {
        return ratingMaxValue;
    }

    public int getCommentMaxLength() {
        return commentMaxLength;
    }
}
