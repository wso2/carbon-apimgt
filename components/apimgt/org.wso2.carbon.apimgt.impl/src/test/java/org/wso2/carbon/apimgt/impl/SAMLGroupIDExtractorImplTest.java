/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.soap.wssecurity.impl.AttributedStringImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DocumentBuilderFactory.class, XMLObjectProviderRegistrySupport.class,
        ServiceReferenceHolder.class, PrivilegedCarbonContext.class, AuthenticatorsConfiguration.class })
public class SAMLGroupIDExtractorImplTest {

    private DocumentBuilder documentBuilder;
    private DocumentBuilderFactory documentBuilderFactory;
    private Document document;
    private Element element;
    private UnmarshallerFactory unmarshallerFactory;
    private Unmarshaller unmarshaller;

    @Before
    public void init() {
        PowerMockito.mockStatic(DocumentBuilderFactory.class);
        documentBuilder = Mockito.mock(DocumentBuilder.class);
        documentBuilderFactory = Mockito.mock(DocumentBuilderFactory.class);
        document = Mockito.mock(Document.class);
        element = Mockito.mock(Element.class);
        unmarshallerFactory = Mockito.mock(UnmarshallerFactory.class);
        unmarshaller = Mockito.mock(Unmarshaller.class);
    }

    @Test
    public void getGroupingIdentifiersTestCase() throws ParserConfigurationException, IOException, SAXException,
            UnmarshallingException, UserStoreException {

        SAMLGroupIDExtractorImpl samlGroupIDExtractor = new SAMLGroupIDExtractorImplWrapper();

        Mockito.when(DocumentBuilderFactory.newInstance()).thenReturn(documentBuilderFactory);
        Mockito.when(documentBuilderFactory.newDocumentBuilder()).thenReturn(documentBuilder);
        Mockito.when(documentBuilder.parse(samlGroupIDExtractor.getByteArrayInputStream("test"))).
                thenReturn(document);
        Mockito.when(document.getDocumentElement()).thenReturn(element);

        PowerMockito.mockStatic(XMLObjectProviderRegistrySupport.class);
        Response response = Mockito.mock(Response.class);
        List<Assertion> assertion = new ArrayList();
        Subject subject = Mockito.mock(Subject.class);
        NameID nameID = Mockito.mock(NameID.class);
        Assertion assertion1 = Mockito.mock(Assertion.class);
        assertion.add(assertion1);
        Mockito.when(XMLObjectProviderRegistrySupport.getUnmarshallerFactory()).thenReturn(unmarshallerFactory);
        Mockito.when(unmarshallerFactory.getUnmarshaller(element)).thenReturn(unmarshaller);
        Mockito.when(unmarshaller.unmarshall(element)).thenReturn(response);
        Mockito.when(response.getAssertions()).thenReturn(assertion);
        Mockito.when(assertion.get(0).getSubject()).thenReturn(subject);
        Mockito.when(subject.getNameID()).thenReturn(nameID);
        Mockito.when(nameID.getValue()).thenReturn("user");

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        APIManagerConfigurationService apiManagerConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfig = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigService);
        Mockito.when(apiManagerConfigService.getAPIManagerConfiguration()).thenReturn(apiManagerConfig);
        Mockito.when(apiManagerConfig.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI)).
                thenReturn("http://wso2.org/claims/organization");

        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(1234);
        Mockito.when(realmService.getTenantUserRealm(1234)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getUserClaimValue(MultitenantUtils.
                getTenantAwareUsername("user"), "http://wso2.org/claims/organization", null)).
                thenReturn("organization");

        Assert.assertEquals("carbon.super/organization",samlGroupIDExtractor.
                getGroupingIdentifiers("test"));
    }

    @Test
    public void getGroupingIdentifierListTestCase() throws ParserConfigurationException, IOException, SAXException,
            UnmarshallingException, UserStoreException {

        String claim = "http://wso2.org/claims/organization";
        String organizationValue = "organization";
        SAMLGroupIDExtractorImpl samlGroupIDExtractor = new SAMLGroupIDExtractorImplWrapper();
        Mockito.when(DocumentBuilderFactory.newInstance()).thenReturn(documentBuilderFactory);
        Mockito.when(documentBuilderFactory.newDocumentBuilder()).
                thenReturn(documentBuilder);
        Mockito.when(documentBuilder.parse(samlGroupIDExtractor.getByteArrayInputStream("test"))).
                thenReturn(document);
        Mockito.when(document.getDocumentElement()).thenReturn(element);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(XMLObjectProviderRegistrySupport.class);
        Response response = Mockito.mock(Response.class);
        List<Assertion> assertion = new ArrayList();
        Subject subject = Mockito.mock(Subject.class);
        NameID nameID = Mockito.mock(NameID.class);
        Assertion assertion1 = Mockito.mock(Assertion.class);
        assertion.add(assertion1);
        Mockito.when(XMLObjectProviderRegistrySupport.getUnmarshallerFactory()).thenReturn(unmarshallerFactory);
        Mockito.when(unmarshallerFactory.getUnmarshaller(element)).thenReturn(unmarshaller);
        Mockito.when(unmarshaller.unmarshall(element)).thenReturn(response);
        Mockito.when(response.getAssertions()).thenReturn(assertion);
        Mockito.when(assertion.get(0).getSubject()).thenReturn(subject);
        Mockito.when(subject.getNameID()).thenReturn(nameID);
        Mockito.when(nameID.getValue()).thenReturn("user");
        System.setProperty(APIConstants.READ_ORGANIZATION_FROM_SAML_ASSERTION, "true");
        APIManagerConfigurationService apiManagerConfigService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigService);
        APIManagerConfiguration apiManagerConfig = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigService.getAPIManagerConfiguration()).thenReturn(apiManagerConfig);
        Mockito.when(apiManagerConfig.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI)).
                thenReturn("http://wso2.org/claims/organization");

        System.setProperty("carbon.home", "");
        PrivilegedCarbonContext carbonContext;
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);

        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()).thenReturn(-1234);
        PowerMockito.doNothing().when(carbonContext).setTenantDomain("carbon.super", true);

        AttributeStatement mockAttributeStatement = PowerMockito.mock(AttributeStatement.class);
        List<AttributeStatement> attributeStatementList = Collections.singletonList(mockAttributeStatement);
        PowerMockito.when(assertion1.getAttributeStatements()).thenReturn(attributeStatementList);

        Attribute mockAttribute = PowerMockito.mock(Attribute.class);
        List<Attribute> attributesList = Collections.singletonList(mockAttribute);
        PowerMockito.when(mockAttributeStatement.getAttributes()).thenReturn(attributesList);

        XMLObject rawAttribute = PowerMockito.mock(XMLObject.class);
        PowerMockito.when(rawAttribute.toString()).thenReturn(organizationValue);
        List<XMLObject> mockedAttributeValues = Collections.singletonList(rawAttribute);
        AttributedStringImpl mockedAttributedStringImpl = new AttributedStringImpl("nameSpaceURI", "elementLocalName",
                "namespacePrefix");
        String sampleAttrValue = "MockedAuthParamSampleAttribute";
        mockedAttributedStringImpl.setValue(sampleAttrValue);
        List<XMLObject> mockedXSSAttributeValues = Collections.singletonList((XMLObject) mockedAttributedStringImpl);
        XSAnyImpl mockedXSAnyImpl = Mockito.mock(XSAnyImpl.class);
        PowerMockito.when(mockedXSAnyImpl.getTextContent()).thenReturn(sampleAttrValue);
        List<XMLObject> mockedXSAnyImplAttributeValues = Collections.singletonList((XMLObject) mockedXSAnyImpl);
        List<XMLObject> multiMockedAttributeValues = Arrays.asList(rawAttribute, PowerMockito.mock(XMLObject.class));
        AuthenticatorsConfiguration.AuthenticatorConfig mockedAuthenticatorConfig = Mockito
                .mock(AuthenticatorsConfiguration.AuthenticatorConfig.class);
        PowerMockito.when(mockAttribute.getAttributeValues())
                .thenReturn(mockedAttributeValues, multiMockedAttributeValues, mockedXSSAttributeValues,
                        mockedXSAnyImplAttributeValues);

        PowerMockito.mockStatic(AuthenticatorsConfiguration.class);
        AuthenticatorsConfiguration mockedAuthenticatorsConfiguration = PowerMockito
                .mock(AuthenticatorsConfiguration.class);
        PowerMockito.when(AuthenticatorsConfiguration.getInstance()).thenReturn(mockedAuthenticatorsConfiguration);
        Map<String, String> mockedConfigParameters = new HashMap<String, String>();
        mockedConfigParameters.put(APIConstants.ORGANIZATION_CLAIM_ATTRIBUTE, claim);
        PowerMockito.when(mockedAuthenticatorConfig.getParameters()).thenReturn(mockedConfigParameters);
        PowerMockito.when(mockedAuthenticatorsConfiguration
                .getAuthenticatorConfig(APIConstants.SAML2_SSO_AUTHENTICATOR_NAME))
                .thenReturn(mockedAuthenticatorConfig);
        PowerMockito.when(mockAttribute.getName()).thenReturn(claim);

        String[] organizations = samlGroupIDExtractor.
                getGroupingIdentifierList("test");
        Assert.assertEquals(organizationValue, organizations[0]);
    }
}
