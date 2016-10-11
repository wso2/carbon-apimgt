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

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LCUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifeCycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.ManagedLifeCycle;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LCMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LCMgtDBUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class LCOperationsTest extends TestCase {

    public static LCMgtDAO lcMgtDAO;
    public static ManagedLifeCycle managedLifeCycle;
    private static boolean setUpIsDone = false;

    protected void setUp() throws Exception {
        if(setUpIsDone)
            return;
        String dbConfigPath = System.getProperty("LCManagerDBConfigurationPath");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
        initializeDatabase  (dbConfigPath);
        LCMgtDBUtil.initialize();
        lcMgtDAO = LCMgtDAO.getInstance();
        setUpIsDone = true;
    }

    private void initializeDatabase(String configFilePath) {

        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName")).
                    getText();
            OMElement databaseElement = builder.getDocumentElement().getFirstChildWithName(new QName("Database"));
            String databaseURL = databaseElement.getFirstChildWithName(new QName("URL")).getText();
            String databaseUser = databaseElement.getFirstChildWithName(new QName("Username")).getText();
            String databasePass = databaseElement.getFirstChildWithName(new QName("Password")).getText();
            String databaseDriver = databaseElement.getFirstChildWithName(new QName("Driver")).getText();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
            try {
                InitialContext.doLookup("jdbc/WSO2LifecycleDB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("jdbc");

                ic.bind("jdbc/WSO2LifecycleDB", basicDataSource);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void test1AddLifeCycle() throws Exception{
        String payload = readLifecycleFile(System.getProperty("LCConfigPath")+File.separator + "ServiceLifeCycle.xml" );
        LCUtils.addLifecycle(payload);
        assertTrue(LCUtils.getLifeCycleList().length>0);
        assertNotNull(LCUtils.getLifecycleConfiguration("ServiceLifeCycle"));
    }

    public void test2AddSameLifeCycle() throws Exception{
        String payload = readLifecycleFile(System.getProperty("LCConfigPath")+File.separator + "ServiceLifeCycle.xml" );
        try {
            LCUtils.addLifecycle(payload);
        } catch (LifeCycleException e) {
            assertTrue(e.getMessage().contains("Lifecycle already exist"));
        }

    }

    public void test3AssociateLifeCycle () throws Exception{
        managedLifeCycle = new ManagedLifeCycle();
        LifeCycleState lifeCycleState = managedLifeCycle.associateLifecycle("ServiceLifeCycle");
        assertNotNull(lifeCycleState.getState());
        assertNotNull(managedLifeCycle.getLifeCycleID());
    }

    public void test4ChangeLifeCycleState () throws Exception{
        String nextStateString = managedLifeCycle.getCurrentLifecycleState().getAvailableTransitionBeanList().get(0)
                .getTargetState();
        String action = managedLifeCycle.getCurrentLifecycleState().getAvailableTransitionBeanList().get(0).getEvent();
        LifeCycleState nextState= new LifeCycleState();
        nextState.setState(nextStateString);
        managedLifeCycle.executeLifeCycleEvent(nextState,action,new Object());
        assertEquals(managedLifeCycle.getCurrentLifecycleState().getState(),nextStateString);

    }

    private String readLifecycleFile(String path) throws IOException{
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        }

        return sb.toString();
    }


}
