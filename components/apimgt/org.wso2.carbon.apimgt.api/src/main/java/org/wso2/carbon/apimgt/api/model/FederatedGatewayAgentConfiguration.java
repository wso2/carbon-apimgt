package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FederatedGatewayAgentConfiguration {
    Map<String, Boolean> featureConfigurations = new HashMap<>();

    /**
     * This method used to get Type
     */
    public String getType();

    /**
     * This method returns the Gateway Agent implementation class name
     *
     * @return gateway agent implementation class name
     */
    public String getImplementation();

    /**
     * This method returns the Configurations related to federated gateway registration
     *
     * @return  List<ConfigurationDto> connectionConfigurations
     */
    public List<ConfigurationDto> getConnectionConfigurations();

    /**
     * This method returns the Configurations related to features supported in federated gateway registration
     * By default all features are enabled. If a feature is not supported, it should be disabled at the specific federated
     * gateway implementation level.
     *
     * @return  Map<String, Boolean> featureConfigurations
     */
    public default Map<String, Boolean> getFeatureConfigurations() {
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.CORS_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.SCHEMA_VALIDATION_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.RESPONSE_CACHING_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.TRANSPORTS_HTTP_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.TRANSPORTS_HTTPS_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.TRANSPORTS_MUTUAL_SSL_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.API_LEVEL_RATE_LIMITING_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.OPERATION_LEVEL_RATE_LIMITING_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.OPERATION_SECURITY_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.OPERATION_SCOPES_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.OPERATION_LEVEL_POLICIES_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.MONETIZATION_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.SUBSCRIPTIONS_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.OAUTH2_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.APIKEY_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.BASIC_AUTH_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.AUDIENCE_VALIDATION_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_REST_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_SERVICE_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_SOAP_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_DYNAMIC_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_MOCK_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_LAMBDA_FEATURE, true);
        featureConfigurations.put(APIConstants.FederatedGatewayConstants.ENDPOINTS_SEQUENCE_BACKEND_FEATURE, true);
        return featureConfigurations;
    }

    /**
     * This method used to get Display Name
     *
     * @return String displayName
     */
    public default String getDisplayName() {
        return getType();
    }
}
