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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.cleanup.service.ApplicationPurge;
import org.wso2.carbon.apimgt.cleanup.service.OrganizationPurgeDAO;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;

import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OrganizationPurgeDAO.class})
public class ApplicationPurgeTest {

    private OrganizationPurgeDAO organizationPurgeDAO;

    @Before
    public void init() {
        organizationPurgeDAO = Mockito.mock(OrganizationPurgeDAO.class);
    }

    @Test
    public void testOrganizationRemoval() throws APIManagementException {
        PowerMockito.mockStatic(OrganizationPurgeDAO.class);
        PowerMockito.when(OrganizationPurgeDAO.getInstance()).thenReturn(organizationPurgeDAO);

        Mockito.doNothing().when(organizationPurgeDAO).removePendingSubscriptions(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deleteApplicationList(Mockito.anyString());
        Mockito.doReturn(true).when(organizationPurgeDAO).applicationOrganizationExist(Mockito.anyString());

        ApplicationPurge applicationPurge = new ApplicationPurgeWrapper(organizationPurgeDAO);
        LinkedHashMap<String, String> subtaskResult = applicationPurge.purge("testOrg");

        for(Map.Entry<String, String> entry : subtaskResult.entrySet()) {
            Assert.assertEquals(entry.getKey() + " is not successful",
                    APIConstants.OrganizationDeletion.COMPLETED, entry.getValue());
        }

        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).
                removePendingSubscriptions(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).
                removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1))
                .deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).
                deleteApplicationList(Mockito.anyString());

    }
}
