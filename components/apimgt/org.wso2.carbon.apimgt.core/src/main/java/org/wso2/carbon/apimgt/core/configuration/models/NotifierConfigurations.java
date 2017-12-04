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

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.HashMap;
import java.util.Map;


/**
 * Class to hold Notifier configuration parameters
 */
@Configuration(description = "Notifier Configurations")
public class NotifierConfigurations {

    @Element(description = "Executor Class")
    private String executorClass;

    @Element(description = "Property Map")
    private Map<String, String> propertyList = new HashMap<>();

    public NotifierConfigurations() {
        executorClass = "org.wso2.carbon.apimgt.core.impl.NewApiVersionMailNotifier";
        propertyList.put("Title", "Version $2 of $1 Released");
        propertyList.put("Template", "<html> <body> <h3 style=\"color:Black;\"> We’re happy to announce the arrival" +
                " of the next major version $2 of $1 API  which is now available in Our API Store." +
                "</h3><a href=\"https://localhost:9443/store\">Click here to Visit WSO2 API Store</a></body></html>");
        propertyList.put("mail.smtp.host", "smtp.gmail.com");
        propertyList.put("mail.smtp.auth", "true");
        propertyList.put("mail.smtp.starttls.enable", "true");
        propertyList.put("mail.smtp.port", "587");
        propertyList.put("mail.transport.protocol", "smtp");
    }

    public String getExecutorClass() {
        return executorClass;
    }

    public void setExecutorClass(String executorClass) {
        this.executorClass = executorClass;
    }

    public Map<String, String> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(Map<String, String> propertyList) {
        this.propertyList = propertyList;
    }
}
