/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hostobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import javax.script.ScriptException;

/**
 * This class wrap up the operations needed to authenticate through mutual auth
 */
public class MutualAuthHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(MutualAuthHostObject.class);

    // issuerId, relyingPartyObject .this is to provide sso functionality to multiple jaggery apps.
    private static  MutualAuthHostObject mutualAuthHostObject;

    @Override
    public String getClassName() {
        return "MutualAuthHostObject";
    }

    /**
     * @param cx        context
     * @param args      - args[0]-issuerId, this issuer need to be registered in Identity server.
     * @param ctorObj
     * @return
     * @throws Exception
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws Exception {

        mutualAuthHostObject = new MutualAuthHostObject();
        return mutualAuthHostObject;
    }

    /**
     * Validate the provided user name against user store
     * @param cx context
     * @param thisObj this object
     * @param args arguments
     * @return boolean
     * @throws Exception
     */
    public static boolean jsFunction_validateUserNameHeader(Context cx, Scriptable thisObj,
                                                            Object[] args, Function funObj) throws Exception {

        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String) ) {
            throw new ScriptException("Invalid argument. User Name is not set properly");
        }

        boolean isValidUser = false;
        String userNameHeader = (String) args[0];

        try {

            String tenantDomain = MultitenantUtils.getTenantDomain(userNameHeader);
            String userName = MultitenantUtils.getTenantAwareUsername(userNameHeader);
            TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);

            UserStoreManager userstore = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();

            if (userstore.isExistingUser(userName)) {
                isValidUser = true;
            }

        } catch (Exception e) {
            log.error("Error validating the user " + e.getMessage(), e);
            throw new ScriptException("Error validating the user " + userNameHeader);

        }

        return isValidUser;

    }




}