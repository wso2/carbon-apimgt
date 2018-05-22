package org.wso2.carbon.apimgt.hostobjects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.script.ScriptException;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class MutualAuthHostObjectTest {
    MutualAuthHostObject mutualAuthHostObject = new MutualAuthHostObject();
    @Test
    public void testGetClassName() throws Exception {
        Assert.assertTrue("MutualAuthHostObject".equals(mutualAuthHostObject.getClassName()));
    }

    @Test(expected = ScriptException.class)
    public void testJsFunction_validateUserNameHeader() throws Exception {
        Object args[] = {"user@test.com"};

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        RealmService realmService = Mockito.mock(org.wso2.carbon.user.core.service.RealmService.class);

        Mockito.when(serviceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        mutualAuthHostObject.jsFunction_validateUserNameHeader(null, null, args, null);

        //with existing username
        Mockito.when(userStoreManager.isExistingUser(Mockito.anyString())).thenReturn(true);

        mutualAuthHostObject.jsFunction_validateUserNameHeader(null, null, args, null);

        //invalid arguments
        Object argsInvalid1[] = {};
        mutualAuthHostObject.jsFunction_validateUserNameHeader(null, null, argsInvalid1, null);
    }

}