package org.wso2.carbon.apimgt.gatewaybridge.webhooks;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gatewaybridge.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.gatewaybridge.deployers.APIDeployer;
import org.wso2.carbon.apimgt.gatewaybridge.deployers.APIDeployerImpl;
import org.wso2.carbon.apimgt.gatewaybridge.dto.WebhookSubscriptionDTO;



/**
 * Subscribe the external gateway to receive events.
 */
public class ExternalGatewayWebhookSubscriptionServiceImpl implements ExternalGatewayWebhookSubscriptionService {

    private static final Log log = LogFactory.getLog(ExternalGatewayWebhookSubscriptionServiceImpl.class);

    private static  WebhookSubscriptionGetService webhookSubscriptionGetService =
            new WebhookSubscriptionGetServiceImpl();
    private static WebhooksDAO webhooksDAO = new WebhooksDAO();

    /**
     * Invoke the services needed to
     * store gateway subscriptions in the database.
     * @param webhookSubscriptionDTO the DTO object contains the subscription details
     */
    @Override
    public void addExternalGatewaySubscription(WebhookSubscriptionDTO webhookSubscriptionDTO)
            throws APIManagementException {
        Boolean result = webhooksDAO.addSubscription(webhookSubscriptionDTO);
        webhookSubscriptionGetService.getWebhookSubscription("publish");
        if (result) {
            log.debug("Successfully inserted the subscription ");
        } else {
            log.debug("Unexpected error while inserting the subscription ");
        }

        /* The following section is for the testing the
             webhook because of the version incompatibility.
         */
        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName("test1");
        gatewayAPIDTO.setVersion("1.0.0");
        gatewayAPIDTO.setProvider("ABC");
        gatewayAPIDTO.setApiId("1234");
        gatewayAPIDTO.setApiDefinition("Gold");

        try {
            APIDeployer apiDeployer = new APIDeployerImpl();
            apiDeployer.deployArtifacts(gatewayAPIDTO, "publish");
        } catch (Exception e) {
            log.debug("Unexpected Error:" + e);
        }


    }
}

