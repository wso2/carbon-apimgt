/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.constants.TestConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.InputBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LifecycleMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LifecycleMgtDBUtil;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
public class LifecycleOperationsTest {


    public static LifecycleMgtDAO lifecycleMgtDAO;
    public static SampleAPI sampleAPI;
    private static Logger  log = LoggerFactory.getLogger(LifecycleOperationsTest.class);

    @BeforeClass
    protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("LCManagerDBConfigurationPath");
        setupInitialContext(dbConfigPath);
        LifecycleMgtDBUtil.initialize();
        lifecycleMgtDAO = LifecycleMgtDAO.getInstance();
    }

    private static void setupInitialContext(String configFilePath) {
        try {
            NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {

                @Override public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment)
                        throws NamingException {
                    return new InitialContextFactory() {

                        @Override public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
                            return new InitialContext() {

                                private Hashtable<String, HikariDataSource> dataSources = new Hashtable<>();

                                @Override public Object lookup(String name) throws NamingException {

                                    if (dataSources.isEmpty()) { //init datasources
                                        try {
                                            File fXmlFile = new File(configFilePath);
                                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                            DocumentBuilder dBuilder = null;
                                            dBuilder = dbFactory.newDocumentBuilder();
                                            Document doc = null;
                                            doc = dBuilder.parse(fXmlFile);

                                            String databaseURL = doc.getElementsByTagName("URL").item(0)
                                                    .getTextContent();
                                            String databaseUser = doc.getElementsByTagName("Username").item(0)
                                                    .getTextContent();
                                            String databasePass = doc.getElementsByTagName("Password").item(0)
                                                    .getTextContent();
                                            String databaseDriver = doc.getElementsByTagName("Driver").item(0)
                                                    .getTextContent();

                                            HikariDataSource basicDataSource = new HikariDataSource();
                                            basicDataSource.setDriverClassName(databaseDriver);
                                            basicDataSource.setJdbcUrl(databaseURL);
                                            basicDataSource.setUsername(databaseUser);
                                            basicDataSource.setPassword(databasePass);
                                            dataSources.put("java:comp/env/jdbc/WSO2LifecycleDB", basicDataSource);
                                        } catch (IOException | ParserConfigurationException | SAXException e) {
                                            log.error("Error while setting datasource properties.", e);
                                        }

                                        //add more datasources to the list as necessary
                                    }

                                    if (dataSources.containsKey(name)) {
                                        return dataSources.get(name);
                                    }

                                    throw new NamingException("Unable to find datasource: " + name);
                                }
                            };
                        }

                    };
                }

            });
        } catch (NamingException e) {
            log.error("Error while setting initial context" + e);
        }
    }

    public void testAddLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycle.xml");
        LifecycleUtils.addLifecycle(payload);
        assertTrue(LifecycleUtils.getLifecycleList().length > 0);
        assertNotNull(LifecycleUtils.getLifecycleConfiguration(TestConstants.SERVICE_LIFE_CYCLE));
    }

    @Test(dependsOnMethods = "testAddLifecycle")
    public void testAddSameLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycle.xml");
        try {
            LifecycleUtils.addLifecycle(payload);
        } catch (LifecycleException e) {
            assertTrue(e.getMessage().contains("Lifecycle already exist"));
        }
    }

    @Test(dependsOnMethods = "testAddLifecycle")
    public void testUpdateLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycleUpdated.xml");
        LifecycleUtils.updateLifecycle(TestConstants.SERVICE_LIFE_CYCLE, payload);
        assertTrue(LifecycleUtils.getLifecycleList().length == 1);
        assertNotNull(LifecycleUtils.getLifecycleConfiguration(TestConstants.SERVICE_LIFE_CYCLE));
        assertTrue(LifecycleUtils.getLifecycleConfiguration(TestConstants.SERVICE_LIFE_CYCLE).contains
                ("transitionPermission"));
    }

    @Test(dependsOnMethods = "testAddLifecycle")
    public void testDeleteLifecycle() throws Exception {
        String payload = readLifecycleFile(System.getProperty("LCConfigPath") + File.separator + "APILifeCycle.xml");
        LifecycleUtils.addLifecycle(payload);
        assertTrue(LifecycleUtils.getLifecycleList().length == 2);
        LifecycleUtils.deleteLifecycle("APILifeCycle");
        assertTrue(LifecycleUtils.getLifecycleList().length == 1);
    }

    @Test(dependsOnMethods = "testUpdateLifecycle")
    public void testAssociateLifecycle() throws Exception {
        sampleAPI = createSampleAPI();
        sampleAPI.associateLifecycle(TestConstants.SERVICE_LIFE_CYCLE,
                TestConstants.ADMIN);
        assertNotNull(sampleAPI.getLifecycleState().getState());
        assertNotNull(sampleAPI.getLifecycleState().getLifecycleId());
    }

    @Test(dependsOnMethods = "testAssociateLifecycle")
    public void testCheckDeletingAssociatedLifecycle() throws Exception {
        try {
            LifecycleUtils.deleteLifecycle(TestConstants.SERVICE_LIFE_CYCLE);
        } catch (LifecycleException e) {
            assertTrue(e.getMessage().contains("is associated with assets"));
        }
    }

    @Test(dependsOnMethods = "testAssociateLifecycle")
    public void testChangeLifecycleState() throws Exception {
        LifecycleState currentState = sampleAPI.getLifecycleState();
        String nextStateString = currentState.getAvailableTransitionBeanList().get(0)
                .getTargetState();
        // Lets set custom input values as well
        for (InputBean inputBean : currentState.getInputBeanList()) {
            inputBean.setValues("value 1");
        }
        String action = currentState.getAvailableTransitionBeanList().get(0)
                .getEvent();
        LifecycleState nextState = new LifecycleState();
        nextState.setState(nextStateString);
        sampleAPI.executeLifecycleEvent(nextState, currentState.getLifecycleId(), action, TestConstants.ADMIN,
                sampleAPI);
        assertEquals(sampleAPI.getLifecycleState().getState(), nextStateString);

    }

    @Test(dependsOnMethods = "testAssociateLifecycle")
    public void testDissociateLifecycle() throws Exception {
        String uuid = sampleAPI.getLifecycleState().getLifecycleId();
        sampleAPI.dissociateLifecycle(uuid);
        try {
            sampleAPI.getCurrentLifecycleState(uuid);
        } catch (LifecycleException e) {
            assertTrue(e.getMessage().contains("Error while getting lifecycle data for id"));
        }
    }

    private String readLifecycleFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        }

        return sb.toString();
    }

    private SampleAPI createSampleAPI() {
        SampleAPI sampleAPI = new SampleAPI();
        sampleAPI.setName("API 1");
        sampleAPI.setVersion("1.0.0");
        return sampleAPI;
    }

}
