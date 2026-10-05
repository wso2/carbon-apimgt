package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;
@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUtil.class, MultitenantUtils.class, PrivilegedCarbonContext.class, ServiceReferenceHolder.class,
        GovernanceUtils.class, PaginationContext.class, IOUtils.class, AXIOMUtil.class, RegistryUtils.class,
        AbstractAPIManager.class, OASParserUtil.class, KeyManagerHolder.class })
public class GlobalMediationPolicyImplTest {
    private Registry registry;
    public static final String SAMPLE_RESOURCE_ID = "xyz";
    @Test
    public void testGetAllGlobalMediationPolicies()
            throws RegistryException, APIManagementException, IOException, XMLStreamException {
        registry = Mockito.mock(Registry.class);
        GlobalMediationPolicyImpl globalMediationPolicyImplWrapper = new GlobalMediationPolicyImplWrapper(registry);
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/in";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl();
        resource.setUUID(SAMPLE_RESOURCE_ID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        try {
            globalMediationPolicyImplWrapper.getAllGlobalMediationPolicies();
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get global mediation policies"));
        }
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);

        List<Mediation> policies = globalMediationPolicyImplWrapper.getAllGlobalMediationPolicies();
        Assert.assertNotNull(policies);
        Assert.assertEquals(policies.size(), 1);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);
        globalMediationPolicyImplWrapper.getAllGlobalMediationPolicies(); // cover the logged only exceptions
        globalMediationPolicyImplWrapper.getAllGlobalMediationPolicies(); // cover the logged only exceptions
    }
    @Test
    public void testGetGlobalMediationPolicy()
            throws RegistryException, APIManagementException, XMLStreamException, IOException {
        registry = Mockito.mock(Registry.class);
        GlobalMediationPolicyImpl abstractAPIManager = new GlobalMediationPolicyImplWrapper(registry);
        String resourceUUID = SAMPLE_RESOURCE_ID;
        Collection parentCollection = new CollectionImpl();
        String mediationResourcePath = APIConstants.API_CUSTOM_SEQUENCE_LOCATION;
        String childCollectionPath = mediationResourcePath + "/testMediation";
        parentCollection.setChildren(new String[] { childCollectionPath });
        Mockito.when(registry.get(mediationResourcePath)).thenThrow(RegistryException.class).thenReturn(parentCollection);
        Collection childCollection = new CollectionImpl();
        String resourcePath = childCollectionPath + "/policy1";
        childCollection.setChildren(new String[] { resourcePath });
        Mockito.when(registry.get(childCollectionPath)).thenReturn(childCollection);
        Resource resource = new ResourceImpl(resourcePath, new ResourceDO());
        resource.setUUID(resourceUUID);

        Mockito.when(registry.get(resourcePath)).thenReturn(resource);
        try {
            abstractAPIManager.getGlobalMediationPolicy(resourceUUID);
            Assert.fail("Registry Exception not thrown for error scenario");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Error while accessing registry objects"));
        }
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // test for registry exception
        String mediationPolicyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"default-endpoint\">\n</sequence>";
        resource.setContent(mediationPolicyContent);
        Mediation policy = abstractAPIManager.getGlobalMediationPolicy(resourceUUID);
        Assert.assertNotNull(policy);
        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(AXIOMUtil.class);
        PowerMockito.when(IOUtils.toString((InputStream) Mockito.any(), Mockito.anyString()))
                .thenThrow(IOException.class).thenReturn(mediationPolicyContent);
        PowerMockito.when(AXIOMUtil.stringToOM(Mockito.anyString())).thenThrow(XMLStreamException.class);
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // cover the logged only exceptions
        abstractAPIManager.getGlobalMediationPolicy(resourceUUID); // cover the logged only exceptions

    }

}