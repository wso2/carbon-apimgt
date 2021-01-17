package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.gateway.CredentialDto;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryService;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template.APITemplateBuilderImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class TemplateBuilderUtil {

    private static final String PRODUCT_VERSION = "1.0.0";

    private static final Log log = LogFactory.getLog(TemplateBuilderUtil.class);

    public static APITemplateBuilderImpl getAPITemplateBuilder(API api, String tenantDomain,
                                                               List<ClientCertificateDTO> clientCertificateDTOS)
            throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APITemplateBuilderImpl vtb = new APITemplateBuilderImpl(api);
        Map<String, String> latencyStatsProperties = new HashMap<String, String>();
        latencyStatsProperties.put(APIConstants.API_UUID, api.getUUID());
        vtb.addHandler(
                "org.wso2.carbon.apimgt.gateway.handlers.common.APIMgtLatencyStatsHandler",
                latencyStatsProperties);
        Map<String, String> corsProperties = new HashMap<String, String>();
        corsProperties.put(APIConstants.CORSHeaders.IMPLEMENTATION_TYPE_HANDLER_VALUE, api.getImplementation());

        //Get authorization header from the API object or from the tenant registry
        String authorizationHeader;
        if (!StringUtils.isBlank(api.getAuthorizationHeader())) {
            authorizationHeader = api.getAuthorizationHeader();
        } else {
            //Retrieves the auth configuration from tenant registry or api-manager.xml if not available
            // in tenant registry
            authorizationHeader = APIUtil.getOAuthConfiguration(tenantId, APIConstants.AUTHORIZATION_HEADER);
        }
        if (!StringUtils.isBlank(authorizationHeader)) {
            corsProperties.put(APIConstants.AUTHORIZATION_HEADER, authorizationHeader);
        }

        if (api.getCorsConfiguration() != null && api.getCorsConfiguration().isCorsConfigurationEnabled()) {
            CORSConfiguration corsConfiguration = api.getCorsConfiguration();
            if (corsConfiguration.getAccessControlAllowHeaders() != null) {
                StringBuilder allowHeaders = new StringBuilder();
                for (String header : corsConfiguration.getAccessControlAllowHeaders()) {
                    allowHeaders.append(header).append(',');
                }
                if (allowHeaders.length() != 0) {
                    allowHeaders.deleteCharAt(allowHeaders.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_HEADERS_HANDLER_VALUE, allowHeaders.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowOrigins() != null) {
                StringBuilder allowOrigins = new StringBuilder();
                for (String origin : corsConfiguration.getAccessControlAllowOrigins()) {
                    allowOrigins.append(origin).append(',');
                }
                if (allowOrigins.length() != 0) {
                    allowOrigins.deleteCharAt(allowOrigins.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_ORIGIN_HANDLER_VALUE, allowOrigins.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowMethods() != null) {
                StringBuilder allowedMethods = new StringBuilder();
                for (String methods : corsConfiguration.getAccessControlAllowMethods()) {
                    allowedMethods.append(methods).append(',');
                }
                if (allowedMethods.length() != 0) {
                    allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_METHODS_HANDLER_VALUE, allowedMethods.toString());
                }
            }
            if (corsConfiguration.isAccessControlAllowCredentials()) {
                corsProperties.put(APIConstants.CORSHeaders.ALLOW_CREDENTIALS_HANDLER_VALUE,
                        String.valueOf(corsConfiguration.isAccessControlAllowCredentials()));
            }
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler"
                    , corsProperties);
        } else if (APIUtil.isCORSEnabled()) {
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler"
                    , corsProperties);
        }
        if (APIConstants.PROTOTYPED.equals(api.getStatus())) {
            String extensionHandlerPosition = getExtensionHandlerPosition(tenantDomain);
            if ("top".equalsIgnoreCase(extensionHandlerPosition)) {
                vtb.addHandlerPriority(
                        "org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                        Collections.<String, String>emptyMap(), 0);
            } else {
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                        Collections.<String, String>emptyMap());
            }
        }
        if (!APIConstants.PROTOTYPED.equals(api.getStatus())) {

            Map<String, String> clientCertificateObject = null;
            CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
            if (clientCertificateDTOS != null) {
                clientCertificateObject = new HashMap<>();
                for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                    clientCertificateObject.put(certificateMgtUtils
                                    .getUniqueIdentifierOfCertificate(clientCertificateDTO.getCertificate()),
                            clientCertificateDTO.getTierName());
                }
            }

            Map<String, String> authProperties = new HashMap<>();
            if (!StringUtils.isBlank(authorizationHeader)) {
                authProperties.put(APIConstants.AUTHORIZATION_HEADER, authorizationHeader);
            }
            String apiSecurity = api.getApiSecurity();
            String apiLevelPolicy = api.getApiLevelPolicy();
            authProperties.put(APIConstants.API_SECURITY, apiSecurity);
            authProperties.put(APIConstants.API_LEVEL_POLICY, apiLevelPolicy);
            if (clientCertificateObject != null) {
                authProperties.put(APIConstants.CERTIFICATE_INFORMATION, clientCertificateObject.toString());
            }
            //Get RemoveHeaderFromOutMessage from tenant registry or api-manager.xml
            String removeHeaderFromOutMessage = APIUtil
                    .getOAuthConfiguration(tenantId, APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE);
            if (!StringUtils.isBlank(removeHeaderFromOutMessage)) {
                authProperties.put(APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE, removeHeaderFromOutMessage);
            } else {
                authProperties.put(APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE,
                        APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE_DEFAULT);
            }
            authProperties.put(APIConstants.API_UUID, api.getUUID());
            authProperties.put("keyManagers", String.join(",", api.getKeyManagers()));
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                Map<String, String> apiUUIDProperty = new HashMap<String, String>();
                apiUUIDProperty.put(APIConstants.API_UUID, api.getUUID());
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLAPIHandler",
                        apiUUIDProperty);
            }
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler",
                    authProperties);

            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.graphQL.GraphQLQueryAnalysisHandler",
                        Collections.<String, String>emptyMap());
            }

            Map<String, String> properties = new HashMap<String, String>();

            if (api.getProductionMaxTps() != null) {
                properties.put("productionMaxCount", api.getProductionMaxTps());
            }

            if (api.getSandboxMaxTps() != null) {
                properties.put("sandboxMaxCount", api.getSandboxMaxTps());
            }

            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.ThrottleHandler"
                    , properties);

            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtUsageHandler"
                    , Collections.<String, String>emptyMap());

            properties = new HashMap<String, String>();
            properties.put("configKey", APIConstants.GA_CONF_KEY);
            vtb.addHandler(
                    "org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtGoogleAnalyticsTrackingHandler"
                    , properties);

            String extensionHandlerPosition = getExtensionHandlerPosition(tenantDomain);
            if (extensionHandlerPosition != null && "top".equalsIgnoreCase(extensionHandlerPosition)) {
                vtb.addHandlerPriority(
                        "org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                        Collections.<String, String>emptyMap(), 0);
            } else {
                vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                        Collections.<String, String>emptyMap());
            }

        }

        return vtb;
    }

    public static APITemplateBuilderImpl getAPITemplateBuilder(APIProduct apiProduct, String tenantDomain,
                                                               List<ClientCertificateDTO> clientCertificateDTOS,
                                                               Map<String,APIDTO> associatedAPIMap)
            throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APITemplateBuilderImpl vtb = new APITemplateBuilderImpl(apiProduct, associatedAPIMap);
        Map<String, String> latencyStatsProperties = new HashMap<String, String>();
        latencyStatsProperties.put(APIConstants.API_UUID, apiProduct.getUuid());
        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.common.APIMgtLatencyStatsHandler",
                latencyStatsProperties);

        Map<String, String> corsProperties = new HashMap<>();
        corsProperties.put(APIConstants.CORSHeaders.IMPLEMENTATION_TYPE_HANDLER_VALUE,
                APIConstants.IMPLEMENTATION_TYPE_ENDPOINT);

        //Get authorization header from the API object or from the tenant registry
        String authorizationHeader;
        if (!StringUtils.isBlank(apiProduct.getAuthorizationHeader())) {
            authorizationHeader = apiProduct.getAuthorizationHeader();
        } else {
            //Retrieves the auth configuration from tenant registry or api-manager.xml if not available
            // in tenant registry
            authorizationHeader = APIUtil.getOAuthConfiguration(tenantId, APIConstants.AUTHORIZATION_HEADER);
        }
        if (!StringUtils.isBlank(authorizationHeader)) {
            corsProperties.put(APIConstants.AUTHORIZATION_HEADER, authorizationHeader);
        }

        if (apiProduct.getCorsConfiguration() != null &&
                apiProduct.getCorsConfiguration().isCorsConfigurationEnabled()) {
            CORSConfiguration corsConfiguration = apiProduct.getCorsConfiguration();
            if (corsConfiguration.getAccessControlAllowHeaders() != null) {
                StringBuilder allowHeaders = new StringBuilder();
                for (String header : corsConfiguration.getAccessControlAllowHeaders()) {
                    allowHeaders.append(header).append(',');
                }
                if (allowHeaders.length() != 0) {
                    allowHeaders.deleteCharAt(allowHeaders.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_HEADERS_HANDLER_VALUE, allowHeaders.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowOrigins() != null) {
                StringBuilder allowOrigins = new StringBuilder();
                for (String origin : corsConfiguration.getAccessControlAllowOrigins()) {
                    allowOrigins.append(origin).append(',');
                }
                if (allowOrigins.length() != 0) {
                    allowOrigins.deleteCharAt(allowOrigins.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_ORIGIN_HANDLER_VALUE, allowOrigins.toString());
                }
            }
            if (corsConfiguration.getAccessControlAllowMethods() != null) {
                StringBuilder allowedMethods = new StringBuilder();
                for (String methods : corsConfiguration.getAccessControlAllowMethods()) {
                    allowedMethods.append(methods).append(',');
                }
                if (allowedMethods.length() != 0) {
                    allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                    corsProperties.put(APIConstants.CORSHeaders.ALLOW_METHODS_HANDLER_VALUE, allowedMethods.toString());
                }
            }
            if (corsConfiguration.isAccessControlAllowCredentials()) {
                corsProperties.put(APIConstants.CORSHeaders.ALLOW_CREDENTIALS_HANDLER_VALUE,
                        String.valueOf(corsConfiguration.isAccessControlAllowCredentials()));
            }
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler"
                    , corsProperties);
        } else if (APIUtil.isCORSEnabled()) {
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.CORSRequestHandler"
                    , corsProperties);
        }

        Map<String, String> clientCertificateObject = null;
        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        if (clientCertificateDTOS != null) {
            clientCertificateObject = new HashMap<>();
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOS) {
                clientCertificateObject.put(certificateMgtUtils
                                .getUniqueIdentifierOfCertificate(clientCertificateDTO.getCertificate()),
                        clientCertificateDTO.getTierName());
            }
        }

        Map<String, String> authProperties = new HashMap<String, String>();
        if (!StringUtils.isBlank(authorizationHeader)) {
            authProperties.put(APIConstants.AUTHORIZATION_HEADER, authorizationHeader);
        }
        String apiSecurity = apiProduct.getApiSecurity();
        String apiLevelPolicy = apiProduct.getProductLevelPolicy();
        authProperties.put(APIConstants.API_SECURITY, apiSecurity);
        authProperties.put(APIConstants.API_LEVEL_POLICY, apiLevelPolicy);
        if (clientCertificateObject != null) {
            authProperties.put(APIConstants.CERTIFICATE_INFORMATION, clientCertificateObject.toString());
        }

        //Get RemoveHeaderFromOutMessage from tenant registry or api-manager.xml
        String removeHeaderFromOutMessage = APIUtil
                .getOAuthConfiguration(tenantId, APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE);
        if (!StringUtils.isBlank(removeHeaderFromOutMessage)) {
            authProperties.put(APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE, removeHeaderFromOutMessage);
        } else {
            authProperties.put(APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE,
                    APIConstants.REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE_DEFAULT);
        }

        authProperties.put("apiType", APIConstants.ApiTypes.PRODUCT_API.name());
        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler",
                authProperties);
        Map<String, String> properties = new HashMap<String, String>();

        if (apiProduct.getProductionMaxTps() != null) {
            properties.put("productionMaxCount", apiProduct.getProductionMaxTps());
        }

        if (apiProduct.getSandboxMaxTps() != null) {
            properties.put("sandboxMaxCount", apiProduct.getSandboxMaxTps());
        }

        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.throttling.ThrottleHandler"
                , properties);

        vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtUsageHandler"
                , Collections.<String, String>emptyMap());

        properties = new HashMap<String, String>();
        properties.put("configKey", APIConstants.GA_CONF_KEY);
        vtb.addHandler(
                "org.wso2.carbon.apimgt.gateway.handlers.analytics.APIMgtGoogleAnalyticsTrackingHandler"
                , properties);

        String extensionHandlerPosition = getExtensionHandlerPosition(tenantDomain);
        if ("top".equalsIgnoreCase(extensionHandlerPosition)) {
            vtb.addHandlerPriority(
                    "org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                    Collections.<String, String>emptyMap(), 0);
        } else {
            vtb.addHandler("org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler",
                    Collections.<String, String>emptyMap());
        }
        return vtb;
    }

    /**
     * Retrieves Extension Handler Position from the tenant-config.json
     *
     * @return ExtensionHandlerPosition
     * @throws APIManagementException
     */
    private static String getExtensionHandlerPosition(String tenantDomain) throws APIManagementException {

        String extensionHandlerPosition = null;
        try {
            String content = getTenantConfigContent(tenantDomain);
            if (content != null) {
                JSONParser jsonParser = new JSONParser();
                JSONObject tenantConf = (JSONObject) jsonParser.parse(content);
                extensionHandlerPosition = (String) tenantConf.get(APIConstants.EXTENSION_HANDLER_POSITION);
            }
        } catch (RegistryException | UserStoreException e) {
            throw new APIManagementException("Couldn't read tenant configuration from tenant registry", e);
        } catch (ParseException e) {
            throw new APIManagementException(
                    "Couldn't parse tenant configuration for reading extension handler position", e);
        }
        return extensionHandlerPosition;
    }

    protected static String getTenantConfigContent(String tenantDomain) throws RegistryException, UserStoreException {

        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();

        return apimRegistryService
                .getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);
    }

    public static GatewayAPIDTO retrieveGatewayAPIDto(API api, Environment environment, String tenantDomain,
                                                      APIDTO apidto, String extractedFolderPath)
            throws APIManagementException, XMLStreamException, APITemplateException, CertificateManagementException {

        List<ClientCertificateDTO> clientCertificatesDTOList =
                ImportUtils.retrieveClientCertificates(extractedFolderPath);
        APITemplateBuilder apiTemplateBuilder = TemplateBuilderUtil.getAPITemplateBuilder(api, tenantDomain,
                clientCertificatesDTOList);
        if (!APIConstants.APITransportType.WS.toString().equals(api.getType())) {
            return createAPIGatewayDTOtoPublishAPI(environment, api, apiTemplateBuilder, tenantDomain,
                    extractedFolderPath, apidto);
        }
        return null;
    }


    public static GatewayAPIDTO retrieveGatewayAPIDto(API api, Environment environment, String tenantDomain,
                                                      APIDTO apidto, String extractedFolderPath,
                                                      APIDefinitionValidationResponse apiDefinitionValidationResponse)
            throws APIManagementException, XMLStreamException, APITemplateException, CertificateManagementException {
        if (apiDefinitionValidationResponse.isValid()) {
            APIDefinition parser = apiDefinitionValidationResponse.getParser();
            String definition = apiDefinitionValidationResponse.getJsonContent();
            if (parser != null) {
                Set<URITemplate> uriTemplates = parser.getURITemplates(definition);
                for (URITemplate uriTemplate : uriTemplates) {
                    for (URITemplate template : api.getUriTemplates()) {
                        if (template.getHTTPVerb().equalsIgnoreCase(uriTemplate.getHTTPVerb()) &&
                                template.getUriTemplate().equals(uriTemplate.getUriTemplate())) {
                            template.setMediationScript(uriTemplate.getMediationScript());
                            template.setMediationScripts(uriTemplate.getHTTPVerb(), uriTemplate.getMediationScript());
                            template.setAmznResourceName(uriTemplate.getAmznResourceName());
                            template.setAmznResourceTimeout(uriTemplate.getAmznResourceTimeout());
                            break;
                        }
                    }
                }
            }
        }
        return retrieveGatewayAPIDto(api, environment, tenantDomain, apidto, extractedFolderPath);
    }

    public static GatewayAPIDTO retrieveGatewayAPIDto(APIProduct apiProduct, Environment environment,
                                                      String tenantDomain, String extractedFolderPath,
                                                      APIDefinitionValidationResponse apiDefinitionValidationResponse)
            throws APIManagementException, XMLStreamException, APITemplateException, CertificateManagementException {

        List<ClientCertificateDTO> clientCertificatesDTOList =
                ImportUtils.retrieveClientCertificates(extractedFolderPath);
        Map<String, APIDTO> apidtoMap = retrieveAssociatedApis(extractedFolderPath);
        Map<String, APIDTO> associatedAPIsMap = convertAPIIdToDto(apidtoMap.values());
        for (APIProductResource productResource : apiProduct.getProductResources()) {
            String apiId = productResource.getApiId();
            APIDTO apidto = associatedAPIsMap.get(apiId);
            if (apidto != null){
                API api  = APIMappingUtil.fromDTOtoAPI(apidto,apidto.getProvider());
                productResource.setApiIdentifier(api.getId());
                productResource.setEndpointConfig(api.getEndpointConfig());
                if (StringUtils.isNotEmpty(api.getInSequence())){
                    String sequenceName = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
                    productResource.setInSequenceName(sequenceName);
                 }
                if (StringUtils.isNotEmpty(api.getOutSequence())){
                    String sequenceName = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                    productResource.setOutSequenceName(sequenceName);
                }
                if (StringUtils.isNotEmpty(api.getFaultSequence())){
                    String sequenceName = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                    productResource.setFaultSequenceName(sequenceName);
                }
            }
        }
        APITemplateBuilder
                apiTemplateBuilder =
                TemplateBuilderUtil.getAPITemplateBuilder(apiProduct, tenantDomain, clientCertificatesDTOList,
                        convertAPIIdToDto(associatedAPIsMap.values()));
        return createAPIGatewayDTOtoPublishAPI(environment, apiProduct, apiTemplateBuilder, tenantDomain,
                apidtoMap);
    }

    private static GatewayAPIDTO createAPIGatewayDTOtoPublishAPI(Environment environment, APIProduct apiProduct,
                                                                 APITemplateBuilder builder,
                                                                 String tenantDomain,
                                                                 Map<String, APIDTO> associatedAPIsMap)
            throws CertificateManagementException, APITemplateException, XMLStreamException, APIManagementException {

        APIProductIdentifier id = apiProduct.getId();
        GatewayAPIDTO productAPIDto = new GatewayAPIDTO();
        productAPIDto.setProvider(id.getProviderName());
        productAPIDto.setApiId(apiProduct.getUuid());
        productAPIDto.setName(id.getName());
        productAPIDto.setVersion(id.getVersion());
        productAPIDto.setTenantDomain(tenantDomain);
        productAPIDto.setOverride(false);
        String definition = apiProduct.getDefinition();
        productAPIDto.setLocalEntriesToBeRemove(GatewayUtils.addStringToList(apiProduct.getUuid(),
                productAPIDto.getLocalEntriesToBeRemove()));
        GatewayContentDTO productLocalEntry = new GatewayContentDTO();
        productLocalEntry.setName(apiProduct.getUuid());
        productLocalEntry.setContent("<localEntry key=\"" + apiProduct.getUuid() + "\">" +
                definition.replaceAll("&(?!amp;)", "&amp;").
                        replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                + "</localEntry>");
        productAPIDto.setLocalEntriesToBeAdd(addGatewayContentToList(productLocalEntry,
                productAPIDto.getLocalEntriesToBeAdd()));
        APIIdentifier apiId = new APIIdentifier(id.getProviderName(), id.getName(), PRODUCT_VERSION);
        setClientCertificatesToBeAdded(apiId, tenantDomain, productAPIDto);
        productAPIDto.setApiDefinition(builder.getConfigStringForTemplate(environment));
        for (Map.Entry<String, APIDTO> apidtoEntry : associatedAPIsMap.entrySet()) {
            String apiExtractedPath = apidtoEntry.getKey();
            APIDTO apidto = apidtoEntry.getValue();
            API api = APIMappingUtil.fromDTOtoAPI(apidto, apidto.getProvider());
            GatewayUtils.setCustomSequencesToBeRemoved(api, productAPIDto);
            APITemplateBuilder apiTemplateBuilder = new APITemplateBuilderImpl(api);
            addEndpoints(api, apiTemplateBuilder, productAPIDto);
            setCustomSequencesToBeAdded(api, productAPIDto, apiExtractedPath, apidto);
            setAPIFaultSequencesToBeAdded(api, productAPIDto, apiExtractedPath, apidto);
            setClientCertificatesToBeAdded(api, tenantDomain, productAPIDto);
        }

        return productAPIDto;
    }

    private static GatewayAPIDTO createAPIGatewayDTOtoPublishAPI(Environment environment, API api,
                                                                 APITemplateBuilder builder,
                                                                 String tenantDomain, String extractedPath,
                                                                 APIDTO apidto)
            throws APIManagementException, CertificateManagementException, APITemplateException, XMLStreamException {

        GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
        gatewayAPIDTO.setName(api.getId().getName());
        gatewayAPIDTO.setVersion(api.getId().getVersion());
        gatewayAPIDTO.setProvider(api.getId().getProviderName());
        gatewayAPIDTO.setApiId(api.getUUID());
        gatewayAPIDTO.setTenantDomain(tenantDomain);
        gatewayAPIDTO.setOverride(true);

        String definition;

        if (api.getType() != null && APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
            //Build schema with additional info
            GraphqlComplexityInfo graphqlComplexityInfo = APIUtil.getComplexityDetails(api);
            GraphQLSchemaDefinition schemaDefinition = new GraphQLSchemaDefinition();
            definition = schemaDefinition.buildSchemaWithAdditionalInfo(api, graphqlComplexityInfo);
            gatewayAPIDTO.setLocalEntriesToBeRemove(GatewayUtils.addStringToList(api.getUUID() + "_graphQL",
                    gatewayAPIDTO.getLocalEntriesToBeRemove()));
            GatewayContentDTO graphqlLocalEntry = new GatewayContentDTO();
            graphqlLocalEntry.setName(api.getUUID() + "_graphQL");
            graphqlLocalEntry.setContent("<localEntry key=\"" + api.getUUID() + "_graphQL" + "\">" +
                    definition + "</localEntry>");
            gatewayAPIDTO.setLocalEntriesToBeAdd(addGatewayContentToList(graphqlLocalEntry,
                    gatewayAPIDTO.getLocalEntriesToBeAdd()));
            Set<URITemplate> uriTemplates = new HashSet<>();
            URITemplate template = new URITemplate();
            template.setAuthType("Any");
            template.setHTTPVerb("POST");
            template.setHttpVerbs("POST");
            template.setUriTemplate("/*");
            uriTemplates.add(template);
            api.setUriTemplates(uriTemplates);
        } else if (api.getType() != null && (APIConstants.APITransportType.HTTP.toString().equals(api.getType())
                || APIConstants.API_TYPE_SOAP.equals(api.getType())
                || APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()))) {
            definition = api.getSwaggerDefinition();
            gatewayAPIDTO.setLocalEntriesToBeRemove(GatewayUtils.addStringToList(api.getUUID(),
                    gatewayAPIDTO.getLocalEntriesToBeRemove()));

            GatewayContentDTO apiLocalEntry = new GatewayContentDTO();
            apiLocalEntry.setName(api.getUUID());
            apiLocalEntry.setContent("<localEntry key=\"" + api.getUUID() + "\">" +
                    definition.replaceAll("&(?!amp;)", "&amp;").
                            replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                    + "</localEntry>");
            gatewayAPIDTO.setLocalEntriesToBeAdd(addGatewayContentToList(apiLocalEntry,
                    gatewayAPIDTO.getLocalEntriesToBeAdd()));
        }

        // If the API exists in the Gateway and If the Gateway type is 'production' and a production url has not been
        // specified Or if the Gateway type is 'sandbox' and a sandbox url has not been specified

        if ((APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType())
                && !APIUtil.isProductionEndpointsExists(api.getEndpointConfig()))
                || (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType())
                && !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()))) {
            if (log.isDebugEnabled()) {
                log.debug("Not adding API to environment " + environment.getName() + " since its endpoint URL "
                        + "cannot be found");
            }
            return null;
        }
        GatewayUtils.setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
        setAPIFaultSequencesToBeAdded(api, gatewayAPIDTO, extractedPath, apidto);
        setCustomSequencesToBeAdded(api, gatewayAPIDTO, extractedPath, apidto);
        setClientCertificatesToBeAdded(api, tenantDomain, gatewayAPIDTO);

        //Add the API
        if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
            String prototypeScriptAPI = builder.getConfigStringForPrototypeScriptAPI(environment);
            gatewayAPIDTO.setApiDefinition(prototypeScriptAPI);
        } else if (APIConstants.IMPLEMENTATION_TYPE_ENDPOINT.equalsIgnoreCase(api.getImplementation())) {
            String apiConfig = builder.getConfigStringForTemplate(environment);
            gatewayAPIDTO.setApiDefinition(apiConfig);
            org.json.JSONObject endpointConfig = new org.json.JSONObject(api.getEndpointConfig());
            if (!endpointConfig.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE)
                    .equals(APIConstants.ENDPOINT_TYPE_AWSLAMBDA)) {
                addEndpoints(api, builder, gatewayAPIDTO);
            }
        }

        if (api.isDefaultVersion()) {
            String defaultAPIConfig = builder.getConfigStringForDefaultAPITemplate(api.getId().getVersion());
            gatewayAPIDTO.setDefaultAPIDefinition(defaultAPIConfig);
        }
        setSecureVaultPropertyToBeAdded(api, gatewayAPIDTO);
        return gatewayAPIDTO;
    }

    private static void setCustomSequencesToBeAdded(API api, GatewayAPIDTO gatewayAPIDTO, String extractedPath,
                                                    APIDTO apidto) throws APIManagementException {

        GatewayContentDTO gatewayInContentDTO =
                retrieveSequence(extractedPath, apidto.getMediationPolicies(), APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN
                        , api);
        if (gatewayInContentDTO != null) {
            gatewayAPIDTO
                    .setSequenceToBeAdd(
                            addGatewayContentToList(gatewayInContentDTO, gatewayAPIDTO.getSequenceToBeAdd()));
        }
        GatewayContentDTO gatewayOutContentDTO =
                retrieveSequence(extractedPath, apidto.getMediationPolicies(), APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT
                        , api);
        if (gatewayOutContentDTO != null) {
            gatewayAPIDTO
                    .setSequenceToBeAdd(
                            addGatewayContentToList(gatewayOutContentDTO, gatewayAPIDTO.getSequenceToBeAdd()));
        }
    }

    private static void setAPIFaultSequencesToBeAdded(API api, GatewayAPIDTO gatewayAPIDTO, String extractedPath,
                                                      APIDTO apidto)
            throws APIManagementException {

        String faultSeqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
        gatewayAPIDTO
                .setSequencesToBeRemove(GatewayUtils.addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));
        List<MediationPolicyDTO> mediationPolicies = apidto.getMediationPolicies();
        GatewayContentDTO faultSequenceContent =
                retrieveSequence(extractedPath, mediationPolicies, APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT,
                        api);
        if (faultSequenceContent != null) {
            gatewayAPIDTO.setSequenceToBeAdd(
                    addGatewayContentToList(faultSequenceContent, gatewayAPIDTO.getSequenceToBeAdd()));
        }
        gatewayAPIDTO.setSequencesToBeRemove(GatewayUtils.addStringToList(faultSeqExt, gatewayAPIDTO.getSequencesToBeRemove()));
    }

    /**
     * Store the secured endpoint username password to registry
     *
     * @param api
     * @param tenantDomain
     * @throws APIManagementException
     */
    private static void setSecureVaultProperty(APIGatewayAdminClient securityAdminClient, API api, String tenantDomain)
            throws APIManagementException {

        boolean isSecureVaultEnabled =
                Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
        if (api.isEndpointSecured() && isSecureVaultEnabled) {
            try {
                securityAdminClient.setSecureVaultProperty(api, tenantDomain);
            } catch (Exception e) {
                String msg = "Error in setting secured password.";
                log.error(msg + ' ' + e.getLocalizedMessage(), e);
                throw new APIManagementException(msg);
            }
        }
    }

    /**
     * To deploy client certificate in given API environment.
     *
     * @param api          Relevant API.
     * @param tenantDomain Tenant domain.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private static void setClientCertificatesToBeAdded(API api, String tenantDomain, GatewayAPIDTO gatewayAPIDTO)
            throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, api.getId());
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                GatewayContentDTO clientCertificate = new GatewayContentDTO();
                clientCertificate.setName(clientCertificateDTO.getAlias() + "_" + tenantId);
                clientCertificate.setContent(clientCertificateDTO.getCertificate());
                gatewayAPIDTO.setClientCertificatesToBeAdd(addGatewayContentToList(clientCertificate,
                        gatewayAPIDTO.getClientCertificatesToBeAdd()));
            }
        }
    }

    /**
     * To deploy client certificate in given API environment.
     *
     * @param identifier  Relevant API ID.
     * @param tenantDomain Tenant domain.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private static void setClientCertificatesToBeAdded(APIIdentifier identifier, String tenantDomain,
                                                       GatewayAPIDTO gatewayAPIDTO)
            throws CertificateManagementException {

        if (!CertificateManagerImpl.getInstance().isClientCertificateBasedAuthenticationConfigured()) {
            return;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        List<ClientCertificateDTO> clientCertificateDTOList = CertificateMgtDAO.getInstance()
                .getClientCertificates(tenantId, null, identifier);
        if (clientCertificateDTOList != null) {
            for (ClientCertificateDTO clientCertificateDTO : clientCertificateDTOList) {
                GatewayContentDTO clientCertificate = new GatewayContentDTO();
                clientCertificate.setName(clientCertificateDTO.getAlias() + "_" + tenantId);
                clientCertificate.setContent(clientCertificateDTO.getCertificate());
                gatewayAPIDTO.setClientCertificatesToBeAdd(addGatewayContentToList(clientCertificate,
                        gatewayAPIDTO.getClientCertificatesToBeAdd()));
            }
        }
    }

    private static GatewayContentDTO[] addGatewayContentToList(GatewayContentDTO gatewayContentDTO,
                                                               GatewayContentDTO[] gatewayContents) {

        if (gatewayContents == null) {
            return new GatewayContentDTO[]{gatewayContentDTO};
        } else {
            Set<GatewayContentDTO> gatewayContentDTOList = new HashSet<>();
            Collections.addAll(gatewayContentDTOList, gatewayContents);
            gatewayContentDTOList.add(gatewayContentDTO);
            return gatewayContentDTOList.toArray(new GatewayContentDTO[gatewayContentDTOList.size()]);
        }
    }

    private static void addEndpoints(API api, APITemplateBuilder builder, GatewayAPIDTO gatewayAPIDTO)
            throws APITemplateException, XMLStreamException {

        ArrayList<String> arrayListToAdd = getEndpointType(api);
        for (String type : arrayListToAdd) {
            String endpointConfigContext = builder.getConfigStringForEndpointTemplate(type);
            GatewayContentDTO endpoint = new GatewayContentDTO();
            endpoint.setName(getEndpointName(endpointConfigContext));
            endpoint.setContent(endpointConfigContext);
            gatewayAPIDTO.setEndpointEntriesToBeAdd(addGatewayContentToList(endpoint,
                    gatewayAPIDTO.getEndpointEntriesToBeAdd()));
        }
    }

    /**
     * Returns the defined endpoint types of the in the publisher
     *
     * @param api API that the endpoint/s belong
     * @return ArrayList containing defined endpoint types
     */
    public static ArrayList<String> getEndpointType(API api) {

        ArrayList<String> arrayList = new ArrayList<>();
        if (APIUtil.isProductionEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
        } else if (APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isProductionEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        } else {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        }
        return arrayList;
    }

    private static String getEndpointName(String endpointConfig) throws XMLStreamException {

        OMElement omElement = AXIOMUtil.stringToOM(endpointConfig);
        OMAttribute nameAttribute = omElement.getAttribute(new QName("name"));
        if (nameAttribute != null) {
            return nameAttribute.getAttributeValue();
        } else {
            return null;
        }

    }

    private static void setSecureVaultPropertyToBeAdded(API api, GatewayAPIDTO gatewayAPIDTO) {

        boolean isSecureVaultEnabled =
                Boolean.parseBoolean(ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
        if (api.isEndpointSecured() && isSecureVaultEnabled) {
            String secureVaultAlias =
                    api.getId().getProviderName() + "--" + api.getId().getApiName() + api.getId().getVersion();

            CredentialDto credentialDto = new CredentialDto();
            credentialDto.setAlias(secureVaultAlias);
            credentialDto.setPassword(api.getEndpointUTPassword());
            gatewayAPIDTO.setCredentialsToBeAdd(addCredentialsToList(credentialDto,
                    gatewayAPIDTO.getCredentialsToBeAdd()));
        }

    }

    private static CredentialDto[] addCredentialsToList(CredentialDto credential, CredentialDto[] credentials) {

        if (credentials == null) {
            return new CredentialDto[]{credential};
        } else {
            Set<CredentialDto> credentialList = new HashSet<>();
            Collections.addAll(credentialList, credentials);
            credentialList.add(credential);
            return credentialList.toArray(new CredentialDto[credentialList.size()]);
        }
    }

    private static GatewayContentDTO retrieveSequence(String pathToAchieve, List<MediationPolicyDTO> mediationPolicies,
                                                      String type, API api) throws APIManagementException {

        MediationPolicyDTO mediationPolicyDTO = null;
        for (MediationPolicyDTO mediationPolicy : mediationPolicies) {
            if (type.equalsIgnoreCase(mediationPolicy.getType())) {
                mediationPolicyDTO = mediationPolicy;
                break;
            }
        }
        if (mediationPolicyDTO != null) {
            GatewayContentDTO sequenceContentDto = new GatewayContentDTO();

            String sequenceContent = ImportUtils
                    .retrieveSequenceContent(pathToAchieve, !mediationPolicyDTO.isShared(), type.toLowerCase(),
                            mediationPolicyDTO.getName());
            if (StringUtils.isNotEmpty(sequenceContent)) {
                try {
                    OMElement omElement = APIUtil.buildOMElement(new ByteArrayInputStream(sequenceContent.getBytes()));
                    if (omElement != null) {
                        String seqExt = null;

                        if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT.equalsIgnoreCase(type))
                            seqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                        else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equalsIgnoreCase(type))
                            seqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
                        else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equalsIgnoreCase(type))
                            seqExt = APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_IN_EXT;

                        if (omElement.getAttribute(new QName("name")) != null) {
                            omElement.getAttribute(new QName("name")).setAttributeValue(seqExt);
                        }
                        sequenceContentDto.setName(seqExt);
                        sequenceContentDto.setContent(APIUtil.convertOMtoString(omElement));
                        return sequenceContentDto;
                    }
                } catch (Exception e) {
                    throw new APIManagementException(e);
                }
            }
        }
        return null;
    }

    private static Map<String, APIDTO> retrieveAssociatedApis(String extractedPath) throws APIManagementException {

        Map<String, APIDTO> apidtoMap = new HashMap();
        String apisDirectoryPath = extractedPath + File.separator + APIImportExportConstants.APIS_DIRECTORY;
        File apisDirectory = new File(apisDirectoryPath);
        File[] apisDirectoryListing = apisDirectory.listFiles();
        if (apisDirectoryListing != null) {
            for (File file : apisDirectoryListing) {
                try {
                    APIDTO apidto = ImportUtils.retrievedAPIDto(file.getAbsolutePath());
                    apidtoMap.put(file.getAbsolutePath(), apidto);
                } catch (IOException e) {
                    throw new APIManagementException("Error while reading api", e);
                }
            }
        }
        return apidtoMap;
    }

    private static Map<String, APIDTO> convertAPIIdToDto(Collection<APIDTO> apidtoSet) {

        Map<String, APIDTO> apidtoMap = new HashMap<>();
        for (APIDTO apidto : apidtoSet) {
            apidtoMap.put(apidto.getId(), apidto);
        }
        return apidtoMap;
    }
}
