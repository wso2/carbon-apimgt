/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mapstruct.ap.internal.util.Collections;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayArtifactsMgtDAOTest {
    public static GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO;
    public static ApiMgtDAO apiMgtDAO;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties().
                setArtifactSynchronizerDataSource("java:/comp/env/jdbc/WSO2AM_DB");
        GatewayArtifactsMgtDBUtil.initialize();
        gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
        APIMgtDBUtil.initialize();
        apiMgtDAO = ApiMgtDAO.getInstance();
    }


    private static void initializeDatabase(String configFilePath)
            throws XMLStreamException, IOException, NamingException {

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
            throw new XMLStreamException("Unexpected error in processing XML");
        } catch (IOException e) {
            throw new IOException("Error in processing the APIManagerDBConfiguration file ");
        } catch (NamingException e) {
            throw new NamingException("Error in database Username and Password");
        }
    }

    @Test
    public void testAddGatewayAPIArtifactAndMetaData() throws APIManagementException {
        String uuid = UUID.randomUUID().toString();
        String name = "apiname";
        String version = "1.0.0";
        String revision = UUID.randomUUID().toString();
        URL resource = getClass().getClassLoader().getResource("admin-PizzaShackAPI-1.0.0.zip");
        File file = new File(resource.getPath());
        gatewayArtifactsMgtDAO.addGatewayAPIArtifactAndMetaData(uuid, name, version, revision, "carbon.super",
                APIConstants.HTTP_PROTOCOL, file);

        API api = new API(new APIIdentifier("test-provider", name, version));
        api.setContext("/context1");
        api.setContextTemplate("/context1/{version}");
        api.setUUID(uuid);
        apiMgtDAO.addAPI(api, -1234, "testOrg");

        String gatewayAPIId = gatewayArtifactsMgtDAO.getGatewayAPIId(name, version, "carbon.super");
        Assert.assertEquals(gatewayAPIId, uuid);
        Map<String, String> gatewayVhosts = new HashMap<>();
        gatewayVhosts.put("label1", "dev.wso2.com");
        gatewayArtifactsMgtDAO.addAndRemovePublishedGatewayLabels(uuid, revision, Collections.asSet("label1"),
                gatewayVhosts);
        List<APIRuntimeArtifactDto> artifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByAPIIDAndLabel(uuid,
                new String[]{"label1"}, "carbon.super");
        Assert.assertEquals(artifacts.size(), 1);
        RuntimeArtifactDto artifact = artifacts.get(0);
        Assert.assertNotNull(artifact);
        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revision);
        apiRevisionDeployment.setDeployment("label1");
        gatewayVhosts = new HashMap<>();
        gatewayVhosts.put("label2", "prod.wso2.com");
        gatewayArtifactsMgtDAO.addAndRemovePublishedGatewayLabels(uuid, revision, Collections.asSet("label2"),
                gatewayVhosts, Collections.asSet(apiRevisionDeployment));
        artifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByAPIIDAndLabel(uuid, new String[]{"label1"}, "carbon.super");
        Assert.assertEquals(artifacts.size(), 0);
        artifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByAPIIDAndLabel(uuid, new String[]{"label2"},
                "carbon.super");
        Assert.assertEquals(artifacts.size(), 1);
        artifact = artifacts.get(0);
        Assert.assertNotNull(artifact);
    }

    @Test
    public void testGetAPIContextForMetaData() throws APIManagementException{
        String uuid = UUID.randomUUID().toString();
        String name = "contextapiname";
        String version = "1.0.0";
        String revision = UUID.randomUUID().toString();
        String context = "/context2";
        URL resource = getClass().getClassLoader().getResource("admin-PizzaShackAPI-1.0.0.zip");
        File file = new File(resource.getPath());
        gatewayArtifactsMgtDAO.addGatewayAPIArtifactAndMetaData(uuid, name, version, revision, "carbon.super",
                APIConstants.HTTP_PROTOCOL, file);

        API api = new API(new APIIdentifier("test-provider", name, version));
        api.setContext(context);
        api.setContextTemplate("/context2/{version}");
        api.setUUID(uuid);
        apiMgtDAO.addAPI(api, -1234, "testOrg");

        Map<String, String> gatewayVhosts = new HashMap<>();
        gatewayVhosts.put("label1", "dev.wso2.com");
        gatewayArtifactsMgtDAO.addAndRemovePublishedGatewayLabels(uuid, revision, Collections.asSet("label1"),
                gatewayVhosts);
        List<APIRuntimeArtifactDto> artifacts = gatewayArtifactsMgtDAO.retrieveGatewayArtifactsByAPIIDAndLabel(uuid,
                new String[]{"label1"}, "carbon.super");
        Assert.assertEquals(artifacts.size(), 1);
        RuntimeArtifactDto artifact = artifacts.get(0);
        Assert.assertNotNull(artifact);
        Assert.assertEquals(context, artifacts.get(0).getContext());
    }
}
