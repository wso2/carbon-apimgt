
package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.handlers.ScopesIssuer;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.*;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;

/**
 * This grant handler will accept validity period as a parameter.
 */
public class ApplicationTokenGrantHandler extends ExtendedClientCredentialsGrantHandler {

    private static Log log = LogFactory.getLog(ApplicationTokenGrantHandler.class);
    private static final String OPENKM_GRANT_PARAM = "validity_period";
    private static final int DEFAULT_VALIDITY_PERIOD = 3600000;

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx){

        RequestParameter[] parameters =  tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();

        Long validityPeriod = null;

        // find out validity period
        for(RequestParameter parameter : parameters){
            if(OPENKM_GRANT_PARAM.equals(parameter.getKey())){
                if(parameter.getValue() != null && parameter.getValue().length > 0){
                    if(parameter.getValue()[0] == "0"){
                        validityPeriod = null;
                    }else{
                        validityPeriod = Long.valueOf(parameter.getValue()[0]);
                    }

                }
            }
        }

        if(validityPeriod != 0) {
            //set validity time
            tokReqMsgCtx.setValidityPeriod(validityPeriod);
        }

        return true;
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        return super.validateGrant(tokReqMsgCtx);
    }

    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception{
        return super.issueRefreshToken();
    }

    @Override
    public boolean isOfTypeApplicationUser() throws IdentityOAuth2Exception{
        return super.isOfTypeApplicationUser();
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        return super.validateScope(tokReqMsgCtx);
    }

}
