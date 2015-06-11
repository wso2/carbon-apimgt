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
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.util.Base64;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.hostobjects.sso.internal.util.*;
import javax.xml.namespace.QName;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthReqBuilder {

    private static Log log = LogFactory.getLog(AuthReqBuilder.class);
        /**
     * Generate an authentication request.
     *
     * @return AuthnRequest Object
     * @throws Exception error when bootstrapping
     */
    public AuthnRequest buildAuthenticationRequest(String issuerId) throws Exception {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer( issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        return authnRequest;
    }

    /**
     * Generate an Authentication request with passiveAuth and assertionConsumerServiceURL
     *
     * @return AuthnRequest Object
     * @throws Exception error when bootstrapping
     */
    public AuthnRequest buildPassiveAuthenticationRequest(String issuerId, String acsUrl) throws Exception  {
        Util.doBootstrap();
        acsUrl = processAcsUrl(acsUrl);
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer(issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        authnRequest.setIsPassive(true);
        authnRequest.setAssertionConsumerServiceURL(acsUrl);
        return authnRequest;
    }

    /**
     * Generate a signed Authentication request with passiveAuth
     *
     * @param issuerId
     * @param acsUrl
     * @return
     * @throws Exception
     */
    public AuthnRequest buildPassiveSignedAuthenticationRequest(String issuerId, int tenantId,
            String tenantDomain, String destination, String acsUrl) throws Exception {
        Util.doBootstrap();
        acsUrl = processAcsUrl(acsUrl);
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer(issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        authnRequest.setIsPassive(true);
        authnRequest.setAssertionConsumerServiceURL(acsUrl);
        authnRequest.setDestination(destination);
        SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential =
                new SSOAgentCarbonX509Credential(tenantId, tenantDomain);
        setSignature(authnRequest, SignatureConstants.ALGO_ID_SIGNATURE_RSA,
                new X509CredentialImpl(ssoAgentCarbonX509Credential));
        return authnRequest;
    }

    /**
     * Generate an signed authentication request.
     *
     * @return Signed AuthnRequest Object
     * @throws Exception error when bootstrapping
     */
    public AuthnRequest buildSignedAuthRequest(String issuerId, int tenantId,
            String tenantDomain) throws Exception {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer(issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential =
                new SSOAgentCarbonX509Credential(tenantId, tenantDomain);
        setSignature(authnRequest, SignatureConstants.ALGO_ID_SIGNATURE_RSA,
                new X509CredentialImpl(ssoAgentCarbonX509Credential));
        return authnRequest;
    }
    
    
    /**
     * Generate an Signed authentication request with a custom consumer url.
     *
     * @return AuthnRequest Object
     * @throws Exception error when bootstrapping
     */

    public AuthnRequest buildSignedAuthRequestWithConsumerUrl(String issuerId, String destination, String consumerUrl,
            int tenantId,
            String tenantDomain) throws Exception {
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util.buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer(issuerId));
        authnRequest.setNameIDPolicy(buildNameIDPolicy());
        authnRequest.setAssertionConsumerServiceURL(consumerUrl);
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
     * Build the NameIDPolicy object
     *
     * @return NameIDPolicy object
     */
    private static NameIDPolicy buildNameIDPolicy() {
        NameIDPolicy nameIDPolicy = new NameIDPolicyBuilder().buildObject();
        nameIDPolicy.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
        nameIDPolicy.setAllowCreate(true);
        return nameIDPolicy;
    }

    /**
     * Sign the SAML AuthnRequest message
     *
     * @param authnRequest
     * @param signatureAlgorithm
     * @param cred
     * @return
     */
    public static AuthnRequest setSignature(AuthnRequest authnRequest, String signatureAlgorithm,
            X509Credential cred) throws Exception {
        try {
            Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            try {
                KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
                X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
                org.opensaml.xml.signature.X509Certificate cert =
                        (org.opensaml.xml.signature.X509Certificate) buildXMLObject(
                                org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
                String value = Base64.encodeBytes(cred.getEntityCertificate().getEncoded());
                cert.setValue(value);
                data.getX509Certificates().add(cert);
                keyInfo.getX509Datas().add(data);
                signature.setKeyInfo(keyInfo);
            } catch (CertificateEncodingException e) {
                throw new Exception("Error getting certificate", e);
            }

            authnRequest.setSignature(signature);

            List<Signature> signatureList = new ArrayList<Signature>();
            signatureList.add(signature);

            // Marshall and Sign
            MarshallerFactory marshallerFactory =
                    org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(authnRequest);

            marshaller.marshall(authnRequest);
            
            Signer.signObjects(signatureList);
            return authnRequest;

        } catch (Exception e) {
            throw new Exception("Error while signing the SAML Request message", e);
        }
    }
    

    /**
     * Sign the SAML AuthnRequest message
     *
     * @param logoutRequest
     * @param signatureAlgorithm
     * @param cred
     * @return
     */
    
    
    public static LogoutRequest setSignature(LogoutRequest logoutRequest, String signatureAlgorithm,
            X509Credential cred) throws Exception {
        try {
            Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(cred);
            signature.setSignatureAlgorithm(signatureAlgorithm);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            try {
                KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
                X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
                org.opensaml.xml.signature.X509Certificate cert =
                        (org.opensaml.xml.signature.X509Certificate) buildXMLObject(
                                org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
                String value = Base64.encodeBytes(cred.getEntityCertificate().getEncoded());
                cert.setValue(value);
                data.getX509Certificates().add(cert);
                keyInfo.getX509Datas().add(data);
                signature.setKeyInfo(keyInfo);
            } catch (CertificateEncodingException e) {
                throw new SecurityException("Error getting certificate", e);
            }

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

        } catch (Exception e) {
            throw new Exception("Error while signing the Logout Request message", e);
        }
    }
    

    /**
     * Builds SAML Elements
     *
     * @param objectQName
     * @return
     */
    private static XMLObject buildXMLObject(QName objectQName) throws Exception {
        XMLObjectBuilder builder =
                org.opensaml.xml.Configuration.getBuilderFactory()
                        .getBuilder(objectQName);
        if (builder == null) {
            throw new Exception("Unable to retrieve builder for object QName " +
                    objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                objectQName.getPrefix());
    }

    /**
     * Replaces the ${} in url with system properties and returns
     *
     * @param acsUrl
     * @return
     */
    private String processAcsUrl(String acsUrl){
        //matches shortest segments that are between '{' and '}'
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(acsUrl);
        while (matcher.find()) {
            String match = matcher.group(1);
            String property = System.getProperty(match);
            if (property != null) {
                acsUrl = acsUrl.replace("${" + match + "}", property);
            } else {
                log.warn("System Property " + match + " is not set");
            }
        }
        return acsUrl;
    }
    
}
