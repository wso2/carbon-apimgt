package org.wso2.carbon.apimgt.solace.utils;

import com.hazelcast.aws.utility.StringUtil;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiChannelItem;
import io.apicurio.datamodels.asyncapi.v2.models.Aai20Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SolaceNotifierUtils {
    protected static ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceNotifierUtils.class);

    /**
     * Get and patch client id for Solace application
     *
     * @return SolaceAdminApis  object to invoke Solace
     * @throws APIManagementException If the Solace env configuration if not provided properly
     */
    public static SolaceAdminApis getSolaceAdminApis()
            throws APIManagementException {
        Map<String, Environment> thirdPartyEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        Environment solaceEnvironment = null;

        for (Map.Entry<String, Environment> entry : thirdPartyEnvironments.entrySet()) {
            if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }

        if (solaceEnvironment != null) {
            return new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.
                    getUserName(), solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().
                    get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME));
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

    /**
     * Get available transport protocols for the Solace API
     *
     * @param definition Solace API Definition
     * @return List<String> List of available transport protocols
     * @throws APIManagementException If the Solace env configuration if not provided properly
     */
    public static List<String> getTransportProtocolsForSolaceAPI(String definition) throws APIManagementException {
        Aai20Document aai20Document = (Aai20Document) Library.readDocumentFromJSONString(definition);
        SolaceAdminApis solaceAdminApis = getSolaceAdminApis();
        HashSet<String> solaceTransportProtocols = new HashSet<>();
        for (AaiChannelItem channel : aai20Document.getChannels()) {
            solaceTransportProtocols.addAll(solaceAdminApis.getProtocols(channel));
        }
        ArrayList<String> solaceTransportProtocolsList = new ArrayList<>(solaceTransportProtocols);
        return solaceTransportProtocolsList;
    }

    /**
     * Check whether the Solace is Added as a third party environment
     *
     * @return true if Solace is Added as a third party environment
     */
    private boolean isSolaceEnvironmentAdded() {
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        if (gatewayEnvironments.isEmpty()){
            return false;
        }
        Environment solaceEnvironment = null;

        for (Map.Entry<String,Environment> entry: gatewayEnvironments.entrySet()) {
            if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }
        return solaceEnvironment != null;
    }

    /**
     * Check whether the Solace is Added as a third party environment with required additional properties
     *
     * @return true if Solace is Added as an environment with required additional properties
     */
    private boolean isSolaceGatewayDetailsAdded() {
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        if (gatewayEnvironments.isEmpty()) {
            return false;
        }
        Environment solaceEnvironment = null;

        for (Map.Entry<String, Environment> entry : gatewayEnvironments.entrySet()) {
            if (APIConstants.SOLACE_ENVIRONMENT.equals(entry.getValue().getProvider())) {
                solaceEnvironment = entry.getValue();
            }
        }
        if (solaceEnvironment != null) {
            Map<String, String> additionalProperties = solaceEnvironment.getAdditionalProperties();
            if (additionalProperties.isEmpty()) {
                return false;
            } else {
                if (StringUtil.isEmpty(additionalProperties.get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION)) ||
                        StringUtil.isEmpty(additionalProperties.get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME)) ) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Rename the Solace application
     *
     * @param organization Name of the Organization
     * @param application  Solace application
     * @throws APIManagementException is error occurs when renaming the application
     */
    public static void renameSolaceApplication(String organization, Application application) throws APIManagementException {
        SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
        log.info("Renaming solace application display name....");
        HttpResponse response = solaceAdminApis.renameApplication(organization, application);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.info("Renamed solace application display name into '" + application.getName() + "'");
        } else {
            log.error("Error while renaming solace Application display name....");
            throw new APIManagementException(response.getStatusLine().getStatusCode() + "-" + response.getStatusLine().
                    getReasonPhrase());
        }
    }

    /**
     * Get and patch client id for Solace application
     *
     * @param organization Name of the Organization
     * @param application  Solace application
     * @param consumerKey  Consumer key to be used when patching
     * @throws APIManagementException If the Solace env configuration if not provided properly
     */
    public static void patchSolaceApplicationClientId(String organization, Application application, String consumerKey)
            throws APIManagementException {
        SolaceAdminApis solaceAdminApis = SolaceNotifierUtils.getSolaceAdminApis();
        log.info("Identified as Solace Application. Patching ClientID in solace application.....");
        HttpResponse response = solaceAdminApis.patchClientIdForApplication(organization, application, consumerKey);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            log.info("CliendID patched successfully for Solace application");
        } else {
            log.error("Error while patching clientID for Solace application");
        }
    }

    /**
     * Check whether the given API is already deployed in the Solace using revision
     *
     * @param api Name of the API
     * @return returns true if the given API is already deployed
     * @throws APIManagementException If an error occurs when checking API product availability
     */
    public static boolean checkWhetherAPIDeployedToSolaceUsingRevision(API api) throws APIManagementException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            if (deployment.isDisplayOnDevportal()) {
                String environmentName = deployment.getDeployment();
                if (gatewayEnvironments.containsKey(environmentName)) {
                    Environment deployedEnvironment = gatewayEnvironments.get(environmentName);
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get third party Solace broker organization Name for API deployment
     *
     * @param api Name of the API
     * @return String of the name of organization in Solace broker
     * @throws APIManagementException is error occurs when getting the name of the organization name
     */
    public static String getThirdPartySolaceBrokerOrganizationNameOfAPIDeployment(API api) throws APIManagementException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentByApiUUID(api.getUuid());
        for (APIRevisionDeployment deployment : deployments) {
            if (deployment.isDisplayOnDevportal()) {
                String environmentName = deployment.getDeployment();
                if (gatewayEnvironments.containsKey(environmentName)) {
                    Environment deployedEnvironment = gatewayEnvironments.get(environmentName);
                    if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(deployedEnvironment.getProvider())) {
                        return deployedEnvironment.getAdditionalProperties().
                                get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION);
                    }
                }
            }
        }
        return null;
    }

}
