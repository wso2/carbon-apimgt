/*
 *
 *  Copyright (c) 2022, WSO2 LLC (http://www.wso2.com)
 *
 *  WSO2 LLC licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.CorrelationConfigDAO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

@RunWith(PowerMockRunner.class)
public class CorrelationConfigDAOTest{

    public static CorrelationConfigDAO correlationConfigDAO;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        APIMgtDBUtil.initialize();
        correlationConfigDAO = CorrelationConfigDAO.getInstance();
    }

    private static void initializeDatabase(String configFilePath) {

        InputStream in;
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
                InitialContext.doLookup("java:/comp/env/jdbc/WSO2AM_DB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:/comp");
                ic.createSubcontext("java:/comp/env");
                ic.createSubcontext("java:/comp/env/jdbc");

                ic.bind("java:/comp/env/jdbc/WSO2AM_DB", basicDataSource);
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCorrelationConfigDAO() throws Exception{

        boolean isConfigExist = true;
        // Test the isConfigExits() method
        isConfigExist = correlationConfigDAO.isConfigExist();
        Assert.assertFalse(isConfigExist);

        correlationConfigDAO.addDefaultCorrelationConfigs();
        // Trying to insert default configurations again to check it throws an error
        correlationConfigDAO.addDefaultCorrelationConfigs();
        isConfigExist = correlationConfigDAO.isConfigExist();
        Assert.assertTrue(isConfigExist);

        // Test the addDefaultCorrelationConfigs() method
        List<CorrelationConfigDTO> correlationConfigsList = correlationConfigDAO.getCorrelationConfigsList();
        Assert.assertEquals(5, correlationConfigsList.size());
        String[] correlationComponents = { "http", "ldap", "synapse", "jdbc", "method-calls"};
        for(CorrelationConfigDTO correlationConfigDTO: correlationConfigsList) {
            Assert.assertTrue(Arrays.stream(correlationComponents).anyMatch(s -> s.equals(correlationConfigDTO.getName())));
            Assert.assertFalse(Boolean.parseBoolean(correlationConfigDTO.getEnabled()));

            if (correlationConfigDTO.getName().equals("jdbc")) {
                Assert.assertNotNull(correlationConfigDTO.getProperties());
                Assert.assertTrue(correlationConfigDTO.getProperties().size() > 0);
                String[] deniedThreads = correlationConfigDTO.getProperties().get(0).getValue();
                Assert.assertTrue(deniedThreads.length == 4);
            }
        }

        // Test the updateCorrelationConfigs() method
        for (int i = 0; i < 5; i++) {
            correlationConfigsList.get(i).setEnabled("true");
        }
        correlationConfigDAO.updateCorrelationConfigs(correlationConfigsList);
        correlationConfigsList = correlationConfigDAO.getCorrelationConfigsList();
        Assert.assertEquals(5, correlationConfigsList.size());
        for(CorrelationConfigDTO correlationConfigDTO: correlationConfigsList) {
            Assert.assertTrue(Arrays.stream(correlationComponents).anyMatch(s -> s.equals(correlationConfigDTO.getName())));
            Assert.assertTrue(Boolean.parseBoolean(correlationConfigDTO.getEnabled()));

            if (correlationConfigDTO.getName().equals("jdbc")) {
                Assert.assertNotNull(correlationConfigDTO.getProperties());
                Assert.assertTrue(correlationConfigDTO.getProperties().size() > 0);
                String[] deniedThreads = correlationConfigDTO.getProperties().get(0).getValue();
                Assert.assertTrue(deniedThreads.length == 4);
            }
        }
    }

}