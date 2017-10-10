/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.neethi.Policy;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleDataHolder;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;
import org.apache.synapse.util.SynapseBinaryDataSource;
import org.compass.core.util.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.wso2.carbon.h2.osgi.utils.CarbonConstants.CARBON_HOME;

/**
 * ApplicationThrottleController test cases
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, RegistryServiceHolder.class, XMLInputFactory.class, ThrottleFactory
        .class, OMAbstractFactory.class})
public class ApplicationThrottleControllerTest {

    private MessageContext messageContext;
    private ThrottleDataHolder throttleDataHolder;
    private String applicationId;
    private PrivilegedCarbonContext carbonContext;
    private RegistryServiceHolder registryServiceHolder;
    private RegistryService registryService;
    private UserRegistry registry;
    private RealmService realmService;
    private TenantManager tenantManager;
    private Resource throttlingPolicyResource;
    private ThrottleContext throttleContext;
    private String tenantDomain;
    private int tenantID;
    private static final String RESOURCE_PATH = "/apimgt/applicationdata/tiers.xml";
    private static final String THROTTLE_POLICY_KEY = "gov:" + RESOURCE_PATH;
    private String THROTTLING_POLICY_DEFINITION =
            "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
                    "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
                    "    <throttle:MediatorThrottleAssertion>\n" +
                    "        <wsp:Policy>\n" +
                    "            <throttle:ID throttle:type=\"ROLE\">Gold</throttle:ID>\n" +
                    "            <wsp:Policy>\n" +
                    "                <throttle:Control>\n" +
                    "                    <wsp:Policy>\n" +
                    "                        <throttle:MaximumCount>20</throttle:MaximumCount>\n" +
                    "                        <throttle:UnitTime>60000</throttle:UnitTime>\n" +
                    "                        <wsp:Policy>\n" +
                    "                            <throttle:Attributes>\n" +
                    "                                " +
                    "<throttle:x-wso2-BillingPlan>FREE</throttle:x-wso2-BillingPlan>\n" +
                    "                                " +
                    "<throttle:x-wso2-StopOnQuotaReach>true</throttle:x-wso2-StopOnQuotaReach>\n" +
                    "                            </throttle:Attributes>\n" +
                    "                        </wsp:Policy>\n" +
                    "                    </wsp:Policy>\n" +
                    "                </throttle:Control>\n" +
                    "            </wsp:Policy>\n" +
                    "        </wsp:Policy>\n" +
                    "    </throttle:MediatorThrottleAssertion>\n" +
                    "</wsp:Policy>\n";

    @Before
    public void init() throws RegistryException, UserStoreException {
        System.setProperty(CARBON_HOME, "");
        tenantDomain = "carbon.super";
        applicationId = "1";
        tenantID = tenantID;
        messageContext = TestUtils.getMessageContextWithAuthContext("api", "v1");
        registryServiceHolder = Mockito.mock(RegistryServiceHolder.class);
        registryService = Mockito.mock(RegistryService.class);
        registry = Mockito.mock(UserRegistry.class);
        realmService = Mockito.mock(RealmService.class);
        tenantManager = Mockito.mock(TenantManager.class);
        carbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        throttlingPolicyResource = Mockito.mock(Resource.class);
        throttleContext = Mockito.mock(ThrottleContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(RegistryServiceHolder.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        PowerMockito.when(RegistryServiceHolder.getInstance()).thenReturn(registryServiceHolder);
        PowerMockito.when(registryServiceHolder.getRegistryService()).thenReturn(registryService);
        PowerMockito.when(carbonContext.getOSGiService(RealmService.class, null)).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
    }

    @Test
    public void testCreatingThrottleContextWhenThrottlingPolicyResourceContextIsString() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION);
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test
    public void testReturnThrottleContextIfItIsAlreadyAvailable() throws UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(throttleContext);
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test
    public void testCreatingThrottleContextWhenThrottlingPolicyResourceContextIsAByteStream() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION.getBytes());
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhileRetrievingTenantID() throws UserStoreException,
            RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.doThrow(new UserStoreException()).when(tenantManager).getTenantId(tenantDomain);
        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder, applicationId,
                THROTTLE_POLICY_KEY);
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhileRetrievingTenantRegistry() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.doThrow(new RegistryException("Error while retrieving registry")).when(registryService)
                .getGovernanceSystemRegistry(Mockito.anyInt());
        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder, applicationId,
                THROTTLE_POLICY_KEY);
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyResourceDoesNotExists() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(false);
        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder, applicationId,
                THROTTLE_POLICY_KEY);
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyResourceRetrievalFailed() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.doThrow(new RegistryException("Error while fetching resource ")).when(registry).get(RESOURCE_PATH);
        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder, applicationId,
                THROTTLE_POLICY_KEY);
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyResourceContentRetrievalFailed()
            throws UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.doThrow(new RegistryException("Error while retrieving resource content")).when
                (throttlingPolicyResource).getContent();
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyFailedDueToXMLStreamException()
            throws UserStoreException, RegistryException, XMLStreamException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION.getBytes());
        PowerMockito.mockStatic(XMLInputFactory.class);
        XMLInputFactory factory = Mockito.mock(XMLInputFactory.class);
        PowerMockito.when(XMLInputFactory.newInstance()).thenReturn(factory);
        PowerMockito.doThrow(new XMLStreamException()).when(factory).createXMLStreamReader((ByteArrayInputStream)
                Mockito.anyObject());
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyFailedDueToOMException()
            throws UserStoreException, RegistryException, XMLStreamException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION.getBytes());
        PowerMockito.mockStatic(XMLInputFactory.class);
        XMLInputFactory factory = Mockito.mock(XMLInputFactory.class);
        PowerMockito.when(XMLInputFactory.newInstance()).thenReturn(factory);
        PowerMockito.doThrow(new OMException()).when(factory).createXMLStreamReader((ByteArrayInputStream)
                Mockito.anyObject());
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyContentIsInValid() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(Mockito.anyInt());
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyResourceIsACollection() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        Collection collection = Mockito.mock(Collection.class);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(collection);
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenThrottlingPolicyKeyNotProvided() throws
            UserStoreException, RegistryException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, null));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenCreatingThrottlingMediatorFails() throws
            UserStoreException, RegistryException, ThrottleException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION);
        PowerMockito.mockStatic(ThrottleFactory.class);
        PowerMockito.when(ThrottleFactory.createMediatorThrottle((Policy) Mockito.anyObject())).thenThrow(new
                ThrottleException());
        Assert.notNull(ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY));
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhenNonXMLThrottlingPolicyContentTypeIsTextPlain()
            throws UserStoreException, RegistryException, XMLStreamException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn(THROTTLING_POLICY_DEFINITION.getBytes());
        PowerMockito.mockStatic(XMLInputFactory.class);
        XMLInputFactory factory = Mockito.mock(XMLInputFactory.class);
        PowerMockito.when(XMLInputFactory.newInstance()).thenReturn(factory);
        PowerMockito.doThrow(new XMLStreamException()).when(factory).createXMLStreamReader((ByteArrayInputStream)
                Mockito.anyObject());
        PowerMockito.when(throttlingPolicyResource.getMediaType()).thenReturn("text/plain");
        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY);
    }

    @Test(expected = SynapseException.class)
    public void testCreatingThrottleContextThrowsSynapseExceptionWhileProcessingNonXMLThrottlingPolicyContent()
            throws UserStoreException, RegistryException, XMLStreamException {
        Mockito.when(throttleDataHolder.getThrottleContext(applicationId)).thenReturn(null);
        PowerMockito.when(tenantManager.getTenantId(tenantDomain)).thenReturn(tenantID);
        PowerMockito.when(registryService.getGovernanceSystemRegistry(tenantID)).thenReturn(registry);
        PowerMockito.when(registry.resourceExists(RESOURCE_PATH)).thenReturn(true);
        PowerMockito.when(registry.get(RESOURCE_PATH)).thenReturn(throttlingPolicyResource);
        PowerMockito.when(throttlingPolicyResource.getContent()).thenReturn("\\xc3\\x28".getBytes());
        PowerMockito.mockStatic(XMLInputFactory.class);
        XMLInputFactory factory = Mockito.mock(XMLInputFactory.class);
        PowerMockito.when(XMLInputFactory.newInstance()).thenReturn(factory);
        PowerMockito.doThrow(new XMLStreamException()).when(factory).createXMLStreamReader((ByteArrayInputStream)
                Mockito.anyObject());
        PowerMockito.mockStatic(OMAbstractFactory.class);
        OMFactory omFactory = Mockito.mock(OMFactory.class);
        PowerMockito.when(OMAbstractFactory.getOMFactory()).thenReturn(omFactory);
        Mockito.doThrow(IOException.class).when(omFactory).createOMText((DataHandler) Mockito.anyObject(), Mockito
                .anyBoolean());


        ApplicationThrottleController.getApplicationThrottleContext(messageContext, throttleDataHolder,
                applicationId, THROTTLE_POLICY_KEY);
    }

}
