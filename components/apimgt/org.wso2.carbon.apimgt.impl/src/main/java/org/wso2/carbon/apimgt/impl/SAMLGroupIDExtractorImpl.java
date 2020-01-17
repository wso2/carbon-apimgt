/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.api.NewPostLoginExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SAMLGroupIDExtractorImpl implements NewPostLoginExecutor {

    private static final Log log = LogFactory.getLog(SAMLGroupIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse) {
        if (log.isDebugEnabled()) {
            log.debug("Login response " + loginResponse);
        }
        ByteArrayInputStream samlResponseStream = null;
        DocumentBuilder docBuilder;
        String username = "";
        String organization = "";
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
            if (StringUtils.isBlank(claim)) {
                claim = "http://wso2.org/claims/organization";
            }
            samlResponseStream = getByteArrayInputStream(loginResponse);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setNamespaceAware(true);
            docBuilder = builderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(samlResponseStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            Response response = (Response) unmarshaller.unmarshall(element);
            List<Assertion> assertions = response.getAssertions();
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            UserStoreManager manager = realm.getUserStoreManager();
            organization =
                    manager.getUserClaimValue(MultitenantUtils.getTenantAwareUsername(username), claim, null);
            if (log.isDebugEnabled()) {
                log.debug("User organization " + organization);
            }
            if (organization != null) {
                organization = tenantDomain + "/" + organization.trim();
            }
        } catch (ParserConfigurationException e) {
            String msg = "Error while parsing SAML Assertion";
            log.error(msg, e);
        } catch (UnmarshallingException e) {
            String msg = "Error while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (SAXException e) {
            String msg = "Parsing exception  occur while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (IOException e) {
            String msg = "IO exception happen while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (UserStoreException e) {
            log.error("User store exception occurred for user" + username, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while checking user existence for " + username, e);
        } finally {
            if (samlResponseStream != null) {
                try {
                    samlResponseStream.close();
                } catch (IOException e) {
                    //Ignore
                    log.error("ERROR_CLOSING_STREAM");
                }
            }
        }
        return organization;
    }

    /**
     * Get the organization claim from authenticators configuration
     *
     * @return OrganizationClaimAttribute value configured in authenticators.xml
     */
    private String getOrganizationClaim() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(APIConstants.SAML2_SSO_AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            Map<String, String> configParameters = authenticatorConfig.getParameters();
            if (configParameters.containsKey(APIConstants.ORGANIZATION_CLAIM_ATTRIBUTE)) {
                return configParameters.get(APIConstants.ORGANIZATION_CLAIM_ATTRIBUTE);
            }
        }
        return APIConstants.DEFAULT_ORGANIZATION_CLAIM_NAME;
    }

    /**
     * Get the organization list from the SAML2 Assertion
     *
     * @param assertions SAML2 assertions returned in SAML response
     * @return Organization list from the assertion
     */
    private String getOrganizationFromSamlAssertion(List<Assertion> assertions) {
        List<String> attributeValueArray = new ArrayList<>();
        String organizationAttributeName = getOrganizationClaim();

        for (Assertion assertion : assertions) {
            List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();
            if (attributeStatementList != null) {
                for (AttributeStatement statement : attributeStatementList) {
                    List<Attribute> attributesList = statement.getAttributes();
                    for (Attribute attribute : attributesList) {
                        String attributeName = attribute.getName();
                        if (organizationAttributeName.equals(attributeName)) {
                            List<XMLObject> attributeValues = attribute.getAttributeValues();
                            if (attributeValues != null) {
                                for (XMLObject attributeValue : attributeValues) {
                                    attributeValueArray.add(getAttributeValue(attributeValue));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Organization list found in assertion: " + attributeValueArray);
        }

        return String.join(",", attributeValueArray);
    }

    /**
     * Get the String value from XMLObject
     *
     * @param attributeValue XMLObject of attribute value recived in SAML Assertion     *
     * @return attribute value as a String
     */
    private String getAttributeValue(XMLObject attributeValue) {
        if (attributeValue == null){
            return null;
        } else if (attributeValue instanceof XSString){
            return getStringAttributeValue((XSString) attributeValue);
        } else if(attributeValue instanceof XSAnyImpl){
            return getAnyAttributeValue((XSAnyImpl) attributeValue);
        } else {
            return attributeValue.toString();
        }
    }

    /**
     * Get the String value from XSAnyImpl object
     *
     * @param attributeValue XSAnyImpl Object instance of attribute value received in SAML Assertion
     * @return attribute value as a String
     */
    private String getAnyAttributeValue(XSAnyImpl attributeValue) {
        return attributeValue.getTextContent();
    }

    /**
     * Get the String value from XSString object
     *
     * @param attributeValue XSString Object instance of attribute value received in SAML Assertion
     * @return attribute value as a String
     */
    private String getStringAttributeValue(XSString attributeValue) {
        return attributeValue.getValue();
    }

    protected ByteArrayInputStream getByteArrayInputStream(String loginResponse) {
        return new ByteArrayInputStream(loginResponse.getBytes());
    }

    @Override
    public String[] getGroupingIdentifierList(String loginResponse) {

        if (log.isDebugEnabled()) {
            log.debug("Login response " + loginResponse);
        }
        ByteArrayInputStream samlResponseStream = null;
        DocumentBuilder docBuilder;
        String username = "";
        String organization = "";

        String[] groupIdArray = null;

        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
            if (StringUtils.isBlank(claim)) {
                claim = "http://wso2.org/claims/organization";
            }
            samlResponseStream = getByteArrayInputStream(loginResponse);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setNamespaceAware(true);
            docBuilder = builderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(samlResponseStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            Response response = (Response) unmarshaller.unmarshall(element);
            List<Assertion> assertions = response.getAssertions();
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }
            String isSAML2Enabled = System.getProperty(APIConstants.READ_ORGANIZATION_FROM_SAML_ASSERTION);

            if (!StringUtils.isEmpty(isSAML2Enabled) && Boolean.parseBoolean(isSAML2Enabled)) {
                organization = getOrganizationFromSamlAssertion(assertions);
            } else {
                RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                String tenantDomain = MultitenantUtils.getTenantDomain(username);
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
                UserStoreManager manager = realm.getUserStoreManager();
                organization =
                        manager.getUserClaimValue(MultitenantUtils.getTenantAwareUsername(username), claim, null);
            }
            if (log.isDebugEnabled()) {
                log.debug("User organization " + organization);
            }
            if (organization != null) {
                if (organization.contains(",")) {
                    groupIdArray = organization.split(",");
                    for (int i = 0; i < groupIdArray.length; i++) {
                        groupIdArray[i] = groupIdArray[i].toString().trim();
                    }
                } else {
                    organization = organization.trim();
                    groupIdArray = new String[] {organization};
                }
            } else {
                // If claim is null then returning a empty string
                groupIdArray = new String[] {};
            }

        } catch (ParserConfigurationException e) {
            String msg = "Error while parsing SAML Assertion";
            log.error(msg, e);
        } catch (UnmarshallingException e) {
            String msg = "Error while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (SAXException e) {
            String msg = "Parsing exception  occur while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (IOException e) {
            String msg = "IO exception happen while unmarshalling the SAML Assertion";
            log.error(msg, e);
        } catch (UserStoreException e) {
            log.error("User store exception occurred for user" + username, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while checking user existence for " + username, e);
        } finally {
            if (samlResponseStream != null) {
                try {
                    samlResponseStream.close();
                } catch (IOException e) {
                    //Ignore
                    log.error("ERROR_CLOSING_STREAM");
                }
            }
        }

        return groupIdArray;
    }
}
