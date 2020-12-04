package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.wso2.carbon.apimgt.impl.token.ClaimsRetriever.DEFAULT_DIALECT_URI;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {LogFactory.class, ServiceReferenceHolder.class,
          MultitenantUtils.class, APIUtil.class})
@PowerMockIgnore("javax.net.ssl.*")
public class APIRealmUtilTest {

    @Test
    public void testLoggedInUserClaims() throws APIManagementException, UserStoreException {
        String configuredClaims = "http://wso2.org/claim1,http://wso2.org/claim2,http://wso2.org/claim3";
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        ServiceReferenceHolder serviceReferenceHolder = TestUtils.getServiceReferenceHolder();
        RealmService realmService = Mockito.mock(RealmService.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        PowerMockito.when(tenantManager.getTenantId(Matchers.anyString())).thenReturn(-1234);

        SortedMap<String, String> claimValues = new TreeMap<String, String>();
        claimValues.put("claim1", "http://wso2.org/claim1");
        claimValues.put("claim2", "http://wso2.org/claim2");
        claimValues.put("claim3", "http://wso2.org/claim3");
        PowerMockito.when(APIUtil.getTenantId("carbon.super")).thenReturn(-1234);

        PowerMockito.when(APIUtil.getClaims("admin", -1234, DEFAULT_DIALECT_URI))
                .thenReturn(claimValues);
        Map loggedInUserClaims = APIRealmUtils.getLoggedInUserClaims("admin");
        assertNotNull(loggedInUserClaims);
        assertEquals(configuredClaims.split(",").length, loggedInUserClaims.size());
    }
}
