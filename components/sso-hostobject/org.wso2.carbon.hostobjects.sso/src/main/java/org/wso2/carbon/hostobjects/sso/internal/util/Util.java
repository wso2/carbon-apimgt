
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

package org.wso2.carbon.hostobjects.sso.internal.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.config.InitializationException;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureValidationProvider;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.hostobjects.sso.SignatureVerificationException;
import org.wso2.carbon.hostobjects.sso.SignatureVerificationFailure;
import org.wso2.carbon.hostobjects.sso.exception.SSOHostObjectException;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.identity.saml.common.util.SAMLInitializer;
import org.xml.sax.SAXException;

import javax.crypto.SecretKey;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Util {

    private static boolean bootStrapped = false;

    private static SecureRandom random = new SecureRandom();

    private static RealmService realmService = null;

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    private static final char[] charMapping = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p'};

    private static Log log = LogFactory.getLog(Util.class);

    /**
     * This method is used to initialize the OpenSAML3 library. It calls the bootstrap method, if it
     * is not initialized yet.
     */
    public static void doBootstrap() {
        if (!bootStrapped) {
            try {
                SAMLInitializer.doBootstrap();
                bootStrapped = true;
            } catch (InitializationException e) {
                System.err.println("Error in bootstrapping the OpenSAML3 library");
                log.error("Error in bootstrapping the OpenSAML3 library", e);
            }
        }
    }

    /**
     * Builds an xml object with the given QName
     *
     * @param objectQName QName object
     * @return built XML object
     * @throws SSOHostObjectException
     */
    public static XMLObject buildXMLObject(QName objectQName)
            throws SSOHostObjectException {

        XMLObjectBuilder builder = XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(objectQName);
        if (builder == null) {
            throw new SSOHostObjectException("Unable to retrieve builder for object QName "
                    + objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(),
                objectQName.getPrefix());
    }


    /**
     * Generates a unique Id for Authentication Requests
     *
     * @return generated unique ID
     */
    public static String createID() {

        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);

        char[] chars = new char[40];

        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = charMapping[left];
            chars[i * 2 + 1] = charMapping[right];
        }

        return String.valueOf(chars);
    }

    /**
     * Constructing the XMLObject Object from a String
     *
     * @param authReqStr
     * @return Corresponding XMLObject which is a SAML2 object
     * @throws Exception
     */
    public static XMLObject unmarshall(String authReqStr) throws Exception {
        try {
            doBootstrap();
            DocumentBuilderFactory documentBuilderFactory = getSecuredDocumentBuilder();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setIgnoringComments(true);
            Document document = getDocument(documentBuilderFactory, authReqStr);
            if (isSignedWithComments(document)) {
                documentBuilderFactory.setIgnoringComments(false);
                document = getDocument(documentBuilderFactory, authReqStr);
            }
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            throw new Exception("Error in constructing AuthRequest from " +
                    "the encoded String ", e);
        }
    }

    /**
     * Serializing a SAML2 object into a String
     *
     * @param xmlObject object that needs to serialized.
     * @return serialized object
     * @throws Exception
     */
    public static String marshall(XMLObject xmlObject) throws Exception {
        try {
            doBootstrap();
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            ByteArrayOutputStream byteArrayOutputStrm = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl =
                    (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStrm);
            writer.write(element, output);
            return byteArrayOutputStrm.toString();
        } catch (Exception e) {
            throw new Exception("Error Serializing the SAML Response", e);
        }
    }

    /**
     * Compressing and Encoding the response
     *
     * @param xmlString String to be encoded
     * @return compressed and encoded String
     */
    public static String encode(String xmlString) throws Exception {

        // Encoding the compressed message
        String encodedRequestMessage = Base64Support.encode(xmlString.getBytes("UTF-8"), Base64Support.UNCHUNKED);
        return encodedRequestMessage.trim();
    }

    /**
     * @param xmlString String to be encoded
     * @return
     */
    public static String deflateAndEncode(String xmlString) throws Exception {
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(
                byteArrayOutputStream, deflater);

        deflaterOutputStream.write(xmlString.getBytes());
        deflaterOutputStream.close();

        // Encoding the compressed message
        String encodedRequestMessage = Base64Support.encode(byteArrayOutputStream
                .toByteArray(), Base64Support.UNCHUNKED);
        return encodedRequestMessage.trim();

    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param encodedStr encoded AuthReq
     * @return decoded AuthReq
     */
    public static String decode(String encodedStr) throws Exception {
        return new String(Base64Support.decode(encodedStr));
    }

    /**
     * This method validates the signature of the SAML Response.
     *
     * @param signature Signature to verify
     * @return true, if signature is valid.
     */
    public static boolean validateSignature(Signature signature, String keyStoreName, String keyStorePassword,
            String alias, int tenantId, String tenantDomain)
            throws SignatureVerificationException, SignatureVerificationFailure {
        boolean isSigValid = false;
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            KeyStore keyStore = null;
            java.security.cert.X509Certificate cert = null;
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                // get an instance of the corresponding Key Store Manager instance
                KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
                keyStore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(tenantDomain));
                cert = (java.security.cert.X509Certificate) keyStore.getCertificate(tenantDomain);
            } else {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(new File(keyStoreName)), keyStorePassword.toCharArray());
                cert = (java.security.cert.X509Certificate) keyStore.getCertificate(alias);
            }
            if (log.isDebugEnabled()) {
                log.debug("Validating against " + cert.getSubjectDN().getName());
            }
            try {
                SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
                signatureProfileValidator.validate(signature);
            } catch (SignatureException e) {
                String logMsg = "The signature do not confirm to SAML signature profile. Possible XML Signature Wrapping Attack!";
                if (log.isDebugEnabled()) {
                    log.debug(logMsg, e);
                }
                log.error(e.getMessage(), e);
                //Returning false,without throwing the exception as to propagate 401 to UI
                return false;
            }
            X509CredentialImpl credentialImpl = new X509CredentialImpl(cert);

            thread.setContextClassLoader(SignatureValidationProvider.class.getClassLoader());
            SignatureValidator.validate(signature, credentialImpl);
            isSigValid = true;
            return isSigValid;

        } catch (KeyStoreException e) {
            log.error("Error when getting certificate of tenant " + tenantDomain, e);
            throw new SignatureVerificationException(e);
        } catch (CertificateException e) {
            log.error("Could not load the keystore " + keyStoreName, e);
            throw new SignatureVerificationException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Could not load the keystore " + keyStoreName, e);
            throw new SignatureVerificationException(e);
        } catch (FileNotFoundException e) {
            log.error("Could not find the key store file " + keyStoreName, e);
            throw new SignatureVerificationException(e);
        } catch (IOException e) {
            log.error("Could not load the keystore " + keyStoreName, e);
            throw new SignatureVerificationException(e);
        } catch (SignatureException e) {
            String logMsg = "The signature do not confirm to SAML signature profile. Possible XML Signature Wrapping Attack!";
            if (log.isDebugEnabled()) {
                log.debug(logMsg, e);
            }
            //Do not log the exception here. Clients of this method use it in a fall back fashion to verify signatures
            //using different public keys. Therefore logging an error would cause unnecessary logs. Throwing an
            //exception is sufficient so that clients can decide what to do with it.
            throw new SignatureVerificationFailure(e);
        } catch (Exception e) {
            //keyStoreManager.getKeyStore throws a generic 'Exception'
            log.error("Error when getting key store of tenant " + tenantDomain, e);
            throw new SignatureVerificationException(e.getMessage(), e);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    public static Assertion getDecryptedAssertion(EncryptedAssertion encryptedAssertion, String keyStoreName,
                                                  String keyStorePassword, String alias, int tenantId, String tenantDomain) throws Exception {

        try {
            KeyStore keyStore = null;

            java.security.cert.X509Certificate cert = null;

            SSOAgentCarbonX509Credential ssoAgentCarbonX509Credential = new SSOAgentCarbonX509Credential(tenantId,
                    tenantDomain);

            KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(
                    new X509CredentialImpl(ssoAgentCarbonX509Credential));

            EncryptedKey key = encryptedAssertion.getEncryptedData().
                    getKeyInfo().getEncryptedKeys().get(0);
            Decrypter decrypter = new Decrypter(null, keyResolver, null);
            SecretKey dkey = (SecretKey) decrypter.decryptKey(key, encryptedAssertion.getEncryptedData().
                    getEncryptionMethod().getAlgorithm());
            Credential shared = CredentialSupport.getSimpleCredential(dkey);
            decrypter = new Decrypter(new StaticKeyInfoCredentialResolver(shared), null, null);
            decrypter.setRootInNewDocument(true);
            return decrypter.decrypt(encryptedAssertion);
        } catch (Exception e) {
            throw new Exception("Decrypted assertion error", e);

        }
    }


    public static String getDomainName(XMLObject samlObject) {
        NodeList list = samlObject.getDOM().getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "NameID");
        String domainName = null;
        if (list.getLength() > 0) {
            String userName = list.item(0).getTextContent();
            domainName = MultitenantUtils.getTenantDomain(userName);
        }
        return domainName;
    }

    /**
     * Generate the key store name from the domain name
     *
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    private static String generateKSNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }


    public static void setRealmService(RealmService realmService) {
        Util.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return Util.realmService;
    }

    /**
     * Build NameIDPolicy object given name ID policy format
     *
     * @param nameIdPolicy Name ID policy format
     * @return SAML NameIDPolicy object
     */
    public static NameIDPolicy buildNameIDPolicy(String nameIdPolicy) {
        NameIDPolicy nameIDPolicyObj = new NameIDPolicyBuilder().buildObject();
        if (!StringUtils.isEmpty(nameIdPolicy)) {
            nameIDPolicyObj.setFormat(nameIdPolicy);
        } else {
            nameIDPolicyObj.setFormat(SSOConstants.NAME_ID_POLICY_DEFAULT);
        }
        nameIDPolicyObj.setAllowCreate(true);
        return nameIDPolicyObj;
    }

    /**
     * Build NameID object given name ID format
     *
     * @param nameIdFormat Name ID format
     * @param subject      Subject
     * @return SAML NameID object
     */
    public static NameID buildNameID(String nameIdFormat, String subject) {
        NameID nameIdObj = new NameIDBuilder().buildObject();
        if (!StringUtils.isEmpty(nameIdFormat)) {
            nameIdObj.setFormat(nameIdFormat);
        } else {
            nameIdObj.setFormat(SSOConstants.NAME_ID_POLICY_DEFAULT);
        }
        nameIdObj.setValue(subject);
        return nameIdObj;
    }

    /**
     * Replaces the ${} in url with system properties and returns
     *
     * @param acsUrl assertion consumer service url
     * @return acsUrl with system properties replaced
     */
    public static String processAcsUrl(String acsUrl) {
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

    /**
     * Returns a secured DocumentBuilderFactory instance
     *
     * @return DocumentBuilderFactory
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * Get the username from the SAML2 Assertion
     *
     * @param assertion SAML2 assertion
     * @return username
     */
    public static String getUsernameFromAssertion(Assertion assertion, String usernameAttribute) {
        String username = null;
        if (!StringUtils.isEmpty(usernameAttribute)) {
            // There can be multiple AttributeStatements in Assertion
            List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
            if (attributeStatements != null) {
                for (AttributeStatement attributeStatement : attributeStatements) {
                    // There can be multiple Attributes in an attributeStatement
                    List<Attribute> attributes = attributeStatement.getAttributes();
                    if (attributes != null) {
                        for (Attribute attribute : attributes) {
                            String attributeName = attribute.getDOM().getAttribute(SSOConstants.SAML_NAME_ATTRIBUTE);
                            if (attributeName.equals(usernameAttribute)) {
                                List<XMLObject> attributeValues = attribute.getAttributeValues();
                                // There can be multiple attribute values in an attribute, but get the first one
                                username = attributeValues.get(0).getDOM().getTextContent();
                                if (log.isDebugEnabled()) {
                                    log.debug("Name of authenticated user from SAML response : " + username);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Subject subject = assertion.getSubject();
            if (subject != null) {
                if (subject.getNameID() != null) {
                    username = subject.getNameID().getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("Name of authenticated user from SAML response : " + username);
                    }
                }
            }
        }
        return username;
    }

    /**
     * Return whether SAML Assertion has the canonicalization method
     * set to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'.
     *
     * @param document
     * @return true if canonicalization method equals to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'
     */
    private static boolean isSignedWithComments(Document document) {

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            String assertionId = (String) xPath.compile("//*[local-name()='Assertion']/@ID")
                    .evaluate(document, XPathConstants.STRING);

            if (StringUtils.isBlank(assertionId)) {
                return false;
            }

            NodeList nodeList = ((NodeList) xPath.compile(
                    "//*[local-name()='Assertion']" + "/*[local-name()='Signature']" + "/*[local-name()='SignedInfo']"
                            + "/*[local-name()='Reference'][@URI='#" + assertionId + "']"
                            + "/*[local-name()='Transforms']" + "/*[local-name()='Transform']"
                            + "[@Algorithm='http://www.w3.org/2001/10/xml-exc-c14n#WithComments']")
                    .evaluate(document, XPathConstants.NODESET));
            return nodeList != null && nodeList.getLength() > 0;
        } catch (XPathExpressionException e) {
            String message = "Failed to find the canonicalization algorithm of the assertion. Defaulting to: "
                    + "http://www.w3.org/2001/10/xml-exc-c14n#";
            log.warn(message);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            return false;
        }
    }

    private static Document getDocument(DocumentBuilderFactory documentBuilderFactory, String samlString)
            throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(samlString.getBytes());
        return docBuilder.parse(inputStream);
    }
}
