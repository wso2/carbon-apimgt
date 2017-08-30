/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;


/**
 * Class to hold New API Version created Notification parameters.
 */
@Configuration(description = "API Version Notifier Configurations")
public class NewVersionNotifierConfiguration {


    @Element(description = "properties passed to the executor")
    private String notifierTemplate = "{\"Notifier\" :[{" +
            "\"Class\":\"org.wso2.carbon.apimgt.core.impl.NewApiVersionMailNotifier\"," +
            "\"Title\":\"Version $2 of $1 Released\"," +
            "\"Template\": \" <html> <body> <h3 style=\\\"color:Black;\\\">" +
            "We’re happy to announce the arrival of the next major version $2 of $1 API " +
            "which is now available in Our API Store.</h3><a href=\\\"https://localhost:9443/store\\\">" +
            "Click here to Visit WSO2 API Store</a></body></html>\"" +
            "}]" +
            "}";

    public String getNotifierTemplate() {
        return notifierTemplate;
    }

    public void setNotifierTemplate(String notifierTemplate) {
        this.notifierTemplate = notifierTemplate;
    }
}
