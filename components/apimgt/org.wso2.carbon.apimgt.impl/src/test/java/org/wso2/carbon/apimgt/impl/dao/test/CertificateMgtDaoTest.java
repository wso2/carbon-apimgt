/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManagerDatabaseException;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.EndpointForCertificateExistsException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * This method holds test cases for the class CertificateMgtDAO.
 * Junit @FixMethodOrder is used to order the test methods, so that the method names are in lexicographic order.
 * https://github.com/junit-team/junit4/blob/master/doc/ReleaseNotes4.11.md#test-execution-order
 */
public class CertificateMgtDaoTest {

    private static CertificateMgtDAO certificateMgtDAO;
    private static String TEST_ALIAS = "test alias";
    private static String TEST_ALIAS_2 = "test alias 2";
    private static String TEST_ENDPOINT = "test end point";
    private static String TEST_ENDPOINT_2 = "test end point 2";
    private static int TENANT_ID = MultitenantConstants.SUPER_TENANT_ID;
    private static final int TENANT_2 = 1001;
    private static final String certificate =
            "MIIDPTCCAiWgAwIBAgIETWBSTzANBgkqhkiG9w0BAQsFADBOMQswCQYDVQQGEwJsazELMAkGA1UECBMCbGsxCz" +
                    "AJBgNVBAcTAmxrMQswCQYDVQQKEwJsazELMAkGA1UECxMCbGsxCzAJBgNVBAMTAmxrMCAXDTE4MDEy" +
                    "NTExNDY1NloYDzMwMTcwNTI4MTE0NjU2WjBOMQswCQYDVQQGEwJsazELMAkGA1UECBMCbGsxCzAJBg" +
                    "NVBAcTAmxrMQswCQYDVQQKEwJsazELMAkGA1UECxMCbGsxCzAJBgNVBAMTAmxrMIIBIjANBgkqhkiG" +
                    "9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxLw0sVn/HP3i/5Ghp9vy0OnCs0LEJUAvjndi/Gq+ZRw7HLCVvZ" +
                    "kZc896Kdn2k/9zdmtUptAmXswttCt6cFMIMbeMi2qeCbmPM+WXgm0Ngw+XbBL4qsyvCfnGp7d2i+Qz" +
                    "7x1rm6cb4WGScTdRHXC9EsUGEvotmn2w8g4ksZx/1bR1D/2IZ5BL4G/4kfVcOnPXXXq2IwjVzVUWrc" +
                    "q+fZxAo2iJ2VzGh8vfyNj9Z97Q5ey+Nreqw5HAiPjBcnD8TrbKYfn6tQTTVg8AaY97SXC/AwSvtgvD" +
                    "PMTNNbE5c4JLo+/CeL5d6e6/qsolFpDJUfKES4Gp8MTDlwA3YF8/r0OrHQIDAQABoyEwHzAdBgNVHQ" +
                    "4EFgQU5ZqqRPSTyT8ESAE3keTFMDQqG7owDQYJKoZIhvcNAQELBQADggEBAAL/i00VjPx9BtcUYMN6" +
                    "hJX5cVhbvUBNzuWy+FVk3m3FfRgjXdrWhIRHXVslo/NOoxznd5TGD0GYiBuPtPEG+wYzNgpEbdKrcs" +
                    "M1+YkZVvoon8rItY2vTC57uch/EulrKIeNiYeLxtKNgXpvvAYC0HPtKB/aiC7Vc0gH0JVNrJNah9Db" +
                    "d7HmgeAeiDPvUpZWSvuJPg81G/rC1Gu9yFuiR8HjzcTDRVMepkefA3IpHwYvoQGjeNC/GFGAH/9jih" +
                    "rqw8anwwPALocNSvzwB148w/viIOaopfrmMqBlBWAwUf2wYCU6W3rhhg7H6Zf2cTweLe4v57GVlOWt" +
                    "YOXlgJzeUuc=";
    private APIIdentifier apiIdentifier = new APIIdentifier("CERTIFICATE", "API1", "1.0.0");

