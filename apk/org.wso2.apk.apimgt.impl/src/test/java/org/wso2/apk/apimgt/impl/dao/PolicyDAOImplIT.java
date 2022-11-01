package org.wso2.apk.apimgt.impl.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.apk.apimgt.api.model.policy.APIPolicy;
import org.wso2.apk.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.apk.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.apk.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.apk.apimgt.impl.dao.impl.PolicyDAOImpl;

public class PolicyDAOImplIT extends DAOIntegrationTestBase {
    private static final Logger log = LoggerFactory.getLogger(PolicyDAOImplIT.class);

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Adds an application policy, fetch it by policy name and UUID, Check added policy all available
    |         application policy list and remove added policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify Application policy fetched from DB is not null after it was successfully added
    |         Verify policy name of the application policy fetched by policy name is equal to the name of the policy added
    |         Verify the count of all available application policies in the organization is 1
    |         Verify the added policy's name is among the available application policy names fetched from DB
    |         Verify the policy fetched by policy ID is not null
    |         Verify policy name of the application policy fetched by UUID is equal to the name of the policy added
    |         Verify policy object fetched by policy name is null after the policy is deleted
    -------------------------------------------------------------------*/
    @Test(description = "Add, Get and Delete an Application policy")
    public void testAddGetAndDeleteApplicationPolicy() throws Exception {
        ApplicationPolicy policy = TestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();

        //add policy
        policyDAO.addApplicationPolicy(policy);

        //get added policy
        ApplicationPolicy addedPolicy = policyDAO
                .getApplicationPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());

        ApplicationPolicy[] applicationPolicies = policyDAO.getApplicationPolicies("carbon.super");
        Assert.assertEquals(applicationPolicies.length, 1);

