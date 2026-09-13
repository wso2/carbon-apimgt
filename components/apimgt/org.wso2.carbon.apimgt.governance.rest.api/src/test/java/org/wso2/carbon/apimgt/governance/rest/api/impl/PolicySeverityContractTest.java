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
import org.mockito.ArgumentCaptor;
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
 * either hides a working feature or advertises one that stores nothing.
 * <p>
 * The severity is written by the same statement as the policy it belongs to, so these tests assert what reaches
 * the manager on the policy rather than looking for a second write. Which of the two statements the manager then
 * issues is decided in the DAO and covered by its own tests.
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

        Mockito.when(policyManager.updateGovernancePolicy(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString()))
                .thenReturn(storedPolicy());
        // Deliberately a different object to the one passed in, so the argument captured below still holds what
        // the request asked for rather than the value the service writes onto the response afterwards.
        Mockito.when(policyManager.createGovernancePolicy(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(APIMGovernancePolicy.class)))
                .thenReturn(storedPolicy());

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
     * Build the policy the manager hands back from a write
     *
     * @return Policy as it is held
     */
    private APIMGovernancePolicy storedPolicy() {

        APIMGovernancePolicy stored = new APIMGovernancePolicy();
        stored.setId(POLICY_ID);
        stored.setName("Severity_Test_Policy");
        stored.setRulesetIds(Collections.singletonList(RULESET_ID));
        stored.setLabels(Collections.emptyList());
        stored.setActions(Collections.emptyList());
        stored.setGovernableStates(Collections.singletonList(APIMGovernableState.API_UPDATE));
        return stored;
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
     * Report the feature as enabled, with the given value stored against the policy
     *
     * @param stored Value held in the optional column, null when nothing is configured
     */
    private void featureEnabled(String stored) throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);
        Mockito.when(policyManager.getComplianceAffectingSeverities(POLICY_ID, ORGANIZATION)).thenReturn(stored);
    }

    /**
     * Severity value the service put on the policy it asked the manager to update
     *
     * @return Value carried on the policy, null when the field was not sent
     */
    private String severitiesSentToUpdate() throws Exception {

        ArgumentCaptor<APIMGovernancePolicy> captor = ArgumentCaptor.forClass(APIMGovernancePolicy.class);
        Mockito.verify(policyManager).updateGovernancePolicy(ArgumentMatchers.eq(POLICY_ID), captor.capture(),
                ArgumentMatchers.eq(ORGANIZATION));
        return captor.getValue().getComplianceAffectingSeverities();
    }

    /**
     * Severity value the service put on the policy it asked the manager to create
     *
     * @return Value carried on the policy, null when the field was not sent
     */
    private String severitiesSentToCreate() throws Exception {

        ArgumentCaptor<APIMGovernancePolicy> captor = ArgumentCaptor.forClass(APIMGovernancePolicy.class);
        Mockito.verify(policyManager).createGovernancePolicy(ArgumentMatchers.eq(ORGANIZATION), captor.capture());
        return captor.getValue().getComplianceAffectingSeverities();
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
    public void testAConfiguredSelectionTravelsOnThePolicyWrite() throws Exception {

        // One write, not two: the selection has to reach the manager on the policy itself, so it lands in the same
        // transaction. Sending it separately is what used to leave a policy updated after the severity failed.
        featureEnabled("ERROR,WARN");

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"), messageContext);

        Assert.assertEquals("ERROR,WARN", severitiesSentToUpdate());
        Mockito.verify(policyManager, Mockito.times(1)).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testABlankSelectionReachesTheWriteAsBlankRatherThanBeingDropped() throws Exception {

        // The portals send an empty string when every severity is selected, because that is the same thing as
        // having nothing configured. It has to reach the write, since on update a blank value clears a stored
        // selection; the DAO is what turns it into a null column value.
        featureEnabled(null);

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(""), messageContext);

        Assert.assertEquals("", severitiesSentToUpdate());
    }

    @Test
    public void testAnOmittedFieldReachesTheWriteAsNull() throws Exception {

        // A client which knows nothing about severities, or one which found the field null and dropped it, must
        // not silently wipe a selection an administrator made through the portal. Null is how the DAO is told to
        // run the statement that does not name the column, which is what preserves the stored value.
        featureEnabled("ERROR,WARN");

        policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext);

        Assert.assertNull("An absent field must stay absent all the way to the write, or it clears the selection",
                severitiesSentToUpdate());
    }

    @Test
    public void testTheFieldIsNullWhenTheDeploymentHasNotOptedIn() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Assert.assertNull("A deployment which has not opted in must report null, which is what tells a portal not "
                        + "to offer the control at all",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheConfigurationAloneDecidesWhetherTheFieldIsOffered() throws Exception {

        // The settings resource reports the configuration, and so does this field. Nothing else is consulted, so
        // the two cannot disagree about whether the control exists — which they could while the field reported
        // storage availability and the settings endpoint reported the flag.
        featureEnabled(null);

        Assert.assertEquals("With the configuration on, the field must describe the policy rather than the schema",
                "", fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
        Mockito.verify(policyManager, Mockito.atLeastOnce()).isComplianceAffectingSeverityFilteringEnabled();
    }

    @Test
    public void testTheFieldIsEmptyWhenTheFeatureIsEnabledButNothingIsConfigured() throws Exception {

        // Null in the column and null in the payload mean different things, so an enabled feature with nothing
        // configured has to be reported as an empty string. Reporting null here would hide the control.
        featureEnabled(null);

        Assert.assertEquals("An enabled but unconfigured policy must report an empty string, not null",
                "", fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheStoredSelectionIsReportedBack() throws Exception {

        featureEnabled("ERROR,WARN");

        Assert.assertEquals("ERROR,WARN",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext)));
    }

    @Test
    public void testTheReportedValueComesFromTheStoreRatherThanTheRequest() throws Exception {

        // The response must describe what was stored, not echo what was asked for. A deployment which rejected or
        // normalised the value would otherwise report the request back and look as though it had taken effect.
        featureEnabled("ERROR");

        Assert.assertEquals("The response must reflect the stored value rather than the submitted one",
                "ERROR",
                fieldOf(policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"),
                        messageContext)));
    }

    // Creating a policy takes the same route as updating one, but there is nothing stored yet, so a blank value has
    // nothing to preserve and nothing to clear.

    @Test
    public void testASelectionGivenAtCreationTravelsOnThePolicyInsert() throws Exception {

        featureEnabled("ERROR,WARN");

        policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);

        Assert.assertEquals("ERROR,WARN", severitiesSentToCreate());
    }

    @Test
    public void testCreationStoresTheSelectionBeforeQueueingEvaluation() throws Exception {

        // The scheduler picks evaluation requests up on its own interval, so a request queued before the severity
        // is stored can be evaluated while the policy still has nothing stored, and every severity would affect
        // compliance rather than the ones the policy asked for. That is the verdict this feature exists to
        // correct, so the write has to land first. It now does so by construction, since the severity is part of
        // the insert, and this asserts the ordering has not been reversed.
        featureEnabled("ERROR,WARN");

        policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);

        InOrder order = Mockito.inOrder(policyManager, complianceManager);
        order.verify(policyManager).createGovernancePolicy(ArgumentMatchers.eq(ORGANIZATION),
                ArgumentMatchers.any(APIMGovernancePolicy.class));
        order.verify(complianceManager).handlePolicyChangeEvent(POLICY_ID, ORGANIZATION);
    }

    /**
     * Report the feature as enabled, with the read that describes the policy failing
     * <p>
     * This is the half configured deployment reaching the response: the column the read names is not there, so
     * the read fails where the write did not.
     *
     * @throws Exception If the stubbing cannot be installed
     */
    private void featureEnabledWithAFailingRead() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);
        Mockito.when(policyManager.getComplianceAffectingSeverities(POLICY_ID, ORGANIZATION))
                .thenThrow(new APIMGovernanceException("Unable to read the stored severities"));
    }

    @Test
    public void testACreateStillQueuesEvaluationWhenDescribingThePolicyFails() throws Exception {

        // The policy is committed before the response is assembled, so a read which only shapes that response
        // must not be able to cost the policy its evaluation. It cannot be recovered by retrying either: the
        // policy now exists, so the retry is refused as a duplicate name and the artifacts are never judged
        // against it.
        featureEnabledWithAFailingRead();

        try {
            policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);
            Assert.fail("A failed read must still be reported to the caller");
        } catch (APIMGovernanceException expected) {
            // The caller is told, which is right; what matters is what was queued before it was told.
        }

        Mockito.verify(complianceManager).handlePolicyChangeEvent(POLICY_ID, ORGANIZATION);
    }

    @Test
    public void testAnUpdateStillQueuesEvaluationWhenDescribingThePolicyFails() throws Exception {

        // Worse than the create: the update clears the policy's stored results inside its own transaction, so
        // skipping the queue here leaves the policy with its previous verdicts deleted and nothing scheduled to
        // replace them.
        featureEnabledWithAFailingRead();

        try {
            policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"), messageContext);
            Assert.fail("A failed read must still be reported to the caller");
        } catch (APIMGovernanceException expected) {
            // As above, the report is correct and is not what this asserts.
        }

        Mockito.verify(complianceManager).handlePolicyChangeEvent(POLICY_ID, ORGANIZATION);
    }

    @Test
    public void testACreateIssuesOneWriteOnly() throws Exception {

        // The severity used to be a follow up update. If one reappears, a create can be committed while the
        // severity write fails, which is the state this design removes.
        featureEnabled("ERROR,WARN");

        policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);

        Mockito.verify(policyManager, Mockito.times(1)).createGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class));
        Mockito.verify(policyManager, Mockito.never()).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testTheCreatedPolicyReportsTheThreeStateFieldBack() throws Exception {

        featureEnabled(null);

        Response response = policiesApiService.createGovernancePolicy(payload(null), messageContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertEquals("A created policy on an opted in deployment must report the enabled but unconfigured "
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

    // A request which asks for a selection while the feature is switched off has to be refused rather than
    // quietly ignored: with the configuration off no statement names the column, so nothing would fail on its own.

    @Test
    public void testAnUpdateAskingForSeveritiesIsRefusedWhileTheFeatureIsOff() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        try {
            policiesApiService.updateGovernancePolicyById(POLICY_ID, payload("ERROR,WARN"), messageContext);
            Assert.fail("Asking for a severity selection while the feature is off must be refused");
        } catch (APIMGovernanceException expected) {
            Assert.assertTrue("The rejection must say what to do about it, since it is a deployment step rather "
                            + "than anything wrong with the request",
                    String.valueOf(expected.getMessage()).contains("per_policy_severity_filtering_enabled"));
            // A deployment which has not opted in is a supported state rather than a fault. Reporting it as a
            // server error would put a routine configuration answer into error dashboards, and would log a stack
            // trace for it on every attempt.
            Assert.assertEquals("A deployment which has not enabled the feature is a client error, not a server "
                            + "failure",
                    Response.Status.BAD_REQUEST.getStatusCode(), expected.getErrorHandler().getHttpStatusCode());
        }

        Mockito.verify(policyManager, Mockito.never()).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testACreateAskingForSeveritiesIsRefusedWhileTheFeatureIsOff() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        try {
            policiesApiService.createGovernancePolicy(payload("ERROR,WARN"), messageContext);
            Assert.fail("Creating a policy with a severity selection while the feature is off must be refused");
        } catch (APIMGovernanceException expected) {
            // The policy must not exist afterwards, which is what the verification below asserts
        }

        Mockito.verify(policyManager, Mockito.never()).createGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class));
    }

    @Test
    public void testAnUpdateWithoutSeveritiesIsUnaffectedByTheGuard() throws Exception {

        // The guard must only refuse requests that actually ask for a selection. A client which never sends the
        // field has to keep working on a deployment which has not opted in.
        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Response response = policiesApiService.updateGovernancePolicyById(POLICY_ID, payload(null), messageContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Mockito.verify(policyManager).updateGovernancePolicy(ArgumentMatchers.anyString(),
                ArgumentMatchers.any(APIMGovernancePolicy.class), ArgumentMatchers.anyString());
    }

    @Test
    public void testACreateSendingABlankSeverityIsUnaffectedByTheGuard() throws Exception {

        // A blank value on create asks for nothing in particular, so it must not be refused even on a deployment
        // which has not enabled the feature.
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

        APIMGovernancePolicyList policyList = new APIMGovernancePolicyList();
        policyList.setCount(1);
        policyList.setGovernancePolicyList(Collections.singletonList(storedPolicy()));
        Mockito.when(policyManager.getGovernancePolicies(ORGANIZATION)).thenReturn(policyList);

        Response response = policiesApiService.getGovernancePolicies(10, 0, null, messageContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return ((APIMGovernancePolicyListDTO) response.getEntity()).getList().get(0);
    }

    @Test
    public void testTheListingReportsTheStoredSelection() throws Exception {

        featureEnabled("ERROR,WARN");
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.singletonMap(POLICY_ID, "ERROR,WARN"));

        Assert.assertEquals("A listing must report what is stored, not null",
                "ERROR,WARN", onlyListedPolicy().getComplianceAffectingSeverities());
    }

    @Test
    public void testTheListingReportsTheUnconfiguredStateAsEmpty() throws Exception {

        featureEnabled(null);
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.<String, String>emptyMap());

        Assert.assertEquals("An enabled but unconfigured policy must be an empty string in a listing too, so the "
                        + "listing and the detail view agree",
                "", onlyListedPolicy().getComplianceAffectingSeverities());
    }

    @Test
    public void testTheListingReportsNullWhenTheDeploymentHasNotOptedIn() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        Assert.assertNull("A deployment which has not opted in must report null in the listing",
                onlyListedPolicy().getComplianceAffectingSeverities());
        // Nothing is offered, so nothing is worth reading either.
        Mockito.verify(policyManager, Mockito.never()).getComplianceAffectingSeverities(ORGANIZATION);
    }

    @Test
    public void testTheListingReadsTheSeveritiesInOneQuery() throws Exception {

        // One query per listed policy would turn a page of policies into a page of round trips.
        featureEnabled("ERROR");
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.singletonMap(POLICY_ID, "ERROR"));

        onlyListedPolicy();

        Mockito.verify(policyManager, Mockito.times(1)).getComplianceAffectingSeverities(ORGANIZATION);
        Mockito.verify(policyManager, Mockito.never()).getComplianceAffectingSeverities(
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }
}
