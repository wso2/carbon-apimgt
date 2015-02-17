package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;

public abstract class AbstractKeyValidationHandler implements KeyValidationHandler {


    private static final Log log = LogFactory.getLog(AbstractKeyValidationHandler.class);
    private ApiMgtDAO dao = new ApiMgtDAO();

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException{
        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();

        boolean state = false;

        try {
            if(log.isDebugEnabled() && dto != null){
                log.debug("Before validating subscriptions : "+dto);
                log.debug("Validation Info : { context : "+validationContext.getContext()+" , " +
                          "version : "+validationContext.getVersion()+" , consumerKey : "+dto.getConsumerKey()+" }");
            }

            state = dao.validateSubscriptionDetails(validationContext.getContext(),
                                                    validationContext.getVersion(),
                                                    dto.getConsumerKey(), dto);


            if(log.isDebugEnabled() && dto != null){
                log.debug("After validating subscriptions : "+dto);
            }


        } catch (APIManagementException e) {
            //#TODO throw error
            new APIKeyMgtException(e.getMessage(),e);
        }

        return state;
    }
}
