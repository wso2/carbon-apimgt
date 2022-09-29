/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.UserAuthContext;

/**
 * This Util can be used to get access token sent from UI.
 */
public class UserTokenUtil {

    private static final UserAuthContext userContext = new UserAuthContext();
    private static final ThreadLocal<UserAuthContext> tokenThreadLocal = ThreadLocal.withInitial(() -> userContext);

    private UserTokenUtil() {
    }

    public static String getToken() {
        return tokenThreadLocal.get().getToken();
    }

    public static void setToken(String token) {

        UserAuthContext context = tokenThreadLocal.get();
        context.setToken(token);
        tokenThreadLocal.set(context);
    }

    public static void clear() {
        tokenThreadLocal.remove();
    }

}
