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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.cleanup.service.ApiPurge;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.cleanup.service.OrganizationPurgeDAO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, APIPersistence.class, OrganizationPurgeDAO.class,
        GatewayArtifactsMgtDAO.class, APIUtil.class })
public class ApiPurgeTest {

    private OrganizationPurgeDAO organizationPurgeDAO;
    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    private ServiceReferenceHolder serviceReferenceHolder;
    private APIPersistence apiPersistenceInstance;

    @Before public void init() {
        organizationPurgeDAO = Mockito.mock(OrganizationPurgeDAO.class);
        gatewayArtifactsMgtDAO = Mockito.mock(GatewayArtifactsMgtDAO.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiPersistenceInstance = Mockito.mock(APIPersistence.class);
    }

    @Test public void testOrganizationRemoval() throws APIManagementException, APIPersistenceException {

        PowerMockito.mockStatic(OrganizationPurgeDAO.class);
        PowerMockito.when(OrganizationPurgeDAO.getInstance()).thenReturn(organizationPurgeDAO);

        PowerMockito.mockStatic(GatewayArtifactsMgtDAO.class);
        PowerMockito.when(GatewayArtifactsMgtDAO.getInstance()).thenReturn(gatewayArtifactsMgtDAO);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).
                thenReturn(apiManagerConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.isAllowDisplayAPIsWithMultipleStatus()).thenReturn(true);

        APIIdentifier api = Mockito.mock(APIIdentifier.class);
        ArrayList<APIIdentifier> apiIdentifierList = new ArrayList<>();
        apiIdentifierList.add(api);

        Mockito.doReturn(apiIdentifierList).when(organizationPurgeDAO).getAPIIdList("testOrg");
        Mockito.doNothing().when(organizationPurgeDAO).deleteOrganizationAPIList(Mockito.any());
        Mockito.doNothing().when(gatewayArtifactsMgtDAO).removeOrganizationGatewayArtifacts(Mockito.any());
        Mockito.doNothing().when(apiPersistenceInstance).deleteAllAPIs(any(Organization.class));
        Mockito.doReturn(true).when(organizationPurgeDAO).apiOrganizationExist(Mockito.anyString());

        ApiPurge apiPurge = new ApiPurgeWrapper(apiPersistenceInstance);

        LinkedHashMap<String, String> subtaskResult =   apiPurge.purge("testOrg");
        for(Map.Entry<String, String> entry : subtaskResult.entrySet()) {
            Assert.assertEquals(entry.getKey() + " is not successful",
                    APIConstants.OrganizationDeletion.COMPLETED, entry.getValue());
        }

        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).getAPIIdList("testOrg");
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).deleteOrganizationAPIList(Mockito.any());
        Mockito.verify(gatewayArtifactsMgtDAO, Mockito.times(1)).
                removeOrganizationGatewayArtifacts(Mockito.any());
    }
}