    @Before
    public void setUp() throws APIManagerDatabaseException, APIManagementException, SQLException,
            XMLStreamException, IOException, NamingException {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl
                (config));
        APIMgtDBUtil.initialize();
        certificateMgtDAO = CertificateMgtDAO.getInstance();
    }

    private static void initializeDatabase(String configFilePath) throws IOException, XMLStreamException, NamingException {

        InputStream in;
        in = FileUtils.openInputStream(new File(configFilePath));
        StAXOMBuilder builder = new StAXOMBuilder(in);
        String dataSource = builder.getDocumentElement().getFirstChildWithName(new QName("DataSourceName"))
                .getText();
        OMElement databaseElement = builder.getDocumentElement()
                .getFirstChildWithName(new QName("Database"));
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
    }

    @Test
    public void testIsTableExists() throws CertificateManagementException {
        Assert.assertTrue(certificateMgtDAO.isTableExists());
    }

    @Test
    public void testGetCertificateWithNoCertificate() throws CertificateManagementException {
        List<CertificateMetadataDTO> certificateDTO =
                certificateMgtDAO.getCertificates(TEST_ALIAS_2, TEST_ENDPOINT_2, TENANT_ID);
        Assert.assertEquals(0, certificateDTO.size());
    }

    @Test
    public void testAddCertificate() throws CertificateManagementException, CertificateAliasExistsException,
            EndpointForCertificateExistsException, APIManagementException {
        boolean result = certificateMgtDAO.addCertificate(TEST_ALIAS, TEST_ENDPOINT, TENANT_ID);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetCertificate() throws CertificateManagementException {
        List<CertificateMetadataDTO> certificateDTO =
                certificateMgtDAO.getCertificates("ALIAS_1", "EP_1", TENANT_2);
        Assert.assertNotNull(certificateDTO);
    }

    @Test
    public void testDeleteCertificate() throws CertificateManagementException {
        boolean result = certificateMgtDAO.deleteCertificate("ALIAS2", "EP2", TENANT_ID);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetCertificates() throws CertificateManagementException {
        List<CertificateMetadataDTO> certificates = certificateMgtDAO.getCertificates(null, null, TENANT_ID);
        Assert.assertNotNull(certificates);
        Assert.assertTrue(certificates.size() > 0);
    }

    @Test(expected = CertificateAliasExistsException.class)
    public void testAddCertificateForExistingAlias() throws CertificateManagementException,
            CertificateAliasExistsException, EndpointForCertificateExistsException {
        certificateMgtDAO.addCertificate("ALIAS4", "EP4", TENANT_ID);
    }

    @Test
    public void testAddCertificateForExistingEndpoint() throws CertificateManagementException,
            CertificateAliasExistsException, EndpointForCertificateExistsException {
        // add certificate
        certificateMgtDAO.addCertificate("ALIAS_WSO2_1", "http://wso2.com", TENANT_2);
        // add same cert for endpoint with differnt alias
        Assert.assertTrue(certificateMgtDAO.addCertificate("ALIAS_WSO2_2", "http://wso2.com", TENANT_2));
    }

    /**
     * This method tests the behaviour of addClientCertificate method.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testAddClientCertificate() throws CertificateManagementException {
        try {
            Assert.assertTrue("Client certificate addition failed ", addClientCertificate());
        } finally {
            deleteClientCertificate();
        }
    }

    /**
     * This method tests the behaviour of updateClientCertificate method when trying to update a non-existing alias.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testUpdateClientCertificateOfNonExistingAlias() throws CertificateManagementException {
        Assert.assertFalse("Update of client certificate for a non existing alias succeeded",
                certificateMgtDAO.updateClientCertificate(certificate, "test1", "test", TENANT_ID));
    }

    /**
     * This method tests the behaviour of updateClientCertificate method when trying to update a non-existing alias.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testUpdateClientCertificateOfExistingAlias() throws CertificateManagementException {
        try {
            addClientCertificate();
            Assert.assertTrue("Update of client certificate for an existing alias failed",
                    certificateMgtDAO.updateClientCertificate(null, "test", "test", TENANT_ID));
        } finally {
            deleteClientCertificate();
        }
    }

    /**
     * This method tests ths behaviour of getClientCertificateCount method.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testDeleteClientCertificate() throws CertificateManagementException {
        addClientCertificate();
        Assert.assertTrue("Deletion of client certificate failed", deleteClientCertificate());
    }

    /**
     * This method tests the behaviour of getClientCertificateCount method of the CertificateMgtDAO.
     * @throws CertificateManagementException Certificate Management Exception
     */
    @Test
    public void testGetClientCertificateCount() throws CertificateManagementException {
        addClientCertificate();
        Assert.assertEquals("The expected client certificate count does not match with the retrieved count", 1,
                certificateMgtDAO.getClientCertificateCount(TENANT_ID));
        deleteClientCertificate();
        Assert.assertEquals("The expected client certificate count does not match with the retrieved count", 0,
                certificateMgtDAO.getClientCertificateCount(TENANT_ID));
    }

    /**
     * This method tests whether checkWhetherAliasExist method works as expected.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testCheckWhetherAliasExist() throws CertificateManagementException {
        Assert.assertFalse("The non-existing alias was detected as exist",
                certificateMgtDAO.checkWhetherAliasExist("test", MultitenantConstants.SUPER_TENANT_ID));
        addClientCertificate();
        Assert.assertTrue("The existing alias was detected as notexist",
                certificateMgtDAO.checkWhetherAliasExist("test", MultitenantConstants.SUPER_TENANT_ID));
        deleteClientCertificate();
    }

    /**
     * This method tests the behaviour of getClientCertificates method.
     *
     * @throws CertificateManagementException Certificate Management Exception
     */
    @Test
    public void testGetClientCertificates() throws CertificateManagementException {
        List<ClientCertificateDTO> clientCertificateDTOS = certificateMgtDAO
                .getClientCertificates(TENANT_ID, null, null);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 0,
                clientCertificateDTOS.size());
        addClientCertificate();
        clientCertificateDTOS = certificateMgtDAO.getClientCertificates(TENANT_ID, null, null);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 1,
                clientCertificateDTOS.size());
        clientCertificateDTOS = certificateMgtDAO.getClientCertificates(TENANT_ID, "test", null);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 1,
                clientCertificateDTOS.size());
        clientCertificateDTOS = certificateMgtDAO.getClientCertificates(TENANT_ID, "test1", null);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 0,
                clientCertificateDTOS.size());

        clientCertificateDTOS = certificateMgtDAO.getClientCertificates(TENANT_ID, "test", apiIdentifier);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 1,
                clientCertificateDTOS.size());
        clientCertificateDTOS = certificateMgtDAO.getClientCertificates(TENANT_ID, null, apiIdentifier);
        Assert.assertEquals("The client certificate DTO list that matches the search criteria is not returned", 1,
                clientCertificateDTOS.size());
        deleteClientCertificate();
    }

    /**
     * To test the behaviour of the getDeletedClientCertificateAlias method.
     *
     * @throws CertificateManagementException Certificate Management Exception.
     */
    @Test
    public void testGetDeletedClientCertificates() throws CertificateManagementException {
        certificateMgtDAO.updateRemovedCertificatesFromGateways(apiIdentifier, TENANT_ID);
        List<String> aliasList = certificateMgtDAO.getDeletedClientCertificateAlias(apiIdentifier, TENANT_ID);
        Assert.assertEquals("The number of deleted certificates retrieved was wrong", 0, aliasList.size());
        addClientCertificate();
        aliasList = certificateMgtDAO.getDeletedClientCertificateAlias(apiIdentifier, TENANT_ID);
        Assert.assertEquals("The number of deleted certificates retrieved was wrong", 0, aliasList.size());
        deleteClientCertificate();
        aliasList = certificateMgtDAO.getDeletedClientCertificateAlias(apiIdentifier, TENANT_ID);
        Assert.assertEquals("The number of deleted certificates retrieved was wrong", 1, aliasList.size());
    }

    @Test
    public void getCertificate() throws CertificateManagementException, CertificateAliasExistsException {
        certificateMgtDAO.addCertificate("AliasOne", "http://localhost", -1234);
        certificateMgtDAO.addCertificate("AliasTwo", "http://localhost/abc/123", -1234);
        certificateMgtDAO.addCertificate("AliasThree", "testing string", -1234);
        List<CertificateMetadataDTO> certs = certificateMgtDAO.getCertificates("AliasOne", "http://localhost", -1234);
        CertificateMetadataDTO certOne = certs.get(0);
        CertificateMetadataDTO certTwo = certs.get(1);
        Assert.assertEquals("Endpoint one does not retrieved", "http://localhost", certOne.getEndpoint());
        Assert.assertEquals("Endpoint two does not retrieved", "http://localhost/abc/123", certTwo.getEndpoint());
        List<CertificateMetadataDTO> certs2 = certificateMgtDAO.getCertificates(null, "testing string", -1234);
        CertificateMetadataDTO certThree = certs2.get(0);
        Assert.assertEquals("Endpoint three does not retrieved", "testing string", certThree.getEndpoint());
    }

    /**
     * To add the client certificate.
     *
     * @return true if the addition is successful, otherwise false.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private boolean addClientCertificate() throws CertificateManagementException {
        return certificateMgtDAO.addClientCertificate(certificate, apiIdentifier, "test", "Gold", TENANT_ID, null);
    }

    /**
     * To delete the client certificate.
     *
     * @return trye if deletion is successful, otherwise false.
     * @throws CertificateManagementException Certificate Management Exception.
     */
    private boolean deleteClientCertificate() throws CertificateManagementException {
        return certificateMgtDAO.deleteClientCertificate(apiIdentifier, "test", TENANT_ID, null);
    }

}
