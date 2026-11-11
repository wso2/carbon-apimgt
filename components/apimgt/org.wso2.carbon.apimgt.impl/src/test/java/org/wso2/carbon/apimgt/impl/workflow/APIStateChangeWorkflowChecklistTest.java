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

import com.google.gson.Gson;
import org.json.simple.JSONObject;
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
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LifeCycleUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Verifies that the lifecycle checklist selected when an API state change is requested (e.g. "Requires
 * re-subscription when publishing the API") is applied when the state change workflow is approved, instead of
 * falling back to an empty checklist as the pre-fix behaviour did. With an empty checklist, the subscriptions of
 * the previous API version were always carried over to the newly published version.
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({ "org.wso2.carbon.context.PrivilegedCarbonContext",
        "org.wso2.carbon.apimgt.impl.utils.LifeCycleUtils" })
@PrepareForTest({ ApiMgtDAO.class, APIManagerFactory.class, PrivilegedCarbonContext.class, APIUtil.class,
        LifeCycleUtils.class })
public class APIStateChangeWorkflowChecklistTest {

    private static final String API_NAME      = "PizzaShackAPI";
    private static final String API_VERSION   = "2.0.0";
    private static final String API_UUID      = "api-uuid-1";
    private static final String PROVIDER      = "admin";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int    TENANT_ID     = 1;
    private static final String EXT_WF_REF    = "ext-wf-ref-1";
    private static final String PUBLISH       = "Publish";

    private ApiMgtDAO apiMgtDAO;
    private APIProvider apiProvider;
    private ApiTypeWrapper apiTypeWrapper;

    @Before
    public void init() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        PowerMockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        apiProvider = Mockito.mock(APIProvider.class);
        Mockito.when(factory.getAPIProvider(Mockito.anyString())).thenReturn(apiProvider);

        // Suppress the static context lookups the workflow completion performs.
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext ctx = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(ctx);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getTenantDomainFromTenantId(TENANT_ID)).thenReturn(TENANT_DOMAIN);

        PowerMockito.mockStatic(LifeCycleUtils.class);

        apiTypeWrapper = new ApiTypeWrapper(new API(new APIIdentifier(PROVIDER, API_NAME, API_VERSION)));
        Mockito.when(apiMgtDAO.getUUIDFromIdentifier(Mockito.any(APIIdentifier.class), Mockito.eq(TENANT_DOMAIN)))
                .thenReturn(API_UUID);
        Mockito.when(apiProvider.getAPIorAPIProductByUUID(API_UUID, TENANT_DOMAIN)).thenReturn(apiTypeWrapper);
    }

    @Test
    public void checklistSurvivesWorkflowMetadataPersistence() {
        Map<String, Boolean> checklist = buildChecklist(true, false);

        // Encode the checklist the way APIProviderImpl stores it on the state change workflow request.
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setMetadata(WorkflowConstants.WF_METADATA_LIFECYCLE_CHECKLIST,
                new JSONObject(checklist).toJSONString());
        // Write and read back the metadata the way ApiMgtDAO does with the WF_METADATA column.
        String storedMetadata = workflowDTO.getMetadata().toJSONString();
        Workflow workflow = new Workflow();
        workflow.setMetadata(new Gson().fromJson(storedMetadata, JSONObject.class));

        Assert.assertEquals(checklist, WorkflowUtils.getLifecycleChecklist(workflow));
    }

    @Test
    public void missingChecklistMetadataReturnsEmptyChecklist() {
        // State change requests created before this fix was applied do not carry the checklist metadata key.
        Assert.assertTrue(WorkflowUtils.getLifecycleChecklist(buildWorkflow(null)).isEmpty());
    }

    @Test
    public void malformedChecklistMetadataReturnsEmptyChecklist() {
        Assert.assertTrue(WorkflowUtils.getLifecycleChecklist(buildWorkflow("not-a-json")).isEmpty());
    }

    @Test
    public void nonObjectChecklistMetadataReturnsEmptyChecklist() {
        // Valid JSON that is not an object must not fail the approval of the state change.
        Assert.assertTrue(WorkflowUtils.getLifecycleChecklist(buildWorkflow("null")).isEmpty());
        Assert.assertTrue(WorkflowUtils.getLifecycleChecklist(buildWorkflow("[]")).isEmpty());
    }

    @Test
    public void nonBooleanChecklistItemsAreLeftOut() {
        // Items without a boolean value must fall back to their default option instead of being read as false.
        String checklistJson = "{\"" + APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM + "\":\"TRUE \",\""
                + APIConstants.DEPRECATE_CHECK_LIST_ITEM + "\":true,\"nullItem\":null,\"numericItem\":1}";
        Map<String, Boolean> expected = new HashMap<>();
        expected.put(APIConstants.DEPRECATE_CHECK_LIST_ITEM, true);

        Assert.assertEquals(expected, WorkflowUtils.getLifecycleChecklist(buildWorkflow(checklistJson)));
    }

    @Test
    public void approvedStateChangeAppliesRequestedChecklist() throws Exception {
        Map<String, Boolean> checklist = buildChecklist(true, true);
        completeApprovalWorkflow(buildWorkflow(new JSONObject(checklist).toJSONString()), WorkflowStatus.APPROVED);

        Assert.assertEquals("Checklist selected in the Publisher must be applied on approval",
                checklist, captureChecklistAppliedOnApproval());
    }

    @Test
    public void approvedStateChangeWithoutStoredChecklistAppliesEmptyChecklist() throws Exception {
        // Pending requests created before this fix was applied keep the pre-fix behaviour.
        completeApprovalWorkflow(buildWorkflow(null), WorkflowStatus.APPROVED);

        Assert.assertTrue("Absent checklist metadata must fall back to an empty checklist",
                captureChecklistAppliedOnApproval().isEmpty());
    }

    @Test
    public void rejectedStateChangeDoesNotChangeLifecycle() throws Exception {
        completeApprovalWorkflow(buildWorkflow(new JSONObject(buildChecklist(true, false)).toJSONString()),
                WorkflowStatus.REJECTED);

        PowerMockito.verifyStatic(LifeCycleUtils.class, Mockito.never());
        LifeCycleUtils.changeLifecycle(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
    }

    // ---- helpers -----------------------------------------------------------

    private Map<String, Boolean> buildChecklist(boolean requireResubscription, boolean deprecateOldVersions) {
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM, requireResubscription);
        checklist.put(APIConstants.DEPRECATE_CHECK_LIST_ITEM, deprecateOldVersions);
        return checklist;
    }

    private Workflow buildWorkflow(String lifecycleChecklist) {
        Workflow workflow = new Workflow();
        workflow.setExternalWorkflowReference(EXT_WF_REF);
        workflow.setTenantDomain(TENANT_DOMAIN);
        workflow.setMetadata("ApiName", API_NAME);
        workflow.setMetadata("ApiVersion", API_VERSION);
        workflow.setMetadata("ApiProvider", PROVIDER);
        workflow.setMetadata("Action", PUBLISH);
        workflow.setMetadata("Invoker", PROVIDER);
        if (lifecycleChecklist != null) {
            workflow.setMetadata(WorkflowConstants.WF_METADATA_LIFECYCLE_CHECKLIST, lifecycleChecklist);
        }
        return workflow;
    }

    private WorkflowDTO buildWorkflowDTO(WorkflowStatus status) {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        workflowDTO.setExternalWorkflowReference(EXT_WF_REF);
        workflowDTO.setTenantId(TENANT_ID);
        workflowDTO.setTenantDomain(TENANT_DOMAIN);
        workflowDTO.setStatus(status);
        return workflowDTO;
    }

    private void completeApprovalWorkflow(Workflow workflow, WorkflowStatus status) throws Exception {
        Mockito.when(apiMgtDAO.getworkflowReferenceByExternalWorkflowReference(EXT_WF_REF)).thenReturn(workflow);
        new APIStateChangeApprovalWorkflowExecutor().complete(buildWorkflowDTO(status));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> captureChecklistAppliedOnApproval() throws Exception {
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        PowerMockito.verifyStatic(LifeCycleUtils.class);
        LifeCycleUtils.changeLifecycle(Mockito.eq(PROVIDER), Mockito.eq(apiProvider), Mockito.eq(TENANT_DOMAIN),
                Mockito.eq(apiTypeWrapper), Mockito.eq(PUBLISH), captor.capture());
        return captor.getValue();
    }
}
