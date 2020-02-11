/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.authenticator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.cert.X509Certificate;

/**
 * Authenticator responsible for handle API requests with mutual SSL.
 */
public class MutualSSLAuthenticator implements Authenticator {
    private static final Log log = LogFactory.getLog(MutualSSLAuthenticator.class);
    private String apiLevelPolicy;
    private String requestOrigin;
    private static String challengeString;
    private boolean isMandatory;

    // <UniqueIdentifierName,Tier> -Format
    private HashMap<String, String> certificates;

    static {
        challengeString = "Mutual SSL realm=\"" + ServiceReferenceHolder.getInstance().getServerConfigurationService()
                .getFirstProperty("Name") + "\"";
    }

    /**
     * Initialized the mutual SSL authenticator.
     *
     * @param apiLevelPolicy     API level throttling policy.
     * @param certificateDetails Details of certificates associated with the particular API.
     */
    public MutualSSLAuthenticator(String apiLevelPolicy, boolean isMandatory, String certificateDetails) {
        this.apiLevelPolicy = apiLevelPolicy;
        certificates = new HashMap<>();
        if (StringUtils.isNotEmpty(certificateDetails)) {
            String[] certificateParts = certificateDetails.substring(1, certificateDetails.length() - 1).split(",");
            for (String certificatePart : certificateParts) {
                int tierDivisionIndex = certificatePart.lastIndexOf("=");
                if (tierDivisionIndex > 0) {
                    String uniqueIdentifier = certificatePart.substring(0, tierDivisionIndex).trim();
                    String tier = certificatePart.substring(tierDivisionIndex + 1);
                    certificates.put(uniqueIdentifier, tier);
                }
            }
        }
        this.isMandatory = isMandatory;
    }

    @Override
    public void init(SynapseEnvironment env) {
        // Nothing to do in init phase.
    }

    @Override
    public void destroy() {
        // Nothing to do in destroy phase.
    }

    @Override
    public AuthenticationResponse authenticate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        // try to retrieve the certificate
        X509Certificate sslCertObject;
        try {
            sslCertObject = Utils.getClientCertificate(axis2MessageContext);
        } catch (APIManagementException e) {
            return new AuthenticationResponse(false, isMandatory, !isMandatory,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
        }

        /* If the certificate cannot be retrieved from the axis2Message context, then mutual SSL authentication has
         not happened in transport level.*/
        if (sslCertObject == null) {
            if (log.isDebugEnabled()) {
                log.debug("Mutual SSL authentication has not happened in the transport level for the API "
                        + getAPIIdentifier(messageContext).toString() + ", hence API invocation is not allowed");
            }
            if (isMandatory) {
                log.error("Mutual SSL authentication failure");
            }
            return new AuthenticationResponse(false, isMandatory, !isMandatory,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        } else {
            try {
                setAuthContext(messageContext, sslCertObject);
            } catch (APISecurityException ex) {
                return new AuthenticationResponse(false, isMandatory, !isMandatory, ex.getErrorCode(), ex.getMessage());
            }
        }
        return new AuthenticationResponse(true, isMandatory, true, 0, null);
    }

    /**
     * To set the authentication context in current message context.
     *
     * @param messageContext Relevant message context.
     * @param x509Certificate  SSL certificate.
     * @throws APISecurityException API Security Exception.
     */
    private void setAuthContext(MessageContext messageContext, X509Certificate x509Certificate) throws APISecurityException {

        String subjectDN = x509Certificate.getSubjectDN().getName();
        String uniqueIdentifier = (x509Certificate.getSerialNumber() + "_" + x509Certificate.getIssuerDN()).replaceAll(",",
                        "#").replaceAll("\"", "'").trim();
        String tier = certificates.get(uniqueIdentifier);
        if (StringUtils.isEmpty(tier)) {
            if (log.isDebugEnabled()) {
                log.debug("The client certificate presented is available in gateway, however it was not added against "
                        + "the API " + getAPIIdentifier(messageContext));
            }
            if (isMandatory) {
                log.error("Mutual SSL authentication failure. API is not associated with the certificate");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setUsername(subjectDN);
        try {
            LdapName ldapDN = new LdapName(subjectDN);
            for (Rdn rdn : ldapDN.getRdns()) {
                if (APIConstants.CERTIFICATE_COMMON_NAME.equalsIgnoreCase(rdn.getType())) {
                    authContext.setUsername((String) rdn.getValue());
                }
            }
        } catch (InvalidNameException e) {
            log.warn("Cannot get the CN name from certificate:" + e.getMessage() + ". Please make sure the "
                    + "certificate to include a proper common name that follows naming convention.");
            authContext.setUsername(subjectDN);
        }
        authContext.setApiTier(apiLevelPolicy);
        APIIdentifier apiIdentifier = getAPIIdentifier(messageContext);
        authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        authContext.setStopOnQuotaReach(true);
        authContext.setApiKey(uniqueIdentifier + "_" + apiIdentifier.toString());
        authContext.setTier(tier);
        /* For the mutual SSL based authenticated request, the resource level throttling is not considered, hence
        assigning the unlimited tier for that. */
        List<VerbInfoDTO> verbInfoList = new ArrayList<>(1);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling(APIConstants.UNLIMITED_TIER);
        verbInfoList.add(verbInfoDTO);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);
        if (log.isDebugEnabled()) {
            log.debug("Auth context for the API " + getAPIIdentifier(messageContext) + ": Username[" + authContext
                    .getUsername() + "APIKey[(" + authContext.getApiKey() + "] Tier[" + authContext.getTier() + "]");
        }
        APISecurityUtils.setAuthenticationContext(messageContext, authContext, null);
    }

    /**
     * To get the API Identifier of the current API.
     *
     * @param messageContext Current message context
     * @return API Identifier of currently accessed API.
     */
    private APIIdentifier getAPIIdentifier(MessageContext messageContext) {
        String apiWithversion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        String api = null;
        //if publisher is null,extract the publisher from the api_version
        if (apiPublisher == null && apiWithversion != null) {
            int ind = apiWithversion.indexOf("--");
            apiPublisher = apiWithversion.substring(0, ind);
        }

        if (apiWithversion != null) {
            int index = apiWithversion.indexOf("--");
            if (index != -1) {
                apiWithversion = apiWithversion.substring(index + 2);
            }
            String[] splitParts = apiWithversion.split(":");
            api = splitParts[0];
            apiWithversion = splitParts[1].substring(1);
        }
        return new APIIdentifier(apiPublisher, api, apiWithversion);
    }

    @Override
    public String getChallengeString() {
        return challengeString;
    }

    @Override
    public String getRequestOrigin() {
        return requestOrigin;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * Return Client Certificate from Message Context or from Header.
     * @param axis2MessageContext
     * @return X509 Certificate Object
     * @throws APISecurityException
     */

}
