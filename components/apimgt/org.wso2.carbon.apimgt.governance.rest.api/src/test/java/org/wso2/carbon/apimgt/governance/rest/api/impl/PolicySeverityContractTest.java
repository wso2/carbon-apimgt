/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicyList;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.APIMGovernancePolicyListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;

import java.util.Collections;

import javax.ws.rs.core.Response;

/**
 * Tests the contract the portals rely on for per-policy compliance affecting severities.
 * <p>
 * The field carries three meanings and each one has to survive a round trip. Null means the deployment has not
 * opted in and the control must not be offered at all. An empty string means the feature is available but this
 * policy counts every severity. A value lists the severities that decide the verdict. Getting any of these wrong
 * either hides a working feature or advertises one that cannot store anything.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PoliciesApiServiceImpl.class, APIMGovernanceAPIUtil.class})
public class PolicySeverityContractTest {

    private static final String POLICY_ID = "e5c3d190-413a-4e58-9b44-0a3b1bb741d5";
    private static final String RULESET_ID = "7ca1bcae-6feb-4c4b-9252-78e034b8b89e";
    private static final String ORGANIZATION = "carbon.super";

    private PolicyManager policyManager;
    private ComplianceManager complianceManager;
    private MessageContext messageContext;
    private PoliciesApiServiceImpl policiesApiService;

    @Before
    public void setUp() throws Exception {

        policyManager = Mockito.mock(PolicyManager.class);
        PowerMockito.whenNew(PolicyManager.class).withNoArguments().thenReturn(policyManager);

        complianceManager = Mockito.mock(ComplianceManager.class);
        PowerMockito.whenNew(ComplianceManager.class).withNoArguments().thenReturn(complianceManager);

        APIMGovernancePolicy stored = new APIMGovernancePolicy();
        stored.setId(POLICY_ID);
        stored.setName("Severity_Test_Policy");
        stored.setRulesetIds(Collections.singletonList(RULESET_ID));
        stored.setLabels(Collections.emptyList());
        stored.setActions(Collections.emptyList());
        stored.setGovernableStates(Collections.singletonList(APIMGovernableState.API_UPDATE));
        Mockito.when(policyManager.updateGovernancePolicy(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString()))
                .thenReturn(stored);
        Mockito.when(policyManager.createGovernancePolicy(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(APIMGovernancePolicy.class)))
                .thenAnswer(invocation -> {
                    // The service reads the id off the policy the manager returns, so the stub has to set one
                    APIMGovernancePolicy created = invocation.getArgument(1);
                    created.setId(POLICY_ID);
                    return created;
                });

        // The organization and the user are read through static helpers which reach into the Carbon runtime, so
        // they are stubbed rather than stood up. Neither is what this test is about.
        messageContext = Mockito.mock(MessageContext.class);
        PowerMockito.mockStatic(APIMGovernanceAPIUtil.class);
        PowerMockito.when(APIMGovernanceAPIUtil.getValidatedOrganization(
                ArgumentMatchers.any(MessageContext.class))).thenReturn(ORGANIZATION);
        PowerMockito.when(APIMGovernanceAPIUtil.getLoggedInUsername()).thenReturn("admin");

        policiesApiService = new PoliciesApiServiceImpl();
    }

    /**
     * Build the smallest policy payload the mapping layer accepts, carrying the given severity value
     *
     * @param complianceAffectingSeverities Value of the field under test, may be null
     * @return Policy payload
     */
    private APIMGovernancePolicyDTO payload(String complianceAffectingSeverities) {

        APIMGovernancePolicyDTO policyDTO = new APIMGovernancePolicyDTO();
        policyDTO.setId(POLICY_ID);
        policyDTO.setName("Severity_Test_Policy");
        policyDTO.setRulesets(Collections.singletonList(RULESET_ID));
        policyDTO.setLabels(Collections.singletonList("global"));
        policyDTO.setGovernableStates(
                Collections.singletonList(APIMGovernancePolicyDTO.GovernableStatesEnum.API_UPDATE));
        policyDTO.setComplianceAffectingSeverities(complianceAffectingSeverities);
        return policyDTO;
    }

    /**
     * Report the feature as available, with the given value stored against the policy
     *
     * @param stored Value held in the optional column, null when nothing is configured
     */
    private void featureAvailable(String stored) throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(true);
        Mockito.when(policyManager.getComplianceAffectingSeverities(POLICY_ID, ORGANIZATION)).thenReturn(stored);
    }

    /**
     * Read the field off the response of an update
     *
     * @param response Response returned by the service
     * @return Value of the field as the portals would see it
     */
    private String fieldOf(Response response) {

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return ((APIMGovernancePolicyDTO) response.getEntity()).getComplianceAffectingSeverities();
    }

    @Test
    public void testAConfiguredSelectionIsStoredAsGiven() throws Exception {

        featureAvailable("ERROR,WARN");

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"), messageContext);

        Mockito.verify(policyManager).updateComplianceAffectingSeverities(POLICY_ID, ORGANIZATION, "ERROR,WARN");
    }

    @Test
    public void testABlankSelectionClearsTheStoredValue() throws Exception {

        // The portals send an empty string when every severity is selected, because that is the same thing as
        // having nothing configured. It has to reach the column as null rather than as an empty string, so the
        // policy ends up indistinguishable from one that never used the feature.
        featureAvailable(null);

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(""), messageContext);

        Mockito.verify(policyManager).updateComplianceAffectingSeverities(POLICY_ID, ORGANIZATION, null);
    }

    @Test
    public void testAnOmittedFieldLeavesTheStoredValueAlone() throws Exception {

        // A client which knows nothing about severities, or one which found the field null and dropped it, must
        // not silently wipe a selection an administrator made through the portal.
        featureAvailable("ERROR,WARN");

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext);

        Mockito.verify(policyManager, Mockito.never()).updateComplianceAffectingSeverities(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any());
    }

    @Test
    public void testTheFieldIsNullWhenTheDeploymentHasNotOptedIn() throws Exception {

        // Neither half of the opt in, so neither reporter can claim the feature.
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        Assert.assertNull("A deployment which has not opted in must report null, which is what tells a portal not "
                        + "to offer the control at all",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheFieldIsNullWhenTheConfigurationIsOnButTheColumnIsMissing() throws Exception {

        // Half an opt in: the configuration wants the feature and the schema cannot store it. Writes are rejected
        // in that state, so reporting the empty string would offer a portal a control it is then refused when it
        // uses it. Null is documented as "not available here", which is exactly what this deployment is.
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        Assert.assertNull("A deployment which cannot store a severity must report null rather than an empty "
                        + "string, so the field and the write guard agree",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheFieldIsEmptyWhenTheFeatureIsAvailableButNothingIsConfigured() throws Exception {

        // Null in the column and null in the payload mean different things, so an available feature with nothing
        // configured has to be reported as an empty string. Reporting null here would hide the control.
        featureAvailable(null);

        Assert.assertEquals("An available but unconfigured policy must report an empty string, not null",
                "", fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheStoredSelectionIsReportedBack() throws Exception {

        featureAvailable("ERROR,WARN");

        Assert.assertEquals("ERROR,WARN",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheReportedValueComesFromTheStoreRatherThanTheRequest() throws Exception {

        // The response must describe what was stored, not echo what was asked for. A deployment which rejected or
        // normalised the value would otherwise report the request back and look as though it had taken effect.
        featureAvailable("ERROR");

        Assert.assertEquals("The response must reflect the stored value rather than the submitted one",
                "ERROR",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"),
                        messageContext)));
    }

    // Creating a policy takes a different route to updating one: there is nothing stored yet, so a blank value has
    // nothing to clear and must not be written at all.

    @Test
    public void testASelectionGivenAtCreationIsStored() throws Exception {

        featureAvailable("ERROR,WARN");

        policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);

        Mockito.verify(policyManager).updateComplianceAffectingSeverities(POLICY_ID, ORGANIZATION, "ERROR,WARN");
    }

    @Test
    public void testCreationStoresTheSelectionBeforeQueueingEvaluation() throws Exception {

        // The scheduler picks evaluation requests up on its own interval, so a request queued before the severity
        // write can be evaluated while the policy still has nothing stored, and every severity would affect
        // compliance rather than the ones the policy asked for. That is the verdict this feature exists to
        // correct, so creation must store the selection first. The update path already does.
        featureAvailable("ERROR,WARN");

        policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);

        InOrder order = Mockito.inOrder(policyManager, complianceManager);
        order.verify(policyManager).updateComplianceAffectingSeverities(POLICY_ID, ORGANIZATION, "ERROR,WARN");
        order.verify(complianceManager).handlePolicyChangeEvent(POLICY_ID, ORGANIZATION);
    }

    @Test
    public void testABlankSelectionAtCreationIsNotWritten() throws Exception {

        // A blank value means every severity counts, which is what a policy with nothing stored already does. The
        // write is skipped rather than clearing a column that was never set, so creating a policy on a deployment
        // which has not opted in is not rejected merely for sending the field.
        featureAvailable(null);

        policiesApiService.createGovernancePolicy(payload(""), messageContext);

        Mockito.verify(policyManager, Mockito.never()).updateComplianceAffectingSeverities(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any());
    }

    @Test
    public void testAnOmittedFieldAtCreationIsNotWritten() throws Exception {

        featureAvailable(null);

        policiesApiService.createGovernancePolicy(payload(null), messageContext);

        Mockito.verify(policyManager, Mockito.never()).updateComplianceAffectingSeverities(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any());
    }

    @Test
    public void testTheCreatedPolicyReportsTheThreeStateFieldBack() throws Exception {

        featureAvailable(null);

        Response response = policiesApiService.createGovernancePolicy(payload(null), messageContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertEquals("A created policy on an opted in deployment must report the available but unconfigured "
                        + "state, not null",
                "", ((APIMGovernancePolicyDTO) response.getEntity()).getComplianceAffectingSeverities());
    }

    @Test
    public void testTheCreatedPolicyReportsNullWhenTheDeploymentHasNotOptedIn() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Response response = policiesApiService.createGovernancePolicy(payload(null), messageContext);

        Assert.assertNull("A deployment which has not opted in must report null on creation too",
                ((APIMGovernancePolicyDTO) response.getEntity()).getComplianceAffectingSeverities());
    }

    // A deployment which cannot store a severity must be refused before anything is written, not after. Discovering
    // it on the second write leaves the policy changed while the client is told the request failed.

    @Test
    public void testAnUpdateIsRefusedBeforeThePolicyIsWrittenWhenTheSeverityCannotBeStored() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        try {
            policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"), messageContext);
            Assert.fail("Storing a severity on a deployment which cannot hold one must be refused");
        } catch (APIMGovernanceException expected) {
            Assert.assertTrue("The rejection must say what to do about it, since it is a deployment step rather "
                            + "than anything wrong with the request",
                    String.valueOf(expected.getMessage()).contains("per_policy_severity_filtering_enabled"));
            // A deployment which has not opted in is a supported state rather than a fault. Reporting it as a
            // server error would put a routine configuration answer into error dashboards, and would log a stack
            // trace for it on every attempt.
            Assert.assertEquals("A deployment which cannot store a severity is a client error, not a server failure",
                    Response.Status.BAD_REQUEST.getStatusCode(), expected.getErrorHandler().getHttpStatusCode());
        }

        Mockito.verify(policyManager, Mockito.never()).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testACreateIsRefusedBeforeThePolicyIsWrittenWhenTheSeverityCannotBeStored() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        try {
            policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);
            Assert.fail("Creating a policy with a severity it cannot store must be refused");
        } catch (APIMGovernanceException expected) {
            // The policy must not exist afterwards, which is what the verification below asserts
        }

        Mockito.verify(policyManager, Mockito.never()).createGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class));
    }

    @Test
    public void testAnUpdateWithoutSeveritiesIsUnaffectedByTheGuard() throws Exception {

        // The guard must only refuse requests that actually ask to store a severity. A client which never sends the
        // field has to keep working on a deployment which has not opted in.
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Response response = policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Mockito.verify(policyManager).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testACreateSendingABlankSeverityIsUnaffectedByTheGuard() throws Exception {

        // A blank value on create asks for nothing to be stored, so it must not be refused even though the
        // deployment could not have stored a real value.
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Response response = policiesApiService.createGovernancePolicy(payload(""), messageContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Mockito.verify(policyManager).createGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class));
    }

    // The listing has to report the same three states as the single policy response. Reporting null there while the
    // detail view reports a value would tell a client the feature is unavailable on a deployment that has it.

    /**
     * Ask the service for the policy listing and return the only policy in it
     *
     * @return The listed policy
     */
    private APIMGovernancePolicyDTO onlyListedPolicy() throws Exception {

        APIMGovernancePolicy listed = new APIMGovernancePolicy();
        listed.setId(POLICY_ID);
        listed.setName("Severity_Test_Policy");
        listed.setRulesetIds(Collections.singletonList(RULESET_ID));
        listed.setLabels(Collections.emptyList());
        listed.setActions(Collections.emptyList());
        listed.setGovernableStates(Collections.singletonList(APIMGovernableState.API_UPDATE));

        APIMGovernancePolicyList policyList = new APIMGovernancePolicyList();
        policyList.setCount(1);
        policyList.setGovernancePolicyList(Collections.singletonList(listed));
        Mockito.when(policyManager.getGovernancePolicies(ORGANIZATION)).thenReturn(policyList);

        Response response = policiesApiService.getGovernancePolicies(10, 0, null, messageContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return ((APIMGovernancePolicyListDTO) response.getEntity()).getList().get(0);
    }

    @Test
    public void testTheListingReportsTheStoredSelection() throws Exception {

        featureAvailable("ERROR,WARN");
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.singletonMap(POLICY_ID, "ERROR,WARN"));

        Assert.assertEquals("A listing must report what is stored, not null",
                "ERROR,WARN", onlyListedPolicy().getComplianceAffectingSeverities());
    }

    @Test
    public void testTheListingReportsTheUnconfiguredStateAsEmpty() throws Exception {

        featureAvailable(null);
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.<String, String>emptyMap());

        Assert.assertEquals("An available but unconfigured policy must be an empty string in a listing too, so the "
                        + "listing and the detail view agree",
                "", onlyListedPolicy().getComplianceAffectingSeverities());
    }

    @Test
    public void testTheListingReportsNullWhenTheDeploymentHasNotOptedIn() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        Assert.assertNull("A deployment which has not opted in must report null in the listing",
                onlyListedPolicy().getComplianceAffectingSeverities());
    }

    @Test
    public void testTheListingReportsNullWhenTheConfigurationIsOnButTheColumnIsMissing() throws Exception {

        // The listing has to reach the same conclusion as the detail view, or a portal reading the list offers the
        // control while the policy it opens says the feature is unavailable.
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);
        Mockito.when(policyManager.isComplianceAffectingSeverityStorageAvailable()).thenReturn(false);

        Assert.assertNull("A listing on a deployment which cannot store a severity must report null",
                onlyListedPolicy().getComplianceAffectingSeverities());
        // Nothing can be stored, so there is nothing worth reading either.
        Mockito.verify(policyManager, Mockito.never()).getComplianceAffectingSeverities(ORGANIZATION);
    }

    @Test
    public void testTheListingReadsTheSeveritiesInOneQuery() throws Exception {

        // One query per listed policy would turn a page of policies into a page of round trips.
        featureAvailable("ERROR");
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.singletonMap(POLICY_ID, "ERROR"));

        onlyListedPolicy();

        Mockito.verify(policyManager, Mockito.times(1)).getComplianceAffectingSeverities(ORGANIZATION);
        Mockito.verify(policyManager, Mockito.never()).getComplianceAffectingSeverities(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }
}
