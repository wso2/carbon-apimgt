package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.token.TokenGenerator;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;

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


            if (log.isDebugEnabled() && dto != null) {
                log.debug("After validating subscriptions : " + dto);
            }


        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }

        return state;
    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext validationContext) throws APIKeyMgtException {

        // If JWT is already taken from cache, we can safely exit from the function.
        if (validationContext.isJWTCacheHit()) {
            return true;
        }

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
