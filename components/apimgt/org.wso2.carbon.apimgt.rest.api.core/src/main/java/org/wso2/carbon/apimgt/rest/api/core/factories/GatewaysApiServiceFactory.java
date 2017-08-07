
package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.impl.APIGatewayPublisherImpl;
import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.GatewaysApiServiceImpl;

public class GatewaysApiServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(GatewaysApiServiceFactory.class);

    private static  GatewaysApiService service = null;

    public static GatewaysApiService getGatewaysApi() {
        try {
            service = new GatewaysApiServiceImpl(DAOFactory.getAPISubscriptionDAO(), DAOFactory.getPolicyDAO(),
                    DAOFactory.getApiDAO(), DAOFactory.getLabelDAO(), DAOFactory.getApplicationDAO(), new
                    APIGatewayPublisherImpl());
        } catch (APIMgtDAOException e) {
            throw new RuntimeException("Error occurred while initializing GatewaysApiService", e);
        }
        return service;
    }
}
