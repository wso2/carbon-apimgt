/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.handlers;

import org.wso2.carbon.identity.oauth2.model.*;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

/**
 * This grant handler will accept validity period as a parameter.
 */
public class ApplicationTokenGrantHandler extends ExtendedClientCredentialsGrantHandler {

    private static final String OPENKM_GRANT_PARAM = "validity_period";

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx){

        RequestParameter[] parameters =  tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();

        Long validityPeriod = null;

        if(parameters == null){
            return true;
        }

        // find out validity period
        for(RequestParameter parameter : parameters){
            if(OPENKM_GRANT_PARAM.equals(parameter.getKey()) 
                    && parameter.getValue() != null && parameter.getValue().length > 0){
                if(parameter.getValue()[0] == "0"){
                    validityPeriod = null;
                }else{
                    validityPeriod = Long.valueOf(parameter.getValue()[0]);
                }
            }
        }

        if(validityPeriod != null && validityPeriod != 0) {
            //set validity time
            tokReqMsgCtx.setValidityPeriod(validityPeriod);
        }

        return true;
    }
   

}
