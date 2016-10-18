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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.ManagedLifecycleImpl;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.InputBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LifecycleMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LifecycleMgtDBUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test public class LCOperationsTest {

    public static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";
    public static final String ADMIN = "admin";
    public static LifecycleMgtDAO lifecycleMgtDAO;
    public static ManagedLifecycleImpl managedLifecycleImpl;
    private static Log log = LogFactory.getLog(LCOperationsTest.class);

    @BeforeClass protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("LCManagerDBConfigurationPath");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
        initializeDatabase(dbConfigPath);
        LifecycleMgtDBUtil.initialize();
        lifecycleMgtDAO = LifecycleMgtDAO.getInstance();
    }

    private void initializeDatabase(String configFilePath) {

        InputStream in = null;
        try {
            File fXmlFile = new File(configFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            String databaseURL = doc.getElementsByTagName("URL").item(0).getTextContent();
            String databaseUser = doc.getElementsByTagName("Username").item(0).getTextContent();
            String databasePass = doc.getElementsByTagName("Password").item(0).getTextContent();
            String databaseDriver = doc.getElementsByTagName("Driver").item(0).getTextContent();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            try {
                InitialContext.doLookup("jdbc/WSO2LifecycleDB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("jdbc");

                ic.bind("jdbc/WSO2LifecycleDB", basicDataSource);
            }
        } catch (IOException | NamingException | ParserConfigurationException | SAXException e) {
            log.error(e);
        }
    }

    public void testAddLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycle.xml");
        LifecycleUtils.addLifecycle(payload);
        assertTrue(LifecycleUtils.getLifecycleList().length > 0);
        assertNotNull(LifecycleUtils.getLifecycleConfiguration(SERVICE_LIFE_CYCLE));
    }

    public void testAddSameLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycle.xml");
        try {
            LifecycleUtils.addLifecycle(payload);
        } catch (LifecycleException e) {
            assertTrue(e.getMessage().contains("Lifecycle already exist"));
        }
    }

    public void testUpdateLifecycle() throws Exception {
        String payload = readLifecycleFile(
                System.getProperty("LCConfigPath") + File.separator + "ServiceLifeCycleUpdated.xml");
        LifecycleUtils.updateLifecycle(SERVICE_LIFE_CYCLE, payload);
        assertTrue(LifecycleUtils.getLifecycleList().length == 1);
        assertNotNull(LifecycleUtils.getLifecycleConfiguration(SERVICE_LIFE_CYCLE));
        assertTrue(LifecycleUtils.getLifecycleConfiguration(SERVICE_LIFE_CYCLE).contains("transitionPermission"));
    }

    public void testDeleteLifecycle() throws Exception {
        String payload = readLifecycleFile(System.getProperty("LCConfigPath") + File.separator + "APILifeCycle.xml");
        LifecycleUtils.addLifecycle(payload);
        assertTrue(LifecycleUtils.getLifecycleList().length == 2);
        LifecycleUtils.deleteLifecycle("APILifeCycle");
        assertTrue(LifecycleUtils.getLifecycleList().length == 1);
    }

    public void testAssociateLifecycle() throws Exception {
        managedLifecycleImpl = new ManagedLifecycleImpl();
        LifecycleState lifecycleState = managedLifecycleImpl.associateLifecycle(SERVICE_LIFE_CYCLE, ADMIN);
        assertNotNull(lifecycleState.getState());
        assertNotNull(managedLifecycleImpl.getLifecycleID());
    }

    public void testcheckDeletingAssociatedLifecycle() throws Exception {
        try {
            LifecycleUtils.deleteLifecycle(SERVICE_LIFE_CYCLE);
        } catch (LifecycleException e) {
            assertTrue(e.getMessage().contains("is associated with assets"));
        }
    }

    public void testChangeLifecycleState() throws Exception {
        String nextStateString = managedLifecycleImpl.getCurrentLifecycleState().getAvailableTransitionBeanList().get(0)
                .getTargetState();
        // Lets set custom input values as well
        for (InputBean inputBean : managedLifecycleImpl.getCurrentLifecycleState().getInputBeanList()) {
            inputBean.setValues("value 1");
        }
        String action = managedLifecycleImpl.getCurrentLifecycleState().getAvailableTransitionBeanList().get(0)
                .getEvent();
        LifecycleState nextState = new LifecycleState();
        nextState.setState(nextStateString);
        managedLifecycleImpl.executeLifecycleEvent(nextState, action, ADMIN, managedLifecycleImpl);
        assertEquals(managedLifecycleImpl.getCurrentLifecycleState().getState(), nextStateString);

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

}
