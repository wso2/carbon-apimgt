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

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.cleanup.service.ApplicationPurge;
import org.wso2.carbon.apimgt.cleanup.service.OrganizationPurgeDAO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.mockito.Mockito;

public class ApplicationPurgeTest {

    private OrganizationPurgeDAO organizationPurgeDAO;
    private ApiMgtDAO apiMgtDAO;

    @Before
    public void init() {
        organizationPurgeDAO = Mockito.mock(OrganizationPurgeDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
    }

    @Test
    public void testOrganizationRemoval() throws APIManagementException {

        Mockito.doNothing().when(organizationPurgeDAO).removePendingSubscriptions(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.doNothing().when(organizationPurgeDAO).deleteApplicationList(Mockito.anyString());

        Subscriber subscriber = Mockito.mock(Subscriber.class);
        Mockito.doReturn(subscriber).when(apiMgtDAO).getSubscriber(Mockito.anyInt());

        ApplicationPurge applicationPurge = new ApplicationPurgeWrapper(organizationPurgeDAO);
        applicationPurge.deleteOrganization("testOrg");

        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).removePendingSubscriptions(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).removeApplicationCreationWorkflows(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1))
                .deletePendingApplicationRegistrations(Mockito.anyString());
        Mockito.verify(organizationPurgeDAO, Mockito.times(1)).deleteApplicationList(Mockito.anyString());

    }
}
