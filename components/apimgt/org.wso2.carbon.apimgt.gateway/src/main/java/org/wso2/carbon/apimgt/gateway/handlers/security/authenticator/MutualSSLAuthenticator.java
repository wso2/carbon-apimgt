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
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

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
    private Map<String, Map<String, String>> certificates;

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
                HashMap<String, String> certificateData = new HashMap<>();
                int tierDivisionIndex = certificatePart.lastIndexOf("=");
                if (tierDivisionIndex > 0) {
                    String uniqueIdentifier = certificatePart.substring(0, tierDivisionIndex).trim();
                    String tierAndKeyTypeString = certificatePart.substring(tierDivisionIndex + 1);
                    String[] tierAndKeyType = tierAndKeyTypeString.split(APIConstants.DELEM_COLON);
                    certificateData.put(APIConstants.CLIENT_CERTIFICATE_TIER, tierAndKeyType[0]);
                    certificateData.put(APIConstants.CLIENT_CERTIFICATE_KEY_TYPE, tierAndKeyType[1]);
                    certificates.put(uniqueIdentifier, certificateData);
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
        if (Utils.isCertificateChainValidationEnabled()) {
            return authenticateCertificateChain(messageContext);
        }
        // try to retrieve the certificate
        Certificate sslCertObject;
        try {
            sslCertObject = Utils.getClientCertificate(axis2MessageContext);
            if (!APIUtil.isCertificateExistsInListenerTrustStore(sslCertObject)) {
                log.debug("Certificate in Header didn't exist in truststore");
                sslCertObject = null;
            }
        } catch (APIManagementException e) {
            return new AuthenticationResponse(false, isMandatory, !isMandatory,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
        }

        /* If the certificate cannot be retrieved from the axis2Message context, then mutual SSL authentication has
         not happened in transport level.*/
        if (sslCertObject == null) {
            return handleMutualSSLAuthenticationFailure(messageContext);
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
     * Authenticate with checking the client certificate chain.
     * @param messageContext    Message context
     * @return                  Authentication response
     */
    private AuthenticationResponse authenticateCertificateChain(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Certificate[] sslCerts;
        try {
            sslCerts = Utils.getClientCertificatesChain(axis2MessageContext);
        } catch (APIManagementException e) {
            return new AuthenticationResponse(false, isMandatory, !isMandatory,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
        }

        /* If the certificates cannot be retrieved from the axis2Message context, then mutual SSL authentication has
         not happened in transport level.*/
        if (sslCerts == null) {
            return handleMutualSSLAuthenticationFailure(messageContext);
        } else {
            try {
                setAuthContext(messageContext, sslCerts);
            } catch (APISecurityException ex) {
                return new AuthenticationResponse(false, isMandatory, !isMandatory, ex.getErrorCode(), ex.getMessage());
            } catch (APIManagementException ex) {
                return new AuthenticationResponse(false, isMandatory, !isMandatory,
                        (int) ex.getErrorHandler().getErrorCode(), ex.getMessage());
            }
        }
        return new AuthenticationResponse(true, isMandatory, true, 0, null);
    }

    /**
     * To set the authentication context in current message context.
     *
     * @param messageContext Relevant message context.
     * @param certificate    SSL certificate.
     * @throws APISecurityException API Security Exception.
     */
    private void setAuthContext(MessageContext messageContext, Certificate certificate) throws APISecurityException {

        X509Certificate x509Certificate = Utils.convertCertificateToX509Certificate(certificate);
        String subjectDN = x509Certificate.getSubjectDN().getName();
        String uniqueIdentifier = (x509Certificate.getSerialNumber() + "_" + x509Certificate.getSubjectDN()).replaceAll(",",
                        "#").replaceAll("\"", "'").trim();
        /* Since there can be previously deleted certificates persisted in the trust store that matches with the
        certificate object but not in the certificates list for the particular API.
        */
        if (certificates.get(uniqueIdentifier) == null ) {
            handleCertificateNotAssociatedToAPIFailure(messageContext);
        }
        String tier = certificates.get(uniqueIdentifier).get(APIConstants.CLIENT_CERTIFICATE_TIER);
        String keyType = certificates.get(uniqueIdentifier).get(APIConstants.CLIENT_CERTIFICATE_KEY_TYPE);
        if (StringUtils.isEmpty(tier)) {
            handleCertificateNotAssociatedToAPIFailure(messageContext);
        }
        setAuthenticationContext(messageContext, subjectDN, uniqueIdentifier, tier, keyType);
    }

    /**
     * Sets the authentication context in current message context.
     *
     * @param messageContext    Relevant message context.
     * @param certificatesArray SSL certificate chain.
     * @throws APISecurityException API Security exception.
     */
    private void setAuthContext(MessageContext messageContext, Certificate[] certificatesArray)
            throws APISecurityException, APIManagementException {

        String tier = null;
        String keyType = null;
        List<X509Certificate> x509Certificates = Utils.convertCertificatesToX509Certificates(certificatesArray);
        String subjectDN = x509Certificates.get(0).getSubjectDN().getName();
        String issuerDNIdentifier = x509Certificates.get(x509Certificates.size() - 1).getIssuerDN().getName()
                .replaceAll(",", "#").replaceAll("\"", "'").trim();
        String uniqueIdentifier = null;
        List<String> subjectDNIdentifiers = new ArrayList<>();
        for (X509Certificate x509Certificate : x509Certificates) {
            String subjectDNIdentifier = (x509Certificate.getSerialNumber() + "_"
                    + x509Certificate.getSubjectDN()).replaceAll(",", "#").replaceAll("\"", "'").trim();
            subjectDNIdentifiers.add(subjectDNIdentifier);
            for (Map.Entry<String, Map<String, String>> entry : certificates.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.equals(subjectDNIdentifier, key)) {
                    uniqueIdentifier = key;
                    tier = entry.getValue().get(APIConstants.CLIENT_CERTIFICATE_TIER);
                    keyType = entry.getValue().get(APIConstants.CLIENT_CERTIFICATE_KEY_TYPE);
                    break;
                }
            }
            if (StringUtils.isNoneEmpty(tier)) {
                break;
            }
        }
        if (StringUtils.isEmpty(tier)) {
            for (Map.Entry<String, Map<String, String>> entry : certificates.entrySet()) {
                String key = entry.getKey();
                if (key.contains(issuerDNIdentifier)) {
                    uniqueIdentifier = key;
                    tier = entry.getValue().get(APIConstants.CLIENT_CERTIFICATE_TIER);
                    keyType = entry.getValue().get(APIConstants.CLIENT_CERTIFICATE_KEY_TYPE);
                }
            }
        }
        if (StringUtils.isEmpty(tier) || StringUtils.isEmpty(keyType)) {
            subjectDNIdentifiers = getUniqueIdentifierFromCompleteCertificateChain(x509Certificates, subjectDNIdentifiers);
            tier = getTierFromCompleteCertificateChain(subjectDNIdentifiers);
            keyType = getKeyTypeFromCompleteCertificateChain(subjectDNIdentifiers);
        }

        if (StringUtils.isEmpty(tier) || StringUtils.isEmpty(keyType)) {
            handleCertificateNotAssociatedToAPIFailure(messageContext);
        }
        setAuthenticationContext(messageContext, subjectDN, uniqueIdentifier, tier, keyType);
    }

    /**
     * Fetches the list of uniqueIdentifiers for complete certificate chain using certificates in truststore.
     *
     * @param x509Certificates client certificates chain
     * @param uniqueIdentifiers Unique identifiers list for client certificate chain
     * @return uniqueIdentifiers
     * @throws APIManagementException
     */
    private List<String> getUniqueIdentifierFromCompleteCertificateChain(List<X509Certificate> x509Certificates,
                List<String> uniqueIdentifiers) throws APIManagementException {

        X509Certificate certificate = x509Certificates.get(x509Certificates.size() - 1);
        String subjectDN = certificate.getSubjectDN().getName();
        String issuerDN = certificate.getIssuerDN().getName();
        boolean isIssuerCertificateUpdated = !StringUtils.equals(subjectDN, issuerDN);

        while (isIssuerCertificateUpdated) {
            X509Certificate issuerCertificate = Utils.getCertificateFromListenerTrustStore(issuerDN);
            if (issuerCertificate == null) {
                isIssuerCertificateUpdated = false;
            } else {
                issuerDN = issuerCertificate.getIssuerDN().getName();
                subjectDN = issuerCertificate.getSubjectDN().getName();
                String uniqueIdentifier = (issuerCertificate.getSerialNumber() + "_"
                        + issuerCertificate.getSubjectDN()).replaceAll(",", "#").replaceAll("\"", "'").trim();
                uniqueIdentifiers.add(uniqueIdentifier);
                isIssuerCertificateUpdated = !StringUtils.equals(subjectDN, issuerDN);
            }
        }
        return uniqueIdentifiers;
    }

    /**
     * Fetches tier assigned to the client certificate after making the complete certificate chain using certificates
     * in truststore.
     *
     * @param uniqueIdentifiers Unique identifiers list for client certificate chain
     * @return Tier
     */
    private String getTierFromCompleteCertificateChain(List<String> uniqueIdentifiers) {

        String tier = null;
        for (String uniqueIdentifier : uniqueIdentifiers) {
            tier = certificates.get(uniqueIdentifier) == null ? null :
                    certificates.get(uniqueIdentifier).get(APIConstants.CLIENT_CERTIFICATE_TIER);
            if (StringUtils.isNotEmpty(tier)) {
                break;
            }
        }
        return tier;
    }

    /**
     * Fetches keyType assigned to the client certificate after making the complete certificate chain using certificates
     * in truststore.
     *
     * @param uniqueIdentifiers Unique identifiers list for client certificate chain
     * @return keyType
     */
    private String getKeyTypeFromCompleteCertificateChain(List<String> uniqueIdentifiers) {

        String keyType = null;
        for (String uniqueIdentifier : uniqueIdentifiers) {
            keyType = certificates.get(uniqueIdentifier) == null ? null :
                    certificates.get(uniqueIdentifier).get(APIConstants.CLIENT_CERTIFICATE_KEY_TYPE);
            if (StringUtils.isNotEmpty(keyType)) {
                break;
            }
        }
        return keyType;
    }

    /**
     * Handles failures where message context does not contain client certificates.
     * @param messageContext    Relevant message context
     * @return                  Authentication response
     */
    private AuthenticationResponse handleMutualSSLAuthenticationFailure(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("Mutual SSL authentication has not happened in the transport level for the API "
                    + getAPIIdentifier(messageContext).toString() + ", hence API invocation is not allowed");
        }
        if (isMandatory) {
            log.error("Mutual SSL authentication failure");
        }
        return new AuthenticationResponse(false, isMandatory, !isMandatory,
                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    /**
     * Handles failures where certificates in the message context not associated with the API.
     * @param messageContext            Relevant message context
     * @throws APISecurityException     API security exception
     */
    private void handleCertificateNotAssociatedToAPIFailure(MessageContext messageContext) throws APISecurityException {

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

    /**
     * Set AuthenticationContext to message context.
     * @param messageContext    Relevant message context
     * @param subjectDN         SubjectDN of the client certificate
     * @param uniqueIdentifier  Unique Identifier of the stored certificate
     * @param tier              Throttling policy tier
     */
    private void setAuthenticationContext(MessageContext messageContext, String subjectDN, String uniqueIdentifier,
            String tier, String keyType) {

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setUsername(subjectDN);
        try {
            LdapName ldapDN = new LdapName(subjectDN);
            for (Rdn rdn : ldapDN.getRdns()) {
                if (APIConstants.CERTIFICATE_COMMON_NAME.equalsIgnoreCase(rdn.getType())) {
                    authContext.setUsername((String) rdn.getValue());
                    messageContext.setProperty(APIConstants.CERTIFICATE_COMMON_NAME, rdn.getValue());
                }
            }
        } catch (InvalidNameException e) {
            log.warn("Cannot get the CN name from certificate:" + e.getMessage() + ". Please make sure the "
                    + "certificate to include a proper common name that follows naming convention.");
            authContext.setUsername(subjectDN);
        }
        authContext.setApiTier(apiLevelPolicy);
        APIIdentifier apiIdentifier = getAPIIdentifier(messageContext);
        authContext.setKeyType(keyType);
        authContext.setStopOnQuotaReach(true);
        authContext.setApiKey(uniqueIdentifier + "_" + apiIdentifier.toString());
        authContext.setTier(tier);
        authContext.setApplicationName(APIConstants.MUTUAL_SSL_AUTH_APPLICATION_NAME);
        authContext.setSubscriber(APIConstants.MUTUAL_SSL_AUTH_APPLICATION_OWNER);
        authContext.setApplicationId(APIConstants.MUTUAL_SSL_AUTH_APPLICATION_NAME);
        authContext.setApplicationUUID(APIConstants.MUTUAL_SSL_AUTH_APPLICATION_NAME);
        /* For the mutual SSL based authenticated request, the resource level throttling is not considered, hence
        assigning the unlimited tier for that. */
        List<VerbInfoDTO> verbInfoList = new ArrayList<>(1);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling(APIConstants.UNLIMITED_TIER);
        verbInfoList.add(verbInfoDTO);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);
        if (log.isDebugEnabled()) {
            log.debug("Auth context for the API " + getAPIIdentifier(messageContext) + ": Username["
                    + authContext.getUsername() + "APIKey[(" + authContext.getApiKey() + "] Tier["
                    + authContext.getTier() + "]");
        }
        messageContext.setProperty(APIMgtGatewayConstants.END_USER_NAME, authContext.getUsername());
        APISecurityUtils.setAuthenticationContext(messageContext, authContext, null);
    }

    /**
     * To get the API Identifier of the current API.
     *
     * @param messageContext Current message context
     * @return API Identifier of currently accessed API.
     */
    private APIIdentifier getAPIIdentifier(MessageContext messageContext) {

        API api = GatewayUtils.getAPI(messageContext);
        return new APIIdentifier(api.getApiProvider(), api.getApiName(), api.getApiVersion());
    }

    @Override
    public String getChallengeString() {
        return "Mutual SSL realm=\"" + ServiceReferenceHolder.getInstance().getServerConfigurationService()
                .getFirstProperty("Name") + "\"";
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