        String[] policyNames = policyDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_APP, "admin@carbon.super");
        Assert.assertEquals(policyNames.length, 1);
        Assert.assertEquals(policyNames[0], policy.getPolicyName());

        ApplicationPolicy addedPolicyByUUID = policyDAO.getApplicationPolicyByUUID(addedPolicy.getUUID());
        Assert.assertNotNull(addedPolicyByUUID);
        Assert.assertEquals(addedPolicyByUUID.getPolicyName(), policy.getPolicyName());

        //delete policy
        policyDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, policy.getPolicyName(), "carbon.super");

        //get policy after deletion
        ApplicationPolicy deletedPolicy = policyDAO.getApplicationPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNull(deletedPolicy);
    }

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Add and update application policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify Application polciy request count after throttle policy update
    |         Verify Application polciy description after throttle policy update
    -------------------------------------------------------------------*/
    @Test(description = "Update an Application policy")
    public void testUpdateApplicationPolicy() throws Exception {
        ApplicationPolicy policy = TestObjectCreator.createDefaultApplicationPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();
        policyDAO.addApplicationPolicy(policy);

        ApplicationPolicy policyToUpdate = TestObjectCreator.createUpdatedApplicationPolicy();
        policyDAO.updateApplicationPolicy(policyToUpdate);
        ApplicationPolicy updatedPolicy = policyDAO.getApplicationPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertEquals(((RequestCountLimit)(updatedPolicy.getDefaultQuotaPolicy().getLimit())).getRequestCount(), 200);
        Assert.assertEquals(updatedPolicy.getDescription(), "Updated Custom Application Policy");

        //delete policy
        policyDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_APP, policy.getPolicyName(), "carbon.super");
    }

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Adds a subscription policy, fetch it by policy name and UUID, Check added policy all available
    |         subscription policy list and remove added policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify subscription policy fetched from DB is not null after it was successfully added
    |         Verify policy name of the subscription policy fetched by policy name is equal to the name of the policy added
    |         Verify the count of all available subscription policies in the organization is 1
    |         Verify the added policy's name is among the available subscription policy names fetched from DB
    |         Verify the policy fetched by policy ID is not null
    |         Verify policy name of the subscription policy fetched by UUID is equal to the name of the policy added
    |         Verify policy object fetched by policy name is null after the policy is deleted
    -------------------------------------------------------------------*/
    @Test(description = "Add, Get and Delete a Subscription policy")
    public void testAddGetAndDeleteSubscriptionPolicy() throws Exception {
        SubscriptionPolicy policy = TestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();

        //add policy
        policyDAO.addSubscriptionPolicy(policy);

        //get added policy
        SubscriptionPolicy addedPolicy = policyDAO
                .getSubscriptionPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());

        SubscriptionPolicy[] subscriptionPolicies = policyDAO.getSubscriptionPolicies("carbon.super");
        Assert.assertEquals(subscriptionPolicies.length, 1);

        String[] policyNames = policyDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, "admin@carbon.super");
        Assert.assertEquals(policyNames.length, 1);
        Assert.assertEquals(policyNames[0], policy.getPolicyName());

        SubscriptionPolicy addedPolicyByUUID = policyDAO.getSubscriptionPolicyByUUID(addedPolicy.getUUID());
        Assert.assertNotNull(addedPolicyByUUID);
        Assert.assertEquals(addedPolicyByUUID.getPolicyName(), policy.getPolicyName());

        //delete policy
        policyDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_SUB, policy.getPolicyName(), "carbon.super");

        //get policy after deletion
        SubscriptionPolicy deletedPolicy = policyDAO.getSubscriptionPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNull(deletedPolicy);
    }

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Add and update subscription policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify subscription polciy request count after throttle policy update
    |         Verify subscription polciy description after throttle policy update
    -------------------------------------------------------------------*/
    @Test(description = "Update a Subscription policy")
    public void testUpdateSubscriptionPolicy() throws Exception {
        SubscriptionPolicy policy = TestObjectCreator.createDefaultSubscriptionPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();
        policyDAO.addSubscriptionPolicy(policy);

        SubscriptionPolicy policyToUpdate = TestObjectCreator.createUpdatedSubscriptionPolicy();
        policyDAO.updateSubscriptionPolicy(policyToUpdate);
        SubscriptionPolicy updatedPolicy = policyDAO.getSubscriptionPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertEquals(((RequestCountLimit)(updatedPolicy.getDefaultQuotaPolicy().getLimit())).getRequestCount(), 200);
        Assert.assertEquals(updatedPolicy.getDescription(), "Updated Custom Subscription Policy");
    }

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Adds an api policy, fetch it by policy name and UUID, Check added policy all available
    |         api policy list and remove added policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify API policy fetched from DB is not null after it was successfully added
    |         Verify policy name of the API policy fetched by policy name is equal to the name of the policy added
    |         Verify the count of all available API policies in the organization is 1
    |         Verify the added policy's name is among the available API policy names fetched from DB
    |         Verify the policy fetched by policy ID is not null
    |         Verify policy name of the API policy fetched by UUID is equal to the name of the policy added
    |         Verify policy object fetched by policy name is null after the policy is deleted
    -------------------------------------------------------------------*/
    @Test(description = "Add, Get and Delete an API policy")
    public void testAddGetAndDeleteAPIPolicy() throws Exception {
        APIPolicy policy = TestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();

        //add policy
        policyDAO.addAPIPolicy(policy);

        //get added policy
        APIPolicy addedPolicy = policyDAO
                .getAPIPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNotNull(addedPolicy);
        Assert.assertEquals(addedPolicy.getPolicyName(), policy.getPolicyName());

        APIPolicy[] apiPolicies = policyDAO.getAPIPolicies("carbon.super");
        Assert.assertEquals(apiPolicies.length, 1);

        String[] policyNames = policyDAO.getPolicyNames(PolicyConstants.POLICY_LEVEL_API, "admin@carbon.super");
        Assert.assertEquals(policyNames.length, 1);
        Assert.assertEquals(policyNames[0], policy.getPolicyName());

        APIPolicy addedPolicyByUUID = policyDAO.getAPIPolicyByUUID(addedPolicy.getUUID());
        Assert.assertNotNull(addedPolicyByUUID);
        Assert.assertEquals(addedPolicyByUUID.getPolicyName(), policy.getPolicyName());

        //delete policy
        policyDAO.removeThrottlePolicy(PolicyConstants.POLICY_LEVEL_API, policy.getPolicyName(), "carbon.super");

        //get policy after deletion
        APIPolicy deletedPolicy = policyDAO.getAPIPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertNull(deletedPolicy);
    }

    /*---------------------------------------------------------------------
    |  Author:  Sachini De Silva
    |  Test Scenario:  Add and update API policy
    |  Traceability: https://github.com/wso2/apk/issues/63
    |  Pre-condition: N/A
    |  Post-condition: N/A
    |  Dependencies: N/A
    |  Assertions:
    |         Verify API polciy request count after throttle policy update
    |         Verify API polciy description after throttle policy update
    -------------------------------------------------------------------*/
    @Test(description = "Update an API policy")
    public void testUpdateAPIPolicy() throws Exception {
        APIPolicy policy = TestObjectCreator.createDefaultAPIPolicy();
        PolicyDAO policyDAO = PolicyDAOImpl.getInstance();
        policyDAO.addAPIPolicy(policy);

        APIPolicy policyToUpdate = TestObjectCreator.createUpdatedAPIPolicy();
        policyDAO.updateAPIPolicy(policyToUpdate);
        APIPolicy updatedPolicy = policyDAO.getAPIPolicy(policy.getPolicyName(), "carbon.super");
        Assert.assertEquals(((RequestCountLimit)(updatedPolicy.getDefaultQuotaPolicy().getLimit())).getRequestCount(), 200);
        Assert.assertEquals(updatedPolicy.getDescription(), "Updated Custom API Policy");
    }
}
