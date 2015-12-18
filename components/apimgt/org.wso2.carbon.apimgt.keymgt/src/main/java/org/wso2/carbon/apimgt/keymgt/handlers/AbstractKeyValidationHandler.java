package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.token.TokenGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractKeyValidationHandler implements KeyValidationHandler {

    private static final Log log = LogFactory.getLog(AbstractKeyValidationHandler.class);
    private ApiMgtDAO dao = new ApiMgtDAO();

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext == null || validationContext.getValidationInfoDTO() == null) {
            return false;
        }

        if (validationContext.isCacheHit()) {
            return true;
        }

        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();


        if (validationContext.getTokenInfo() != null) {
            if (validationContext.getTokenInfo().isApplicationToken()) {
                dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            } else {
                dto.setUserType("APPLICATION_USER");
            }

            AccessTokenInfo tokenInfo = validationContext.getTokenInfo();

            // This block checks if a Token of Application Type is trying to access a resource protected with
            // Application Token
            if (!hasTokenRequiredAuthLevel(validationContext.getRequiredAuthenticationLevel(), tokenInfo)) {
                dto.setAuthorized(false);
                dto.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                return false;
            }
        }

        boolean state = false;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Before validating subscriptions : " + dto);
                log.debug("Validation Info : { context : " + validationContext.getContext() + " , " +
                          "version : " + validationContext.getVersion() + " , consumerKey : " + dto.getConsumerKey() + " }");
            }

            state = dao.validateSubscriptionDetails(validationContext.getContext(),
                                                    validationContext.getVersion(),
                                                    dto.getConsumerKey(), dto);
            if (state) {

                dto.setAuthorizedDomains(APIUtil.getListOfAuthorizedDomainsByConsumerKey(validationContext
                                                                                                 .getTokenInfo().getConsumerKey()));
                checkClientDomainAuthorized(dto, validationContext.getClientDomain());
            }


            if (log.isDebugEnabled()) {
                log.debug("After validating subscriptions : " + dto);
            }


        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }

        return state;
    }

    protected void checkClientDomainAuthorized(APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String clientDomain)
            throws APIKeyMgtException {
        try {
            APIUtil.checkClientDomainAuthorized(apiKeyValidationInfoDTO, clientDomain);
        } catch (APIManagementException e) {
            log.error("Error while validating client domain", e);
        }

    }

    /**
     * Determines whether the provided token is an ApplicationToken.
     *
     * @param tokenInfo
     */
    protected void setTokenType(AccessTokenInfo tokenInfo) {


    }

    /**
     * Resources protected with Application token type can only be accessed using Application Access Tokens. This method
     * verifies if a particular resource can be accessed using the obtained token.
     *
     * @param authScheme Type of token required by the resource (Application | User Token)
     * @param tokenInfo  Details about the Token
     * @return {@code true} if token is of the type required, {@code false} otherwise.
     */
    protected boolean hasTokenRequiredAuthLevel(String authScheme,
                                                AccessTokenInfo tokenInfo) {

        if (authScheme == null || authScheme.isEmpty() || tokenInfo == null) {
            return false;
        }
        // setTokenType(tokenInfo);

        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authScheme)) {
            return tokenInfo.isApplicationToken();
        } else if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authScheme)) {
            return !tokenInfo.isApplicationToken();
        }

        return true;

    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext validationContext) throws APIKeyMgtException {

      TokenGenerator generator = APIKeyMgtDataHolder.getTokenGenerator();

        try {
            String jwt = generator.generateToken(validationContext.getValidationInfoDTO(),
                                                 validationContext.getContext(), validationContext
                            .getVersion(), validationContext.getAccessToken());
            validationContext.getValidationInfoDTO().setEndUserToken(jwt);
            return true;

        } catch (APIManagementException e) {
            log.error("Error occurred while generating JWT. ", e);
        }

        return false;
    }

}
