package org.wso2.carbon.apimgt.persistence.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

public class RegistryLCManagerTestCase {

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
    public void testGetDefaultLCConfigJSON() throws XMLStreamException, ParserConfigurationException, IOException, RegistryException, SAXException, ParseException {
        RegistryLCManager registryLCManager = new RegistryLCManager(-1234);
        JSONParser jsonParser = new JSONParser();
        String LCConfig = String.valueOf(this.LCConfigObj);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(LCConfig);
        Assert.assertEquals(jsonObject, registryLCManager.getDefaultLCConfigJSON());
    }

    @Test
    public void testGetAllowedActionsForState() throws XMLStreamException, ParserConfigurationException, IOException, RegistryException, SAXException {
        RegistryLCManager registryLCManager = new RegistryLCManager(-1234);

        Map<String, String[]> states = new HashMap<>();
        states.put("Created", new String[]{"Publish", "Deploy as a Prototype"});
        states.put("Prototyped",new String[]{"Publish","Demote to Created"});
        states.put("Published",new String[]{"Block","Deploy as a Prototype","Demote to Created","Deprecate"});
        states.put("Blocked",new String[]{"Deprecate","Re-Publish"});
        states.put("Deprecated",new String[]{"Retire"});

        for (String state : states.keySet()){
            Object[] actions = states.get(state);
            Object[] expectedActions = registryLCManager.getAllowedActionsForState(state.toUpperCase()).toArray();
            Assert.assertArrayEquals(expectedActions,actions);
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(registryLCManager.getAllowedActionsForState(state.toUpperCase()));

    }

    @Test
    public void testGetTransitionAction() throws XMLStreamException, ParserConfigurationException, IOException, RegistryException, SAXException {
        RegistryLCManager registryLCManager = new RegistryLCManager(-1234);
        Map<String[], String> actionMap = new HashMap<>();

        //CreatedState  actions
        actionMap.put(new String[]{"Created","Published"},"Publish");
        actionMap.put(new String[]{"Created","Prototyped"},"Deploy as a Prototype");

        //PrototypedState actions
        actionMap.put(new String[]{"Prototyped","Published"},"Publish");
        actionMap.put(new String[]{"Prototyped","Created"},"Demote to Created");

        //PublishedState actions
        actionMap.put(new String[]{"Published","Blocked"},"Block");
        actionMap.put(new String[]{"Published","Prototyped"},"Deploy as a Prototype");
        actionMap.put(new String[]{"Published","Created"},"Demote to Created");
        actionMap.put(new String[]{"Published","Deprecated"},"Deprecate");

        //BlockedState actions
        actionMap.put(new String[]{"Blocked","Deprecated"},"Deprecate");
        actionMap.put(new String[]{"Blocked","Published"},"Re-Publish");

        //DeprecatedState actions
        actionMap.put(new String[]{"Deprecated","Retired"},"Retire");


        for (String[] state : actionMap.keySet()){
            String action = actionMap.get(state);
            String expectedAction = registryLCManager.getTransitionAction(state[0].toUpperCase(),state[1].toUpperCase());
            Assert.assertArrayEquals(new String[]{expectedAction},new String[]{action});
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(registryLCManager.getTransitionAction(String.valueOf(new String[]{state,state}),"Block"));

    }

    @Test
    public void testGetCheckListItemsForState() throws XMLStreamException, ParserConfigurationException, IOException, RegistryException, SAXException {
        RegistryLCManager registryLCManager = new RegistryLCManager(-1234);

        Map<String, String[]> checkListItemMap = new HashMap<>();
        checkListItemMap.put("Created", new String[]{"Deprecate old versions after publishing the API","Requires re-subscription when publishing the API"});
        checkListItemMap.put("Prototyped", new String[]{"Deprecate old versions after publishing the API","Requires re-subscription when publishing the API"});

        for (String state : checkListItemMap.keySet()){
            Object[] checkLists = checkListItemMap.get(state);
            Object[] expectedCheckLists = registryLCManager.getCheckListItemsForState(state.toUpperCase()).toArray();
            Assert.assertArrayEquals(expectedCheckLists,checkLists);
        }

        //Check for returning null line
        String state = "notAState";
        Assert.assertNull(registryLCManager.getCheckListItemsForState(state.toUpperCase()));
    }


}