package org.wso2.carbon.apimgt.impl.gatewayBridge.webhooks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.gatewayBridge.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.impl.gatewayBridge.deployers.APIDeployer;
import org.wso2.carbon.apimgt.impl.gatewayBridge.deployers.APIDeployerImpl;
import org.wso2.carbon.apimgt.impl.gatewayBridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;


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

        //creating an environment for the external gateway subscriber
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Environment env = new Environment();
        env.setName(webhookSubscriptionDTO.getSubscriberName());
        env.setExternalGWEnv(true);

        //creating a VHost for the new env
        VHost vHost = new VHost();
        vHost.setHost(webhookSubscriptionDTO.getvHost()); // eg:-aws.com
        //set vHost ports
        env.getVhosts().add(vHost);

        apiAdmin.addEnvironment(tenantDomain, env);

        /* The following section is for the testing the
             webhook because of the version incompatibility.
         */
        //why not API
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
