package org.wso2.carbon.apimgt.impl.lifecycle;

import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class LCManagerTestCase extends TestCase {

    String LCConfigObj = "{\"States\":[{\"State\":\"Created\",\"CheckItems\":[\"Deprecate old versions after publishing the API\",\"Requires re-subscription when publishing the API\"],\"Transitions\":[{\"Event\":\"Publish\",\"Target\":\"Published\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"}]},{\"State\":\"Prototyped\",\"CheckItems\":[\"Deprecate old versions after publishing the API\",\"Requires re-subscription when publishing the API\"],\"Transitions\":[{\"Event\":\"Publish\",\"Target\":\"Published\"},{\"Event\":\"Demote to Created\",\"Target\":\"Created\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"}]},{\"State\":\"Published\",\"Transitions\":[{\"Event\":\"Block\",\"Target\":\"Blocked\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"},{\"Event\":\"Demote to Created\",\"Target\":\"Created\"},{\"Event\":\"Deprecate\",\"Target\":\"Deprecated\"},{\"Event\":\"Publish\",\"Target\":\"Published\"}]},{\"State\":\"Blocked\",\"Transitions\":[{\"Event\":\"Deprecate\",\"Target\":\"Deprecated\"},{\"Event\":\"Re-Publish\",\"Target\":\"Published\"}]},{\"State\":\"Deprecated\",\"Transitions\":[{\"Event\":\"Retire\",\"Target\":\"Retired\"}]},{\"State\":\"Retired\"}]}";

    @Test
    public void testGetDefaultLCConfigJSON() throws ParseException, APIManagementException {

        LCManager LCManager = new LCManager("carbon.super");
        JSONParser jsonParser = new JSONParser();
        String LCConfig = String.valueOf(this.LCConfigObj);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(LCConfig);
        Assert.assertEquals(jsonObject, LCManager.getDefaultLCConfigJSON());
    }

    public void prepareGetTenantConfig() throws Exception {

        PowerMockito.mockStatic(APIUtil.class);
        String json = "{\"EnableMonetization\":false,\"EnableRecommendation\":false,\"IsUnlimitedTierPaid\":false,\"ExtensionHandlerPosition\":\"bottom\",\"RESTAPIScopes\":{\"Scope\":[{\"Name\":\"apim:api_publish\",\"Roles\":\"admin,Internal/publisher\"},{\"Name\":\"apim:api_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_generate_key\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:api_view\",\"Roles\":\"admin,Internal/publisher,Internal/creator,Internal/analytics,Internal/observer\"},{\"Name\":\"apim:api_delete\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:subscribe\",\"Roles\":\"admin,Internal/subscriber,Internal/devops\"},{\"Name\":\"apim:tier_view\",\"Roles\":\"admin,Internal/publisher,Internal/creator\"},{\"Name\":\"apim:tier_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:bl_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:bl_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:subscription_view\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:subscription_block\",\"Roles\":\"admin,Internal/publisher\"},{\"Name\":\"apim:subscription_manage\",\"Roles\":\"admin,Internal/publisher\"},{\"Name\":\"apim:mediation_policy_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:mediation_policy_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_mediation_policy_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_workflow_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:api_workflow_approve\",\"Roles\":\"admin\"},{\"Name\":\"apim:admin\",\"Roles\":\"admin\"},{\"Name\":\"apim:app_owner_change\",\"Roles\":\"admin\"},{\"Name\":\"apim:app_import_export\",\"Roles\":\"admin,Internal/devops\"},{\"Name\":\"apim:api_import_export\",\"Roles\":\"admin,Internal/devops\"},{\"Name\":\"apim:api_product_import_export\",\"Roles\":\"admin,Internal/devops\"},{\"Name\":\"apim:label_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:label_read\",\"Roles\":\"admin\"},{\"Name\":\"apim:app_update\",\"Roles\":\"admin,Internal/subscriber\"},{\"Name\":\"apim:app_manage\",\"Roles\":\"admin,Internal/subscriber,Internal/devops\"},{\"Name\":\"apim:sub_manage\",\"Roles\":\"admin,Internal/subscriber,Internal/devops\"},{\"Name\":\"apim:monetization_usage_publish\",\"Roles\":\"admin, Internal/publisher\"},{\"Name\":\"apim:document_create\",\"Roles\":\"admin, Internal/creator,Internal/publisher\"},{\"Name\":\"apim:ep_certificates_update\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:client_certificates_update\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:client_certificates_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:threat_protection_policy_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:document_manage\",\"Roles\":\"admin, Internal/creator,Internal/publisher\"},{\"Name\":\"apim:client_certificates_add\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:publisher_settings\",\"Roles\":\"admin,Internal/creator,Internal/publisher,Internal/observer\"},{\"Name\":\"apim:store_settings\",\"Roles\":\"admin,Internal/subscriber\"},{\"Name\":\"apim:admin_settings\",\"Roles\":\"admin\"},{\"Name\":\"apim:client_certificates_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:mediation_policy_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:threat_protection_policy_create\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:ep_certificates_add\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:ep_certificates_view\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:ep_certificates_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:api_key\",\"Roles\":\"admin,Internal/subscriber\"},{\"Name\":\"apim_analytics:admin\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:monitoring_dashboard:own\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:monitoring_dashboard:edit\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:monitoring_dashboard:view\",\"Roles\":\"admin,Internal/analytics\"},{\"Name\":\"apim_analytics:business_analytics:own\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:business_analytics:edit\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:business_analytics:view\",\"Roles\":\"admin,Internal/analytics\"},{\"Name\":\"apim_analytics:api_analytics:own\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:api_analytics:edit\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:api_analytics:view\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim_analytics:application_analytics:own\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:application_analytics:edit\",\"Roles\":\"admin\"},{\"Name\":\"apim_analytics:application_analytics:view\",\"Roles\":\"admin,Internal/subscriber\"},{\"Name\":\"apim:pub_alert_manage\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:sub_alert_manage\",\"Roles\":\"admin,Internal/subscriber\"},{\"Name\":\"apim:tenantInfo\",\"Roles\":\"admin\"},{\"Name\":\"apim:tenant_theme_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:admin_operations\",\"Roles\":\"admin\"},{\"Name\":\"apim:shared_scope_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:admin_alert_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:bot_data\",\"Roles\":\"admin\"},{\"Name\":\"apim:scope_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:role_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:environment_manage\",\"Roles\":\"admin\"},{\"Name\":\"apim:environment_read\",\"Roles\":\"admin\"},{\"Name\":\"service_catalog:service_view\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"service_catalog:service_write\",\"Roles\":\"admin,Internal/creator\"},{\"Name\":\"apim:comment_view\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:comment_write\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:comment_manage\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:throttling_policy_manage\",\"Roles\":\"admin,Internal/publisher,Internal/creator,Internal/analytics\"},{\"Name\":\"apim:admin_application_view\",\"Roles\":\"admin\"},{\"Name\":\"apim:api_list_view\",\"Roles\":\"Internal/integration_dev\"},{\"Name\":\"apim:api_definition_view\",\"Roles\":\"Internal/integration_dev\"},{\"Name\":\"apim:common_operation_policy_view\",\"Roles\":\"admin,Internal/creator,Internal/publisher\"},{\"Name\":\"apim:common_operation_policy_manage\",\"Roles\":\"admin,Internal/creator\"}]},\"Meta\":{\"Migration\":{\"3.0.0\":true}},\"NotificationsEnabled\":\"false\",\"Notifications\":[{\"Type\":\"new_api_version\",\"Notifiers\":[{\"Class\":\"org.wso2.carbon.apimgt.impl.notification.NewAPIVersionEmailNotifier\",\"ClaimsRetrieverImplClass\":\"org.wso2.carbon.apimgt.impl.token.DefaultClaimsRetriever\",\"Title\":\"Version $2 of $1 Released\",\"Template\":\" <html> <body> <h3 style=\\\"color:Black;\\\">We’re happy to announce the arrival of the next major version $2 of $1 API which is now available in Our API Store.</h3><a href=\\\"https://localhost:9443/store\\\">Click here to Visit WSO2 API Store</a></body></html>\"}]}],\"DefaultRoles\":{\"PublisherRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":\"Internal/publisher\"},\"CreatorRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":\"Internal/creator\"},\"SubscriberRole\":{\"CreateOnTenantLoad\":true},\"DevOpsRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":\"Internal/devops\"},\"ObserverRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":\"Internal/observer\"},\"IntegrationDeveloperRole\":{\"CreateOnTenantLoad\":true,\"RoleName\":\"Internal/integration_dev\"}},\"LifeCycle\":{\"States\":[{\"State\":\"Created\",\"CheckItems\":[\"Deprecate old versions after publishing the API\",\"Requires re-subscription when publishing the API\"],\"Transitions\":[{\"Event\":\"Publish\",\"Target\":\"Published\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"}]},{\"State\":\"Prototyped\",\"CheckItems\":[\"Deprecate old versions after publishing the API\",\"Requires re-subscription when publishing the API\"],\"Transitions\":[{\"Event\":\"Publish\",\"Target\":\"Published\"},{\"Event\":\"Demote to Created\",\"Target\":\"Created\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"}]},{\"State\":\"Published\",\"Transitions\":[{\"Event\":\"Block\",\"Target\":\"Blocked\"},{\"Event\":\"Deploy as a Prototype\",\"Target\":\"Prototyped\"},{\"Event\":\"Demote to Created\",\"Target\":\"Created\"},{\"Event\":\"Deprecate\",\"Target\":\"Deprecated\"},{\"Event\":\"Publish\",\"Target\":\"Published\"}]},{\"State\":\"Blocked\",\"Transitions\":[{\"Event\":\"Deprecate\",\"Target\":\"Deprecated\"},{\"Event\":\"Re-Publish\",\"Target\":\"Published\"}]},{\"State\":\"Deprecated\",\"Transitions\":[{\"Event\":\"Retire\",\"Target\":\"Retired\"}]},{\"State\":\"Retired\"}]}}";
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
        PowerMockito.when(APIUtil.class, "getTenantConfig", Mockito.anyString()).thenReturn(jsonObject);
    }

    @Test
    public void testGetAllowedActionsForState() throws Exception {

        prepareGetTenantConfig();
        LCManager LCManager = new LCManager("carbon.super");
        Map<String, String[]> states = new HashMap<>();
        states.put("Created", new String[]{"Publish", "Deploy as a Prototype"});
        states.put("Prototyped", new String[]{"Publish", "Demote to Created"});
        states.put("Published", new String[]{"Block", "Deploy as a Prototype", "Demote to Created", "Deprecate"});
        states.put("Blocked", new String[]{"Deprecate", "Re-Publish"});
        states.put("Deprecated", new String[]{"Retire"});

        for (String state : states.keySet()) {
            Object[] actions = states.get(state);
            Object[] expectedActions = LCManager.getAllowedActionsForState(state.toUpperCase()).toArray();
            Assert.assertArrayEquals(expectedActions, actions);
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(LCManager.getAllowedActionsForState(state.toUpperCase()));
    }

    @Test
    public void testGetTransitionAction() throws Exception {

        prepareGetTenantConfig();
        LCManager LCManager = new LCManager("carbon.super");
        Map<String[], String> actionMap = new HashMap<>();

        //CreatedState  actions
        actionMap.put(new String[]{"Created", "Published"}, "Publish");
        actionMap.put(new String[]{"Created", "Prototyped"}, "Deploy as a Prototype");

        //PrototypedState actions
        actionMap.put(new String[]{"Prototyped", "Published"}, "Publish");
        actionMap.put(new String[]{"Prototyped", "Created"}, "Demote to Created");

        //PublishedState actions
        actionMap.put(new String[]{"Published", "Blocked"}, "Block");
        actionMap.put(new String[]{"Published", "Prototyped"}, "Deploy as a Prototype");
        actionMap.put(new String[]{"Published", "Created"}, "Demote to Created");
        actionMap.put(new String[]{"Published", "Deprecated"}, "Deprecate");

        //BlockedState actions
        actionMap.put(new String[]{"Blocked", "Deprecated"}, "Deprecate");
        actionMap.put(new String[]{"Blocked", "Published"}, "Re-Publish");

        //DeprecatedState actions
        actionMap.put(new String[]{"Deprecated", "Retired"}, "Retire");

        for (String[] state : actionMap.keySet()) {
            String action = actionMap.get(state);
            String expectedAction = LCManager.getTransitionAction(state[0].toUpperCase(), state[1].toUpperCase());
            Assert.assertArrayEquals(new String[]{expectedAction}, new String[]{action});
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(LCManager.getTransitionAction(String.valueOf(new String[]{state, state}), "Block"));

    }

    @Test
    public void testGetCheckListItemsForState() throws Exception {

        prepareGetTenantConfig();
        LCManager LCManager = new LCManager("carbon.super");
        Map<String, String[]> checkListItemMap = new HashMap<>();
        checkListItemMap.put("Created", new String[]{"Deprecate old versions after publishing the API", "Requires re-subscription when publishing the API"});
        checkListItemMap.put("Prototyped", new String[]{"Deprecate old versions after publishing the API", "Requires re-subscription when publishing the API"});

        for (String state : checkListItemMap.keySet()) {
            Object[] checkLists = checkListItemMap.get(state);
            Object[] expectedCheckLists = LCManager.getCheckListItemsForState(state.toUpperCase()).toArray();
            Assert.assertArrayEquals(expectedCheckLists, checkLists);
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(LCManager.getCheckListItemsForState(state.toUpperCase()));
    }

}