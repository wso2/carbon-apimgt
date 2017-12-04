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

/**
 * Class to hold key Mail configurations.
 */
@Configuration(description = "Mail Configurations")
public class MailConfigurations {

    @Element(description = "Hostname")
    private String smtpHostname = "smtp.gmail.com";

    @Element(description = "AuthUser")
    private String smtpAuthUser = "user@gmail.com";

    @Element(description = "AuthPass")
    private String smtpAuthPwd = "userPWD";

    @Element(description = "FromUser")
    private String fromUser = "user@gmail.com";

    @Element(description = "StartTSL")
    private Boolean startTsl = true;

    public String getSmtpHostname() {
        return smtpHostname;
    }

    public void setSmtpHostname(String smtpHostname) {
        this.smtpHostname = smtpHostname;
    }

    public String getSmtpAuthUser() {
        return smtpAuthUser;
    }

    public void setSmtpAuthUser(String smtpAuthUser) {
        this.smtpAuthUser = smtpAuthUser;
    }

    public String getSmtpAuthPwd() {
        return smtpAuthPwd;
    }

    public void setSmtpAuthPwd(String smtpAuthPwd) {
        this.smtpAuthPwd = smtpAuthPwd;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public Boolean getStartTsl() {
        return startTsl;
    }

    public void setStartTsl(Boolean startTsl) {
        this.startTsl = startTsl;
    }
}
