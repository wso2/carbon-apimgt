/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.lifecycle;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIUtil.class)
public class LCManagerTestCase extends TestCase {

    String LCConfigObj = "{\n" +
            "  \"States\": [\n" +
            "    {\n" +
            "      \"State\": \"Created\",\n" +
            "      \"CheckItems\": [\n" +
            "        \"Deprecate old versions after publishing the API\",\n" +
            "        \"Requires re-subscription when publishing the API\"\n" +
            "      ],\n" +
            "      \"Transitions\": [\n" +
            "        {\n" +
            "          \"Event\": \"Publish\",\n" +
            "          \"Target\": \"Published\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Deploy as a Prototype\",\n" +
            "          \"Target\": \"Prototyped\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"State\": \"Prototyped\",\n" +
            "      \"CheckItems\": [\n" +
            "        \"Deprecate old versions after publishing the API\",\n" +
            "        \"Requires re-subscription when publishing the API\"\n" +
            "      ],\n" +
            "      \"Transitions\": [\n" +
            "        {\n" +
            "          \"Event\": \"Publish\",\n" +
            "          \"Target\": \"Published\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Demote to Created\",\n" +
            "          \"Target\": \"Created\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Deploy as a Prototype\",\n" +
            "          \"Target\": \"Prototyped\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"State\": \"Published\",\n" +
            "      \"Transitions\": [\n" +
            "        {\n" +
            "          \"Event\": \"Block\",\n" +
            "          \"Target\": \"Blocked\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Deploy as a Prototype\",\n" +
            "          \"Target\": \"Prototyped\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Demote to Created\",\n" +
            "          \"Target\": \"Created\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Deprecate\",\n" +
            "          \"Target\": \"Deprecated\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Publish\",\n" +
            "          \"Target\": \"Published\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"State\": \"Blocked\",\n" +
            "      \"Transitions\": [\n" +
            "        {\n" +
            "          \"Event\": \"Deprecate\",\n" +
            "          \"Target\": \"Deprecated\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"Event\": \"Re-Publish\",\n" +
            "          \"Target\": \"Published\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"State\": \"Deprecated\",\n" +
            "      \"Transitions\": [\n" +
            "        {\n" +
            "          \"Event\": \"Retire\",\n" +
            "          \"Target\": \"Retired\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"State\": \"Retired\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";

    @Test
    public void testGetDefaultLCConfigJSON() throws ParseException, IOException, URISyntaxException, APIManagementException {

        LCManager LCManager = new LCManager("carbon.super");
        JSONParser jsonParser = new JSONParser();
        String LCConfig = String.valueOf(this.LCConfigObj);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(LCConfig);
        Assert.assertEquals(jsonObject, LCManager.getDefaultLCConfigJSON());
    }

    public void prepareGetTenantConfig() throws Exception {

        PowerMockito.mockStatic(APIUtil.class);
        File siteConfFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("tenant-conf.json").getFile());
        String tenantConf = FileUtils.readFileToString(siteConfFile);
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(tenantConf);
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