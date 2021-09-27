package org.wso2.carbon.apimgt.impl.notifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpResponseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.deployer.ExternalGatewayDeployer;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExternalGatewayNotifier extends DeployAPIInGatewayNotifier{
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(ExternalGatewayNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        process(event);
        return true;
    }

    /**
     * Process gateway notifier events related to Solace broker deployments
     *
     * @param event related to deployments
     * @throws NotifierException if error occurs when casting event
     */
    private void process (Event event) throws NotifierException {
        DeployAPIInGatewayEvent deployAPIInGatewayEvent;
        try {
            deployAPIInGatewayEvent = (DeployAPIInGatewayEvent) event;
        } catch (ExceptionInInitializerError e) {
            throw new NotifierException("Event types is not provided correctly");
        }

        if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(event.getType())) {
            deployApi(deployAPIInGatewayEvent);
        } else {
            unDeployApi(deployAPIInGatewayEvent);
        }
    }

    /**
     * Deploy APIs to Solace broker
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to deploy APIs to Solace broker
     * @throws NotifierException if error occurs when deploying APIs to Solace broker
     */
    private void deployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deployedToSolace;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (gatewayEnvironments.get(deploymentEnv).getProvider());
                    if ( deployer!= null) {
                        try {
                            deployedToSolace = deployer.deploy(api, gatewayEnvironments.get(deploymentEnv));
                            if (!deployedToSolace) {
                                throw new APIManagementException("Error while deploying API product to Solace broker");
                            }
                        } catch (DeployerException e) {
                            throw new APIManagementException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }

    /**
     * Undeploy APIs from Solace broker
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to undeploy APIs from Solace broker
     * @throws NotifierException if error occurs when undeploying APIs from Solace broker
     */
    private void unDeployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deletedFromSolace;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPI(apiMgtDAO.getAPIIdentifierFromUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (gatewayEnvironments.get(deploymentEnv).getProvider());
                    if (deployer != null) {
                        try {
                            deletedFromSolace = deployer.undeploy(api, gatewayEnvironments.get(deploymentEnv));
                            if (!deletedFromSolace) {
                                throw new NotifierException("Error while deleting API product from Solace broker");
                            }
                        } catch (DeployerException e) {
                            throw new NotifierException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }

    }
}
