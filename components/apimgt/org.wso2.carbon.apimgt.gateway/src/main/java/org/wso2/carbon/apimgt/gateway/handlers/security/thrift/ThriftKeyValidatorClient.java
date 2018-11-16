/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationService;
import org.wso2.carbon.apimgt.impl.generated.thrift.ConditionGroupDTO;

import java.util.ArrayList;
import java.util.List;

public class ThriftKeyValidatorClient {

    private ThriftUtils thriftUtils = null;
    private String sessionId = null;
    private static final Log log = LogFactory.getLog(ThriftKeyValidatorClient.class);
    private APIKeyValidationService.Client keyValClient = null;

    public ThriftKeyValidatorClient() throws APISecurityException {
        try {
            thriftUtils = ThriftUtils.getInstance();
            sessionId = thriftUtils.getSessionId();
            //create new APIKeyValidator client
            TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();

            param.setTrustStore(thriftUtils.getTrustStorePath(), thriftUtils.getTrustStorePassword());

            TTransport transport = TSSLTransportFactory.getClientSocket(
                    ThriftUtils.getThriftServerHost(), thriftUtils.getThriftPort(),
                    thriftUtils.getThriftClientConnectionTimeOut(), param);

            //TProtocol protocol = new TCompactProtocol(transport);
            //TODO:needs to decide on the optimum protocol.
            TProtocol protocol = new TBinaryProtocol(transport);

            //create thrift based entitlement service client and invoke
            keyValClient = new APIKeyValidationService.Client(protocol);

        } catch (TTransportException e) {
            log.error("Could not connect to Thrift host", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }
    }

    @MethodStats
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
                                                 String requiredAuthenticationLevel, String clientDomain,
                                                 String matchingResource, String httpVerb) throws APISecurityException {

        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
        org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyValidationInfoDTO thriftDTO;

        try {
            thriftDTO = keyValClient.validateKey(context, apiVersion, apiKey, sessionId, requiredAuthenticationLevel,
                                                 clientDomain, matchingResource, httpVerb);

        } catch (Exception e) {
            try {

                log.warn("Login failed.. Authenticating again..");
                sessionId = thriftUtils.reLogin();
                //we re-initialize the thrift client in case open sockets have been closed due to
                //key manager restart.
                reInitializeClient();
                thriftDTO = keyValClient.validateKey(context, apiVersion, apiKey, sessionId, requiredAuthenticationLevel,
                                                     clientDomain, matchingResource, httpVerb);

            } catch (Exception e1) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e1.getMessage(), e1);
            }
        }
        //do the conversion other side
        apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApplicationName(thriftDTO.getApplicationName());
        apiKeyValidationInfoDTO.setAuthorized(thriftDTO.isAuthorized());
        apiKeyValidationInfoDTO.setEndUserName(thriftDTO.getEndUserName());
        apiKeyValidationInfoDTO.setEndUserToken(thriftDTO.getEndUserToken());
        apiKeyValidationInfoDTO.setSubscriber(thriftDTO.getSubscriber());
        apiKeyValidationInfoDTO.setTier(thriftDTO.getTier());
        apiKeyValidationInfoDTO.setType(thriftDTO.getType());
        apiKeyValidationInfoDTO.setValidationStatus(thriftDTO.getValidationStatus());
        apiKeyValidationInfoDTO.setApplicationId(thriftDTO.getApplicationId());
        apiKeyValidationInfoDTO.setApplicationTier(thriftDTO.getApplicationTier());
        apiKeyValidationInfoDTO.setApiName(thriftDTO.getApiName());
        apiKeyValidationInfoDTO.setApiPublisher(thriftDTO.getApiPublisher());
        apiKeyValidationInfoDTO.setConsumerKey(thriftDTO.getConsumerKey());
        //apiKeyValidationInfoDTO.setAuthorizedDomains(thriftDTO.getAuthorizedDomains());
        apiKeyValidationInfoDTO.setScopes(thriftDTO.getScopes());
        apiKeyValidationInfoDTO.setIssuedTime(thriftDTO.getIssuedTime());
        apiKeyValidationInfoDTO.setValidityPeriod(thriftDTO.getValidityPeriod());
        apiKeyValidationInfoDTO.setApiTier(thriftDTO.getApiTier());
        apiKeyValidationInfoDTO.setThrottlingDataList(thriftDTO.getThrottlingDataList());
        apiKeyValidationInfoDTO.setSpikeArrestLimit(thriftDTO.getSpikeArrestLimit());
        apiKeyValidationInfoDTO.setSpikeArrestUnit(thriftDTO.getSpikeArrestUnit());
        apiKeyValidationInfoDTO.setSubscriberTenantDomain(thriftDTO.getSubscriberTenantDomain());
        apiKeyValidationInfoDTO.setStopOnQuotaReach(thriftDTO.isStopOnQuotaReach());
        apiKeyValidationInfoDTO.setContentAware(thriftDTO.isIsContentAware());
        return apiKeyValidationInfoDTO;
    }
    @MethodStats
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException {
        ArrayList<URITemplate> templates=new ArrayList<URITemplate>();
        List<org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate> uriTemplates;

        try {
            uriTemplates = keyValClient.getAllURITemplates(context, apiVersion,sessionId);

        } catch (Exception e) {
            try {

                log.warn("Login failed.. Authenticating again..");
                sessionId = thriftUtils.reLogin();
                //we re-initialize the thrift client in case open sockets have been closed due to
                //key manager restart.
                reInitializeClient();
                uriTemplates = keyValClient.getAllURITemplates(context, apiVersion, sessionId);

            } catch (Exception e1) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e1.getMessage(), e1);
            }
        }
        for (org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate aDto : uriTemplates) {
            URITemplate temp = toTemplates(aDto);
            templates.add(temp);
        }
        return templates;
    }


    private URITemplate toTemplates(
            org.wso2.carbon.apimgt.impl.generated.thrift.URITemplate dto) {
        URITemplate template = new URITemplate();
        template.setAuthType(dto.getAuthType());
        template.setHTTPVerb(dto.getHttpVerb());
        template.setResourceSandboxURI(dto.getResourceSandboxURI());
        template.setUriTemplate(dto.getUriTemplate());
        template.setThrottlingTier(dto.getThrottlingTier());
        template.setThrottlingConditions(dto.getThrottlingConditions());
        List<ConditionGroupDTO> conditionGroupsThrift = dto.getConditionGroups();

        // Create a URITemplate object out of the type specific to Thrift protocol.
        if (conditionGroupsThrift != null && !conditionGroupsThrift.isEmpty()) {
            org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO[] conditionGroups = new org.wso2.carbon.apimgt.api.dto
                    .ConditionGroupDTO[conditionGroupsThrift.size()];
            for (short groupCounter = 0; groupCounter < conditionGroupsThrift.size(); groupCounter++) {
                org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO conditionGroup = new org.wso2.carbon.apimgt.api.dto
                        .ConditionGroupDTO();
                ConditionGroupDTO conditionGroupThrift = conditionGroupsThrift.get(groupCounter);
                conditionGroup.setConditionGroupId(conditionGroupThrift.getConditionGroupId());
                List<org.wso2.carbon.apimgt.impl.generated.thrift.ConditionDTO> conditionsThrift = conditionGroupThrift
                        .getConditions();
                if (conditionsThrift != null && !conditionsThrift.isEmpty()) {
                    ConditionDTO[] conditions = new ConditionDTO[conditionsThrift.size()];
                    for (short conditionCounter = 0; conditionCounter < conditionsThrift.size(); conditionCounter++) {
                        ConditionDTO condition = new ConditionDTO();
                        org.wso2.carbon.apimgt.impl.generated.thrift.ConditionDTO conditionThrift = conditionsThrift
                                .get(conditionCounter);
                        condition.setConditionType(conditionThrift.getConditionType());
                        condition.setConditionName(conditionThrift.getConditionName());
                        condition.setConditionValue(conditionThrift.getConditionValue());
                        condition.isInverted(conditionThrift.isIsInverted());
                        conditions[conditionCounter] = condition;
                    }
                    conditionGroup.setConditions(conditions);
                }
                conditionGroups[groupCounter] = conditionGroup;
            }
            template.setConditionGroups(conditionGroups);
        }

        template.setApplicableLevel(dto.getApplicableLevel());
        return template;
    }


    private void reInitializeClient() throws APISecurityException, TTransportException {
            //create new APIKeyValidator client
            TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();

            param.setTrustStore(thriftUtils.getTrustStorePath(), thriftUtils.getTrustStorePassword());

            TTransport transport = TSSLTransportFactory.getClientSocket(
                    ThriftUtils.getThriftServerHost(), thriftUtils.getThriftPort(),
                    thriftUtils.getThriftClientConnectionTimeOut(), param);

            //TProtocol protocol = new TCompactProtocol(transport);
            //TODO:needs to decide on the optimum protocol.
            TProtocol protocol = new TBinaryProtocol(transport);

            //create thrift based entitlement service client and invoke
            keyValClient = new APIKeyValidationService.Client(protocol);
    }
}
