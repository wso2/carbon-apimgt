
package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.*;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;

public class OpenKeyManagerGrantHandler extends ClientCredentialsGrantHandler {

    private static Log log = LogFactory.getLog(OpenKeyManagerGrantHandler.class);
    private static final String OPENKM_GRANT_PARAM = "validity_period";

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx){

        RequestParameter[] parameters =  tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();

        Long validityPeriod = null;

        // find out validity period
        for(RequestParameter parameter : parameters){
            if(OPENKM_GRANT_PARAM.equals(parameter.getKey())){
                if(parameter.getValue() != null && parameter.getValue().length > 0){
                    validityPeriod = Long.valueOf(parameter.getValue()[0]);
                }
            }
        }

        if(validityPeriod != null) {
            //set validity time
            tokReqMsgCtx.setValidityPeriod(validityPeriod);
        }else{
            //set default validity time
            tokReqMsgCtx.setValidityPeriod(360000);
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
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx){
        ScopesIssuer scopesIssuer = new ScopesIssuer();
        return scopesIssuer.setScopes(tokReqMsgCtx);
    }

}
