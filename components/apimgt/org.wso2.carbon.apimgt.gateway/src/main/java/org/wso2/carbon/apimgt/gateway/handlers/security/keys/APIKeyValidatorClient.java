/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.xsd.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.xsd.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceAPIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class APIKeyValidatorClient {

    private static final Log log = LogFactory.getLog(APIKeyValidatorClient.class);

    private APIKeyValidationServiceStub keyValidationServiceStub;
    private String username;
    private String password;
    private String cookie;
    Map<String, String> tracerSpecificCarrier = new HashMap<>();

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS",
                                                      justification = "It is required to set two options on the Options object")
    public APIKeyValidatorClient() throws APISecurityException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        if (serviceURL == null || username == null || password == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Required connection details for the key management server not provided");
        }
        try {
            ConfigurationContext ctx = ServiceReferenceHolder.getInstance().getAxis2ConfigurationContext();
            keyValidationServiceStub = new APIKeyValidationServiceStub(ctx, serviceURL + "APIKeyValidationService");
            ServiceClient client = keyValidationServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setCallTransportCleanup(true);
            options.setManageSession(true);


        } catch (AxisFault axisFault) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while initializing the API key validation stub", axisFault);
        }
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
                                                 String requiredAuthenticationLevel, String clientDomain,
                                                 String matchingResource, String httpVerb) throws APISecurityException {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password, keyValidationServiceStub._getServiceClient());
        if (cookie != null) {
            keyValidationServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        TracingSpan span = null;
        if (Util.tracingEnabled()) {
            TracingSpan keySpan = (TracingSpan) MessageContext.getCurrentMessageContext()
                    .getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
            TracingTracer tracer = Util.getGlobalTracer();
            if (keySpan != null) {
                span = Util.startSpan(APIMgtGatewayConstants.KEY_VALIDATION_FROM_GATEWAY_NODE, keySpan, tracer);
                Util.inject(keySpan, tracer, tracerSpecificCarrier);
            }
        }
        try {
            List headerList = (List) keyValidationServiceStub._getServiceClient().getOptions().getProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS);
            Map headers = (Map) MessageContext.getCurrentMessageContext().getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (Util.tracingEnabled()) {
                for (Map.Entry<String, String> entry : tracerSpecificCarrier.entrySet()) {
                    headerList.add(new Header(entry.getKey(), entry.getValue()));
                }
            }
            if (headers != null) {
                headerList.add(new Header(APIConstants.ACTIVITY_ID, (String) headers.get(APIConstants.ACTIVITY_ID)));
            }
            keyValidationServiceStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, headerList);
            /**/

            if (log.isDebugEnabled()) {
                log.debug("KeyValidation request from gateway to keymanager via web service call for:" + context
                        + " with ID: " + MessageContext.getCurrentMessageContext().getMessageID() + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO dto =
                    keyValidationServiceStub.validateKey(context, apiVersion, apiKey, requiredAuthenticationLevel, clientDomain,
                                                         matchingResource, httpVerb);
            if (log.isDebugEnabled()) {
                log.debug("KeyValidation response received to gateway from keymanager via web service call for:"
                        + context + " with ID: " + MessageContext.getCurrentMessageContext().getMessageID() + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }

            ServiceContext serviceContext = keyValidationServiceStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return toDTO(dto);
        } catch (RemoteException e) {
            if (Util.tracingEnabled()) {
                Util.setTag(span, APIMgtGatewayConstants.ERROR, APIMgtGatewayConstants.API_KEY_VALIDATOR_ERROR);
            }
            log.error("Error while accessing backend services for API key validation", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } catch (Exception e) {
            if (Util.tracingEnabled()) {
                Util.setTag(span, APIMgtGatewayConstants.ERROR, APIMgtGatewayConstants.API_KEY_VALIDATOR_ERROR);
            }
            log.error("Error while accessing backend services for API key validation", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while accessing backend services for API key validation", e);
        } finally {
            if (Util.tracingEnabled() && span != null) {
                Util.finishSpan(span);
            }
        }
    }

    public APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey)
            throws APISecurityException {
        CarbonUtils.setBasicAccessSecurityHeaders(username, password, keyValidationServiceStub._getServiceClient());
        if (cookie != null) {
            keyValidationServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        try {

            if (log.isDebugEnabled()) {
                log.debug("Subscription Validation request from gateway " +
                        "to key manager via web service call for:" + context
                        + " with ID: " + MessageContext.getCurrentMessageContext().getMessageID() + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO dto =
                    keyValidationServiceStub.validateSubscription(context, version, consumerKey);
            if (log.isDebugEnabled()) {
                log.debug("Subscription Validation response received to gateway " +
                        "from key manager via web service call for:"
                        + context + " with ID: " + MessageContext.getCurrentMessageContext().getMessageID() + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            ServiceContext serviceContext = keyValidationServiceStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return toDTO(dto);
        } catch (RemoteException | APIKeyValidationServiceAPIKeyMgtException |
                APIKeyValidationServiceAPIManagementException e) {
            log.error("Error while accessing backend services to validate subscriptions", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API subscription validation", e);
        }
    }

    private APIKeyValidationInfoDTO toDTO(
            org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO generatedDto) {
        APIKeyValidationInfoDTO dto = new APIKeyValidationInfoDTO();
        dto.setSubscriber(generatedDto.getSubscriber());
        dto.setAuthorized(generatedDto.getAuthorized());
        dto.setTier(generatedDto.getTier());
        dto.setType(generatedDto.getType());
        dto.setEndUserToken(generatedDto.getEndUserToken());
        dto.setEndUserName(generatedDto.getEndUserName());
        dto.setApplicationName(generatedDto.getApplicationName());
        dto.setEndUserName(generatedDto.getEndUserName());
        dto.setConsumerKey(generatedDto.getConsumerKey());
        //dto.setAuthorizedDomains(Arrays.asList(generatedDto.getAuthorizedDomains()));
        dto.setValidationStatus(generatedDto.getValidationStatus());
        dto.setApplicationId(generatedDto.getApplicationId());
        dto.setApplicationTier(generatedDto.getApplicationTier());
        dto.setApiPublisher(generatedDto.getApiPublisher());
        dto.setApiName(generatedDto.getApiName());
        dto.setValidityPeriod(generatedDto.getValidityPeriod());
        dto.setIssuedTime(generatedDto.getIssuedTime());
        dto.setApiTier(generatedDto.getApiTier());
        dto.setContentAware(generatedDto.getContentAware());
        dto.setScopes(generatedDto.getScopes() == null ? null : new HashSet<String>(Arrays.asList(generatedDto.getScopes())));
        dto.setThrottlingDataList(Arrays.asList(generatedDto.getThrottlingDataList()));
        dto.setSpikeArrestLimit(generatedDto.getSpikeArrestLimit());
        dto.setSpikeArrestUnit(generatedDto.getSpikeArrestUnit());
        dto.setSubscriberTenantDomain(generatedDto.getSubscriberTenantDomain());
        dto.setStopOnQuotaReach(generatedDto.getStopOnQuotaReach());
        dto.setProductName(generatedDto.getProductName());
        dto.setProductProvider(generatedDto.getProductProvider());
        return dto;
    }

    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password, keyValidationServiceStub._getServiceClient());
        if (cookie != null) {
            keyValidationServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        try {
            TracingSpan span = null;
            if (Util.tracingEnabled()) {
                TracingSpan keySpan = (TracingSpan) MessageContext.getCurrentMessageContext()
                        .getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
                TracingTracer tracer = Util.getGlobalTracer();
                span = Util.startSpan(APIMgtGatewayConstants.GET_ALL_URI_TEMPLATES, keySpan, tracer);
            }
            if (log.isDebugEnabled()) {
                log.debug("Get all URI templates request from gateway to keymanager via web service call for:"
                        + context + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            org.wso2.carbon.apimgt.api.model.xsd.URITemplate[] dto =
                    keyValidationServiceStub.getAllURITemplates(context, apiVersion);
            if (log.isDebugEnabled()) {
                log.debug("Get all URI templates response received to gateway from keymanager via web service"
                        + " call for:" + context + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            if (Util.tracingEnabled()) {
                Util.finishSpan(span);
            }
            ServiceContext serviceContext = keyValidationServiceStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            ArrayList<URITemplate> templates = new ArrayList<URITemplate>();
            for (org.wso2.carbon.apimgt.api.model.xsd.URITemplate aDto : dto) {
                URITemplate temp = toTemplates(aDto);
                templates.add(temp);
            }
            return templates;
        } catch (RemoteException e) {
            log.error("Error while accessing backend services for get URI templates", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } catch (Exception e) {
            log.error("Error while accessing backend services for get URI templates", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while accessing backend services for API key validation", e);
        }
    }

    public ArrayList<URITemplate> getAPIProductURITemplates(String context, String apiVersion
    ) throws APISecurityException {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password, keyValidationServiceStub._getServiceClient());
        if (cookie != null) {
            keyValidationServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        try {
            TracingSpan span = null;
            if (Util.tracingEnabled()) {
                TracingSpan keySpan = (TracingSpan) MessageContext.getCurrentMessageContext()
                        .getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
                TracingTracer tracer = Util.getGlobalTracer();
                span = Util.startSpan(APIMgtGatewayConstants.GET_ALL_URI_TEMPLATES, keySpan, tracer);
            }
            if (log.isDebugEnabled()) {
                log.debug("Get all URI templates request from gateway to keymanager via web service call for:"
                        + context + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            org.wso2.carbon.apimgt.api.model.xsd.URITemplate[] dto =
                    keyValidationServiceStub.getAPIProductURITemplates(context, apiVersion);
            if (log.isDebugEnabled()) {
                log.debug("Get all URI templates response received to gateway from keymanager via web service"
                        + " call for:" + context + " at "
                        + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            if (Util.tracingEnabled()) {
                Util.finishSpan(span);
            }
            ServiceContext serviceContext = keyValidationServiceStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            ArrayList<URITemplate> templates = new ArrayList<URITemplate>();
            for (org.wso2.carbon.apimgt.api.model.xsd.URITemplate aDto : dto) {
                URITemplate temp = toTemplates(aDto);
                templates.add(temp);
            }
            return templates;
        } catch (RemoteException e) {
            log.error("Error while accessing backend services for get product URI templates", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } catch (Exception e) {
            log.error("Error while accessing backend services for get product URI templates", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        }
    }

    private URITemplate toTemplates(
            org.wso2.carbon.apimgt.api.model.xsd.URITemplate dto) {
        URITemplate template = new URITemplate();
        template.setAuthType(dto.getAuthType());
        template.setHTTPVerb(dto.getHTTPVerb());
        template.setResourceSandboxURI(dto.getResourceSandboxURI());
        template.setUriTemplate(dto.getUriTemplate());
        template.setThrottlingTier(dto.getThrottlingTier());

        ConditionGroupDTO[] xsdConditionGroups = dto.getConditionGroups();
        org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO[] conditionGroups = new org.wso2.carbon.apimgt
                .api.dto.ConditionGroupDTO[xsdConditionGroups.length];

        for (short groupCounter = 0; groupCounter < xsdConditionGroups.length; groupCounter++) {
            org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO conditionGroup = new org.wso2.carbon.apimgt.api.dto
                    .ConditionGroupDTO();
            ConditionGroupDTO xsdConditionGroup = xsdConditionGroups[groupCounter];

            // Have to check nullity explicitly here because, in certain cases, length becomes 1 even when there are
            // no elements in the array.
            if(xsdConditionGroup != null) {
                conditionGroup.setConditionGroupId(xsdConditionGroup.getConditionGroupId());
                ConditionDTO[] xsdConditions = xsdConditionGroup.getConditions();

                if (xsdConditions != null) {
                    org.wso2.carbon.apimgt.api.dto.ConditionDTO[] conditions = new org.wso2.carbon.apimgt.api.dto
                            .ConditionDTO[xsdConditions.length];
                    for (short conditionCounter = 0; conditionCounter < xsdConditions.length; conditionCounter++) {

                        ConditionDTO xsdCondition = xsdConditions[conditionCounter];
                        if (xsdCondition != null) {
                            org.wso2.carbon.apimgt.api.dto.ConditionDTO condition = new org.wso2.carbon.apimgt.api.dto
                                    .ConditionDTO();
                            condition.setConditionName(xsdCondition.getConditionName());
                            condition.setConditionType(xsdCondition.getConditionType());
                            condition.setConditionValue(xsdCondition.getConditionValue());
                            condition.isInverted(xsdCondition.getInverted());
                            conditions[conditionCounter] = condition;
                        }
                    }
                    conditionGroup.setConditions(conditions);
                }
                conditionGroups[groupCounter] = conditionGroup;
            }
        }
        template.setConditionGroups(conditionGroups);
        template.setThrottlingConditions((Arrays.asList(dto.getThrottlingConditions())));
        return template;
    }

}
