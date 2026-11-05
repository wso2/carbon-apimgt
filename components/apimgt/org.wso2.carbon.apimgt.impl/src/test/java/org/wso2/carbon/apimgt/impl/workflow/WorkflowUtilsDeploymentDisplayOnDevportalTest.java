/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.workflow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Set;

/**
 * Verifies that {@link WorkflowUtils#sendNotificationAfterWFComplete} (via its
 * revision-deployment branch) applies the user's requested
 * {@code displayOnDevportal} value from the workflow metadata on approval,
 * instead of hard-coding {@code true} as the pre-fix behaviour did.
 *
 * Regression guard for issue #19620 — "API revision deployment overrides the
 * displayOnDevportal value to true on each revision".
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
@PrepareForTest({ ApiMgtDAO.class, APIManagerFactory.class, PrivilegedCarbonContext.class })
public class WorkflowUtilsDeploymentDisplayOnDevportalTest {

    private static final String API_ID       = "api-uuid-1";
    private static final String REVISION_ID  = "revision-uuid-1";
    private static final String ENVIRONMENT  = "Default";
    private static final String PROVIDER     = "admin";
    private static final String ORG          = "carbon.super";
    private static final String USER         = "admin";
    private static final String EXT_WF_REF   = "ext-wf-ref-1";

    private ApiMgtDAO apiMgtDAO;
    private APIProvider apiProvider;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        apiProvider = Mockito.mock(APIProvider.class);
        Mockito.when(factory.getAPIProvider(PROVIDER)).thenReturn(apiProvider);

        // Suppress the static context lookups the method performs.
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext ctx = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(ctx);
    }

    @Test
    public void displayOnDevportalFalseInMetadataIsPreservedOnApproval() throws Exception {
        runCompleteDeploymentWorkflow(buildWorkflow("false"));
        Assert.assertFalse(
                "displayOnDevportal=false in workflow metadata must be preserved on approval",
                captureUpdatedDisplayOnDevportal());
    }

    @Test
    public void displayOnDevportalTrueInMetadataIsPreservedOnApproval() throws Exception {
        runCompleteDeploymentWorkflow(buildWorkflow("true"));
        Assert.assertTrue(
                "displayOnDevportal=true in workflow metadata must be preserved on approval",
                captureUpdatedDisplayOnDevportal());
    }

    @Test
    public void missingDisplayOnDevportalMetadataFallsBackToTrueForBackwardCompat() throws Exception {
        // Pending deployments created before this fix was applied do not carry the
        // "displayOnDevportal" metadata key — fall back to true to match the
        // pre-fix behaviour so a mid-flight upgrade does not regress their visibility.
        runCompleteDeploymentWorkflow(buildWorkflow(null));
        Assert.assertTrue(
                "Absent displayOnDevportal metadata must fall back to true",
                captureUpdatedDisplayOnDevportal());
    }

    // ---- helpers -----------------------------------------------------------

    private Workflow buildWorkflow(String displayOnDevportal) {
        Workflow workflow = new Workflow();
        workflow.setExternalWorkflowReference(EXT_WF_REF);
        workflow.setWorkflowReference(REVISION_ID);
        workflow.setTenantDomain(ORG);
        workflow.setMetadata("revisionId", "1");
        workflow.setMetadata("apiId", API_ID);
        workflow.setMetadata("apiProvider", PROVIDER);
        workflow.setMetadata("environment", ENVIRONMENT);
        workflow.setMetadata("userName", USER);
        if (displayOnDevportal != null) {
            workflow.setMetadata("displayOnDevportal", displayOnDevportal);
        }
        return workflow;
    }

    private void runCompleteDeploymentWorkflow(Workflow workflow) throws APIManagementException {
        Mockito.when(apiMgtDAO.getworkflowReferenceByExternalWorkflowReference(EXT_WF_REF))
                .thenReturn(workflow);

        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setExternalWorkflowReference(EXT_WF_REF);
        workflowDTO.setTenantDomain(ORG);

        WorkflowUtils.sendNotificationAfterWFComplete(workflowDTO,
                WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT);
    }

    @SuppressWarnings("unchecked")
    private boolean captureUpdatedDisplayOnDevportal() throws APIManagementException {
        ArgumentCaptor<Set<APIRevisionDeployment>> captor =
                ArgumentCaptor.forClass((Class) Set.class);
        Mockito.verify(apiMgtDAO).updateAPIRevisionDeployment(Mockito.eq(API_ID), captor.capture());
        Set<APIRevisionDeployment> captured = captor.getValue();
        Assert.assertEquals("Expected exactly one deployment to be updated",
                1, captured.size());
        APIRevisionDeployment deployment = captured.iterator().next();
        Assert.assertEquals("Updated deployment must target the correct env",
                ENVIRONMENT, deployment.getDeployment());
        Assert.assertEquals("Updated deployment must target the right revision",
                REVISION_ID, deployment.getRevisionUUID());
        return deployment.isDisplayOnDevportal();
    }
}
