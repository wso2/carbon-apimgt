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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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
import java.util.List;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DocumentBuilderFactory.class, Configuration.class, ServiceReferenceHolder.class})
public class SAMLGroupIDExtractorImplTest {

    @Test
    public void getGroupingIdentifiersTestCase() throws ParserConfigurationException, IOException, SAXException,
            UnmarshallingException, UserStoreException {

        SAMLGroupIDExtractorImpl samlGroupIDExtractor = new SAMLGroupIDExtractorImplWrapper();
        DocumentBuilderFactory documentBuilderFactory = Mockito.mock(DocumentBuilderFactory.class);
        PowerMockito.mockStatic(DocumentBuilderFactory.class);
        DocumentBuilder documentBuilder = Mockito.mock(DocumentBuilder.class);
        Document document = Mockito.mock(Document.class);

        Element element = Mockito.mock(Element.class);
        UnmarshallerFactory unmarshallerFactory = Mockito.mock(UnmarshallerFactory.class);
        Unmarshaller unmarshaller = Mockito.mock(Unmarshaller.class);

        Mockito.when(DocumentBuilderFactory.newInstance()).thenReturn(documentBuilderFactory);
        Mockito.when(documentBuilderFactory.newDocumentBuilder()).thenReturn(documentBuilder);
        Mockito.when(documentBuilder.parse(samlGroupIDExtractor.getByteArrayInputStream("test"))).
                thenReturn(document);
        Mockito.when(document.getDocumentElement()).thenReturn(element);

        PowerMockito.mockStatic(Configuration.class);
        Response response = Mockito.mock(Response.class);
        List<Assertion> assertion = new ArrayList();
        Subject subject = Mockito.mock(Subject.class);
        NameID nameID = Mockito.mock(NameID.class);
        Assertion assertion1 = Mockito.mock(Assertion.class);
        assertion.add(assertion1);
        Mockito.when(Configuration.getUnmarshallerFactory()).thenReturn(unmarshallerFactory);
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
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);

        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(1234);
        Mockito.when(realmService.getTenantUserRealm(1234)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getUserClaimValue(MultitenantUtils.
                getTenantAwareUsername("user"), "http://wso2.org/claims/organization", null)).
                thenReturn("organization");

        Assert.assertEquals("carbon.super/organization",samlGroupIDExtractor.
                getGroupingIdentifiers("test"));
    }
}
