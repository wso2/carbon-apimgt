/*
 *
 *  * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.kerberos.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Represent an user.
 */
public class User {

    private static final Log log = LogFactory.getLog(User.class);
    private LoginContext loginContext;

    /**
     * Login this user using the given login context.
     *
     * @param loginContextName login context name
     * @throws Exception if login fails
     */
    public void login(String loginContextName) throws Exception {
        if (isLoggedin()) {
            throw new IllegalStateException("User '" + getSubject().toString() + "' is already logged in.");
        }

        Configuration.setConfiguration(null);
        LoginContext loginContext = new LoginContext(loginContextName);
        loginContext.login();
        this.loginContext = loginContext;
    }

    /**
     * Returns the subject if this user has been logged in.
     *
     * @return subject of the logged-in user.
     */
    public Subject getSubject() {
        if (!isLoggedin()) {
            throw new IllegalStateException("User has not yet logged in.");
        }
        return loginContext.getSubject();
    }

    /**
     * Logout this user if already logged-in previously.
     */
    public void logout() {
        if (isLoggedin()) {
            try {
                loginContext.logout();
            } catch (LoginException ignore) {
            } finally {
                loginContext = null;
            }
        }
    }

    /**
     * Returns whether this user is logged-in or not.
     *
     * @return {@code true} if the user is logged-in
     */
    public boolean isLoggedin() {
        return loginContext != null;
    }
}
