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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.opensaml.xmlsec.signature.support.SignerProvider;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import org.wso2.carbon.hostobjects.sso.exception.SSOHostObjectException;
import org.wso2.carbon.hostobjects.sso.internal.util.*;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AuthReqBuilder {

    private static Log log = LogFactory.getLog(AuthReqBuilder.class);
        /**
     * Generate an authentication request.
     *
     * @return AuthnRequest Object
     * @throws SSOHostObjectException error when bootstrapping
     */
    public AuthnRequest buildAuthenticationRequest(String issuerId, String acsUrl, boolean isPassive,
            String nameIdPolicy) throws SSOHostObjectException {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer( issuerId));
        authnRequest.setNameIDPolicy(Util.buildNameIDPolicy(nameIdPolicy));
        if (isPassive){
            authnRequest.setIsPassive(true);
        }
        if (!StringUtils.isEmpty(acsUrl)) {
            acsUrl = Util.processAcsUrl(acsUrl);
            authnRequest.setAssertionConsumerServiceURL(acsUrl);
        }
        return authnRequest;
    }
    
    /**
     * Generate an Signed authentication request with a custom consumer url.
     *
     * @return AuthnRequest Object
     * @throws SSOHostObjectException error when bootstrapping
     */

    public AuthnRequest buildSignedAuthRequest(String issuerId, String destination, String acsUrl, boolean isPassive,
            int tenantId, String tenantDomain, String nameIdPolicy) throws SSOHostObjectException {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer(issuerId));
        authnRequest.setNameIDPolicy(Util.buildNameIDPolicy(nameIdPolicy));
        if (!StringUtils.isEmpty(acsUrl)) {
            acsUrl = Util.processAcsUrl(acsUrl);
            authnRequest.setAssertionConsumerServiceURL(acsUrl);
        }
        if (isPassive){
            authnRequest.setIsPassive(true);
        }
        authnRequest.setDestination(destination);
        SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential =
                new SSOAgentCarbonX509Credential(tenantId, tenantDomain);
        setSignature(authnRequest, SignatureConstants.ALGO_ID_SIGNATURE_RSA,
                new X509CredentialImpl(ssoAgentCarbonX509Credential));
        return authnRequest;
    }

    /**
     * Build the issuer object
     *
     * @return Issuer object
     */
    private static Issuer buildIssuer(String issuerId) {
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(issuerId);
        return issuer;
    }

    /**
     * Sign the SAML AuthnRequest message
     *
     * @param authnRequest SAML Authentication request
     * @param signatureAlgorithm Signature algorithm
     * @param cred X.509 credential object
     * @return SAML Authentication request including the signature
     */
    public static AuthnRequest setSignature(AuthnRequest authnRequest, String signatureAlgorithm,
            X509Credential cred) throws SSOHostObjectException {
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            Signature signature = (Signature) Util.buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            KeyInfo keyInfo = (KeyInfo) Util.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            X509Data data = (X509Data) Util.buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
            X509Certificate cert = (X509Certificate) Util.buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);
            String value = Base64Support.encode(cred.getEntityCertificate().getEncoded(), Base64Support.UNCHUNKED);
            cert.setValue(value);
            data.getX509Certificates().add(cert);
            keyInfo.getX509Datas().add(data);
            signature.setKeyInfo(keyInfo);

            authnRequest.setSignature(signature);

            List<Signature> signatureList = new ArrayList<Signature>();
            signatureList.add(signature);

            // Marshall and Sign
            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(authnRequest);

            marshaller.marshall(authnRequest);
            
            thread.setContextClassLoader(SignerProvider.class.getClassLoader());
            Signer.signObjects(signatureList);
            return authnRequest;
        } catch (CertificateEncodingException e) {
            handleException("Error getting certificate", e);
        } catch (MarshallingException e) {
            handleException("Error while marshalling auth request", e);
        } catch (SignatureException e) {
            handleException("Error while signing the SAML Request message", e);
        } catch (SSOHostObjectException e) {
            handleException("Error while signing the SAML Request message", e);
        }  finally {
            thread.setContextClassLoader(originalClassLoader);
        }
        return null;
    }

    private static void handleException(String errorMessage, Throwable e) throws SSOHostObjectException {
        log.error(errorMessage);
        throw new SSOHostObjectException(errorMessage, e);
    }
}
