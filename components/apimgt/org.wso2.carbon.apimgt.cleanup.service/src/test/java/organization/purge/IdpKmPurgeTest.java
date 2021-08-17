/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package organization.purge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.cleanup.service.IdpKeyMangerPurge;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, APIAdmin.class, ApiMgtDAO.class,
        GatewayArtifactsMgtDAO.class, APIUtil.class })
public class IdpKmPurgeTest {

    private ApiMgtDAO apiMgtDAO;
    private APIAdminImpl amAdmin;

    @Before public void init() {
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        amAdmin = Mockito.mock(APIAdminImpl.class);
    }

    @Test public void testOrganizationRemoval() throws APIManagementException, UserStoreException {

        PowerMockito.mockStatic(ApiMgtDAO.class);
        PowerMockito.mockStatic(APIUtil.class);

        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        KeyManagerConfigurationDTO kmConfig = Mockito.mock(KeyManagerConfigurationDTO.class);
        List<KeyManagerConfigurationDTO> keyManagerList = new ArrayList<>();
        keyManagerList.add(kmConfig);

        Mockito.doReturn(keyManagerList).when(amAdmin).getKeyManagerConfigurationsByOrganization("testOrg");
        Mockito.doNothing().when(apiMgtDAO).deleteKeyManagerConfigurationList(keyManagerList, "testOrg");
        Mockito.doNothing().when(amAdmin).deleteIdentityProvider("testOrg", kmConfig);

        Mockito.when(APIUtil.isInternalOrganization("testOrg")).thenReturn(true);
        IdpKeyMangerPurge kmPurge = new IdpKeyMangerPurge("test-username");
        LinkedHashMap<String, String> subtaskResult =  kmPurge.deleteOrganization("testOrg");

        for(Map.Entry<String, String> entry : subtaskResult.entrySet()) {
            Assert.assertEquals(entry.getKey() + " is not successful",
                    APIConstants.OrganizationDeletion.COMPLETED, entry.getValue());
        }

        Mockito.verify(apiMgtDAO, Mockito.times(1)).
                deleteKeyManagerConfigurationList(Matchers.anyListOf(KeyManagerConfigurationDTO.class),
                        Mockito.any());
    }
}
