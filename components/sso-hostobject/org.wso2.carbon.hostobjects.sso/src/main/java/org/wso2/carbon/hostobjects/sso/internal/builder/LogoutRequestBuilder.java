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

package org.wso2.carbon.hostobjects.sso.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.util.Base64;
import org.wso2.carbon.hostobjects.sso.exception.SSOHostObjectException;
import org.wso2.carbon.hostobjects.sso.internal.util.*;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to generate the Logout Requests.
 */
public class LogoutRequestBuilder {

    private static Log log = LogFactory.getLog(LogoutRequestBuilder.class);

    /**
     * Build the logout request
     * @param subject name of the user
     * @param reason reason for generating logout request.
     * @return LogoutRequest object
     */
    public LogoutRequest buildLogoutRequest(String subject,String sessionIndexId, String reason,
                                            String issuerId, String nameIdFormat) {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        logoutReq.setNameID(Util.buildNameID(nameIdFormat, subject));

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(sessionIndexId);
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason(reason);

        return logoutReq;
    }

    /**
     * Build the logout request
     * @param subject name of the user
     * @param reason reason for generating logout request.
     * @return LogoutRequest object
     */
    public LogoutRequest buildSignedLogoutRequest(String subject,String sessionIndexId, String reason,
            String issuerId, int tenantId, String tenantDomain, String destination, String nameIdFormat)
            throws SSOHostObjectException {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        logoutReq.setNameID(Util.buildNameID(nameIdFormat, subject));

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(sessionIndexId);
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason(reason);
        logoutReq.setDestination(destination);

        SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential =
                new SSOAgentCarbonX509Credential(tenantId, tenantDomain);
        setSignature(logoutReq, SignatureConstants.ALGO_ID_SIGNATURE_RSA,
                new X509CredentialImpl(ssoAgentCarbonX509Credential));

        return logoutReq;
    }

    /**
     * Overload Logout request for sessionIndexId is not exist case
     *
     * @param subject Subject
     * @param reason Reason for logout
     * @param issuerId id of issuer
     * @return SAML logout request
     */
    public LogoutRequest buildLogoutRequest(String subject, String reason,
                                            String issuerId, String nameIdFormat) {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        logoutReq.setNameID(Util.buildNameID(nameIdFormat, subject));

        logoutReq.setReason(reason);

        return logoutReq;
    }

    /**
     * Overload Logout request for sessionIndexId is not exist case
     *
     * @param subject Subject
     * @param reason Reason for logout
     * @param issuerId id of issuer
     * @return Signed SAML logout request
     */
    public LogoutRequest buildSignedLogoutRequest(String subject, String reason,
            String issuerId, int tenantId, String tenantDomain, String destination, String nameIdFormat)
            throws SSOHostObjectException {
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        logoutReq.setIssuer(issuer);

        logoutReq.setNameID(Util.buildNameID(nameIdFormat, subject));

        logoutReq.setReason(reason);
        logoutReq.setDestination(destination);

        SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential =
                new SSOAgentCarbonX509Credential(tenantId, tenantDomain);
        setSignature(logoutReq, SignatureConstants.ALGO_ID_SIGNATURE_RSA,
                new X509CredentialImpl(ssoAgentCarbonX509Credential));

        return logoutReq;
    }

    /**
     * Sign the SAML LogoutRequest message
     *
     * @param logoutRequest SAML logout request
     * @param signatureAlgorithm Signature algorithm
     * @param cred X.509 credential object
     * @return SAML logout request including the signature
     */
    public static LogoutRequest setSignature(LogoutRequest logoutRequest, String signatureAlgorithm,
            X509Credential cred) throws SSOHostObjectException {
        try {
            Signature signature = (Signature) Util.buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            KeyInfo keyInfo = (KeyInfo) Util.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            X509Data data = (X509Data) Util.buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
            org.opensaml.xml.signature.X509Certificate cert =
                    (org.opensaml.xml.signature.X509Certificate) Util.buildXMLObject(
                            org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
            String value = Base64.encodeBytes(cred.getEntityCertificate().getEncoded());
            cert.setValue(value);
            data.getX509Certificates().add(cert);
            keyInfo.getX509Datas().add(data);
            signature.setKeyInfo(keyInfo);

            logoutRequest.setSignature(signature);

            List<Signature> signatureList = new ArrayList<Signature>();
            signatureList.add(signature);

            // Marshall and Sign
            MarshallerFactory marshallerFactory =
                    org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(logoutRequest);

            marshaller.marshall(logoutRequest);

            Signer.signObjects(signatureList);
            return logoutRequest;
        } catch (CertificateEncodingException e) {
            handleException("Error getting certificate", e);
        } catch (MarshallingException e) {
            handleException("Error while marshalling logout request", e);
        } catch (SignatureException e) {
            handleException("Error while signing the SAML logout request", e);
        } catch (SSOHostObjectException e) {
            handleException("Error while signing the SAML logout request", e);
        }
        return null;
    }

    private static void handleException(String errorMessage, Throwable e) throws SSOHostObjectException {
        log.error(errorMessage);
        throw new SSOHostObjectException(errorMessage, e);
    }

}
