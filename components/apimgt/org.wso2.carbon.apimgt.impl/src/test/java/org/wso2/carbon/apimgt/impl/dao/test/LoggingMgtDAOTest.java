/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.LoggingMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyManagerHolder.class})
public class LoggingMgtDAOTest {

    public static ApiMgtDAO apiMgtDAO;
    private KeyManager keyManager;

    public static LoggingMgtDAO loggingMgtDAO;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                                                                                       (config));
        APIMgtDBUtil.initialize();
        loggingMgtDAO = LoggingMgtDAO.getInstance();

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
    public void testGetApiLogging() throws Exception {
        List<APILogInfoDTO> apiLogInfoDTOList  = loggingMgtDAO.retrieveAPILoggerByAPIID("org1", "7af95c9d-6177-4191-ab3e-d3f6c1cdc4c2");
        assertTrue(apiLogInfoDTOList.size() == 1);
        assertTrue("OFF".equals(apiLogInfoDTOList.get(0).getLogLevel()));

        apiLogInfoDTOList  = loggingMgtDAO.retrieveAPILoggerByAPIID("org1", "7af95c9d");
        assertTrue(apiLogInfoDTOList.size() == 0);

        String apiId = "7af95c9d-6177-4191-ab3e-d3f6c1cdc4c2";
        loggingMgtDAO.addAPILogger("org1", apiId, "FULL");

        apiLogInfoDTOList = loggingMgtDAO.retrieveAPILoggerList("org1", "FULL");
        assertTrue(apiLogInfoDTOList.size() > 0);
        assertTrue(isContainGivenLoggingAPI(apiLogInfoDTOList, apiId, "FULL"));

        apiLogInfoDTOList = loggingMgtDAO.retrieveAPILoggerList("org1", "FULL");
        assertTrue(apiLogInfoDTOList.size() == 1);
        assertTrue(isContainGivenLoggingAPI(apiLogInfoDTOList, apiId, "FULL"));

        apiLogInfoDTOList  = loggingMgtDAO.retrieveAPILoggerByAPIID("org1", "7af95c9d-6177-4191-ab3e-d3f6c1cdc4c2");
        assertTrue(apiLogInfoDTOList.size() == 1);
        assertTrue("FULL".equals(apiLogInfoDTOList.get(0).getLogLevel()));

        loggingMgtDAO.addAPILogger("org1", apiId, "OFF");

        apiLogInfoDTOList = loggingMgtDAO.retrieveAPILoggerList("org1", "OFF");
        assertTrue(apiLogInfoDTOList.size() > 0);
        assertTrue(isContainGivenLoggingAPI(apiLogInfoDTOList, apiId, "OFF"));

        apiLogInfoDTOList = loggingMgtDAO.retrieveAPILoggerList("org1", "FULL");
        assertTrue(apiLogInfoDTOList.size() == 0);
        assertTrue(!isContainGivenLoggingAPI(apiLogInfoDTOList, apiId, "OFF"));
    }

    public boolean isContainGivenLoggingAPI(List<APILogInfoDTO> apiLogInfoDTOList, String apiId, String logLevel) {
        for (APILogInfoDTO apiLogInfoDTO : apiLogInfoDTOList) {
            if (apiLogInfoDTO.getApiId().equals(apiId) && apiLogInfoDTO.getLogLevel().equals(logLevel)) {
                return true;
            }
        }
        return false;
    }
}
