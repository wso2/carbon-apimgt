/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.wsdl;


import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIManagerComponent.class, ServiceReferenceHolder.class,
        MultitenantUtils.class, APIUtil.class, RegistryUtils.class})
public class SequenceGeneratorTest {
    private APIManagerConfiguration config;

    @Before
    public void setup() throws UserStoreException, RegistryException {
        config = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(config.getFirstProperty(APIConstants.VELOCITY_LOGGER)).thenReturn("VELOCITY");
        APIManagerConfigurationService apiManagerConfigurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(apiManagerConfigurationService);
        String templatePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" +
                File.separator + "resources" + File.separator;
        System.setProperty("carbon.home", templatePath);
        System.setProperty(APIConstants.SOAP_TO_REST_PRESERVE_ELEMENT_ORDER, "true");
    }

    @Test
    public void testGenerateSequencesFromSwaggerWithOrder() throws Exception {
        String[] names = new String[]{"userName", "credential", "claims", "profileName", "requirePasswordChange"};

        List<SOAPToRestSequence> sequenceList = SequenceGenerator.generateSequencesFromSwagger(getSwagger());
        OMElement element = extractParameterElements(sequenceList);
        int count = 0;
        Iterator it = element.getChildElements();
        while (it.hasNext()) {
            OMElement childElement = (OMElement) it.next();
            Assert.assertEquals("Parameter order mismatched", childElement.getLocalName(), names[count]);
            count += 1;
        }
    }

    public OMElement extractParameterElements(List<SOAPToRestSequence> sequenceList) throws Exception {
        String inSequence = "";
        if (sequenceList.get(0).getDirection().name() == "IN") {
            inSequence = sequenceList.get(0).getContent();
        } else if (sequenceList.get(1).getDirection().name() == "IN") {
            inSequence = sequenceList.get(1).getContent();
        }

        String start = "<web:addUser xmlns:web=\"http://service.ws.um.carbon.wso2.org\">";
        String end = "</web:addUser>";
        String extracted = inSequence.substring(inSequence.indexOf(start), inSequence.indexOf(end) + end.length());

        return APIUtil.buildOMElement(new ByteArrayInputStream(extracted.getBytes()));
    }


    public String getSwagger() {
        return "{ \"swagger\": \"2.0\", \"info\": { \"version\": \"\", \"title\": \"\" }, \"paths\": { \"/addUser\": " +
                "{ \"post\": { \"operationId\": \"addUser\", \"parameters\": [ { \"in\": \"body\", " +
                "\"name\": \"Payload\", \"required\": true, \"schema\": { \"$ref\": \"#/definitions/addUserInput\" } } ], " +
                "\"responses\": { \"default\": { \"description\": \"\"," +
                " \"schema\": { \"$ref\": \"#/definitions/addUserOutput\" } } }," +
                " \"x-wso2-soap\": { \"x-soap-style\": \"document\", \"soap-action\": \"urn:addUser\"," +
                " \"x-soap-message-type\": \"document\", \"soap-operation\": \"addUser\"," +
                " \"namespace\": \"http://service.ws.um.carbon.wso2.org\", \"x-soap-version\": \"1.2\" } } } }," +
                " \"definitions\": { \"addUserInput\": { \"type\": \"object\", \"properties\": { \"addUser\":" +
                " { \"$ref\": \"#/definitions/addUser\" } } }, \"addUserOutput\": { \"type\": \"object\" }," +
                " \"addUser\": { \"type\": \"object\", \"properties\": { \"userName\": { \"type\": \"string\" }, " +
                "\"credential\": { \"type\": \"string\" }, \"roleList\": { \"type\": \"array\", \"items\": " +
                "{ \"type\": \"string\" } }, \"claims\": { \"type\": \"array\", \"items\": " +
                "{ \"$ref\": \"#/definitions/ClaimValue\" } }, \"profileName\": { \"type\": \"string\" }," +
                " \"requirePasswordChange\": { \"type\": \"boolean\" } }, \"xml\": { \"namespace\":" +
                " \"http://www.w3.org/2001/XMLSchema\", \"prefix\": \"xs\" }, \"x-namespace-qualified\": true }," +
                " \"ClaimValue\": { \"type\": \"object\", \"properties\": { \"claimURI\": { \"type\": \"string\" }," +
                " \"value\": { \"type\": \"string\" } }, \"xml\": { \"namespace\": \"http://www.w3.org/2001/XMLSchema\"," +
                " \"prefix\": \"xs\" }, \"x-namespace-qualified\": true } } }";
    }
}
