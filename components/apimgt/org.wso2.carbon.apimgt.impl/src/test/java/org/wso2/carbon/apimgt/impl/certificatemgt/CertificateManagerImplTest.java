
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
package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.impl.TestUtils;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateAliasExistsException;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.utils.ServerConstants.CARBON_HOME;

/**
 * This class contains unit tests for CertificateManagerImpl class.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateMgtUtils.class, CertificateMgtDAO.class, CarbonContext.class,PrivilegedCarbonContext.class})
public class CertificateManagerImplTest {

    private static CertificateManagerImpl certificateManager;
    private static final String END_POINT = "TEST_ENDPOINT";
    private static final String ALIAS = "TEST_ALIAS_-1234";
    private static final int TENANT_ID = MultitenantConstants.SUPER_TENANT_ID;
    private static final String TEST_PATH = CertificateManagerImplTest.class.getClassLoader().getResource
            ("security/sslprofiles.xml").getPath();
    private static final String TEST_PATH_NOT_EXISTS = "/abx.xml";
    private static final String JAVAX_SSL_TRUST_STORE_PASSWORD = "wso2carbon";
    private static final String JAVAX_NET_SSL_TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    private static final String BASE64_ENCODED_CERT =
            "MIIDEzCCAtGgAwIBAgIEC68tazALBgcqhkjOOAQDBQAwWzELMAkGA1UEBhMCbGsxCzAJBgNVBAgT\r\n" +
                    "AmxrMRAwDgYDVQQHEwdjb2xvbWJvMQ0wCwYDVQQKEwR3c28yMQ0wCwYDVQQLEwR3c28yMQ8wDQYD\r\n" +
                    "VQQDEwZtZW5ha2EwHhcNMTcxMDI2MTA0NzAzWhcNMTgwMTI0MTA0NzAzWjBbMQswCQYDVQQGEwJs\r\n" +
                    "azELMAkGA1UECBMCbGsxEDAOBgNVBAcTB2NvbG9tYm8xDTALBgNVBAoTBHdzbzIxDTALBgNVBAsT\r\n" +
                    "BHdzbzIxDzANBgNVBAMTBm1lbmFrYTCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLf\r\n" +
                    "Spwu7OTn9hG3UjzvRADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQ\r\n" +
                    "paSfn+gEexAiwk+7qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd\r\n" +
                    "0UgBxwIVAJdgUI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlX\r\n" +
                    "TAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6j\r\n" +
                    "fwv6ITVi8ftiegEkO8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDAL5r4\r\n" +
                    "bix3HRG6LkBwAlWZtg+taHxOiLm3NzxMJLEIrJsOZ+ReO31zAO88Wkeibo6ff0D3mFtEdqZwdQDd\r\n" +
                    "zXwljpwk01xW0pg7IxDL/hdeC8jgxlDIB1Zz2NFwjDYeJtw8+l3e5T9c6fG0MsyhOYw3D2zvo66Z\r\n" +
                    "XUHI2Xu3P3ZLhKMhMB8wHQYDVR0OBBYEFOKUFMb/vRAyLr86vxJl0hwmy+jqMAsGByqGSM44BAMF\r\n" +
                    "AAMvADAsAhQW0OvWKXAO5V+37VtaAEX0yAYhgQIUG0q66Btv7Pk/HGGwBnYiHjCpuL4=\r\n";
    static CertificateMgtDAO certificateMgtDAO;

    @BeforeClass
    public static void init() throws XMLStreamException {

        PowerMockito.mockStatic(CertificateMgtDAO.class);
        certificateMgtDAO = Mockito.mock(CertificateMgtDAO.class);
        PowerMockito.when(CertificateMgtDAO.getInstance()).thenReturn(certificateMgtDAO);
        System.setProperty("javax.net.ssl.trustStore",CertificateManagerImplTest.class.getClassLoader().getResource
                ("security/client-truststore.jks").getPath());
        System.setProperty(JAVAX_NET_SSL_TRUST_STORE_PASSWORD_PROPERTY, JAVAX_SSL_TRUST_STORE_PASSWORD);
        TestUtils.initConfigurationContextService(true);
        certificateManager = CertificateManagerImpl.getInstance();
        System.setProperty(CARBON_HOME, "");
        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getTenantDomain()).thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(carbonContext.getTenantId()).thenReturn(MultitenantConstants.SUPER_TENANT_ID);
    }

    @Test
    public void testAddToPublisher() throws CertificateAliasExistsException, CertificateManagementException {

        Mockito.when(certificateMgtDAO.addCertificate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt()))
                .thenReturn(true);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore",String.class,
                String.class))
                .toReturn(ResponseCode.SUCCESS);
        ResponseCode result =
                certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.SUCCESS, result);
    }

    @Test
    public void testAddToPublisherWithInternalServerError() throws CertificateAliasExistsException,
                                                                   CertificateManagementException {

        Mockito.when(certificateMgtDAO.addCertificate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt())).thenReturn(true);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore", String.class,
                String.class))
                .toReturn(ResponseCode.INTERNAL_SERVER_ERROR);
        ResponseCode responseCode = certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS,
                END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testAddToPublisherWithExpiredCertificate()
            throws CertificateAliasExistsException, CertificateManagementException {

        Mockito.when(certificateMgtDAO.addCertificate(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyInt())).thenReturn(true);
        Mockito.when(certificateMgtDAO.deleteCertificate(ALIAS,END_POINT,TENANT_ID)).thenReturn(true);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore",String.class,
                String.class))
                .toReturn(ResponseCode.CERTIFICATE_EXPIRED);
        ResponseCode responseCode = certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS,
                END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.CERTIFICATE_EXPIRED, responseCode);
    }

    @Test
    public void testAddToPublisherWithExistingAlias()
            throws CertificateAliasExistsException, CertificateManagementException {
        Mockito.when(certificateMgtDAO.addCertificate(BASE64_ENCODED_CERT,ALIAS,END_POINT,TENANT_ID)).thenReturn(true);
        Mockito.when(certificateMgtDAO.deleteCertificate(ALIAS,END_POINT,TENANT_ID)).thenReturn(true);
        PowerMockito.stub(PowerMockito
                .method(CertificateMgtUtils.class, "addCertificateToTrustStore", String.class, String.class))
                .toReturn(ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE);
        ResponseCode responseCode = certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS,
                END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE, responseCode);
    }

    @Test
    public void testAddToPublisherWhenDBError() {

        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "addCertificate")).toReturn(false);
        ResponseCode responseCode = certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS,
                END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testAddToPublisherWithExistingAliasInDB()
            throws CertificateManagementException, CertificateAliasExistsException {

        Mockito.when(certificateMgtDAO.addCertificate(BASE64_ENCODED_CERT, ALIAS, END_POINT, TENANT_ID))
                .thenThrow(CertificateAliasExistsException.class);
        ResponseCode responseCode = certificateManager.addCertificateToParentNode(BASE64_ENCODED_CERT, ALIAS,
                END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE, responseCode);
    }

    @Test
    public void testRemoveFromPublisher() throws CertificateManagementException {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.SUCCESS);
        Mockito.when(certificateMgtDAO.deleteCertificate(ALIAS, END_POINT, TENANT_ID)).thenReturn(true);
        List<CertificateMetadataDTO> certificateMetadataDTOList = new ArrayList<>();
        CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
        certificateMetadataDTO.setAlias(ALIAS);
        certificateMetadataDTO.setEndpoint(END_POINT);
        certificateMetadataDTOList.add(certificateMetadataDTO);
        Mockito.when(certificateMgtDAO.getCertificates(ALIAS, null, TENANT_ID)).thenReturn(certificateMetadataDTOList);
        ResponseCode responseCode = certificateManager.deleteCertificateFromParentNode(ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.SUCCESS, responseCode);
    }

    @Test
    public void testRemoveFromPublisherInternalServerError() throws CertificateManagementException {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.SUCCESS);
        Mockito.when(certificateMgtDAO.deleteCertificate(ALIAS, END_POINT, TENANT_ID)).thenReturn(false);
        List<CertificateMetadataDTO> certificateMetadataDTOList = new ArrayList<>();
        CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
        certificateMetadataDTO.setAlias(ALIAS);
        certificateMetadataDTO.setEndpoint(END_POINT);
        certificateMetadataDTOList.add(certificateMetadataDTO);
        Mockito.when(certificateMgtDAO.getCertificates(ALIAS, null, TENANT_ID)).thenReturn(certificateMetadataDTOList);
        ResponseCode responseCode = certificateManager.deleteCertificateFromParentNode(ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testRemoveFromPublisherCertificateNotFound() {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.CERTIFICATE_NOT_FOUND);
        try {
            Mockito.when(certificateMgtDAO.deleteCertificate(ALIAS, END_POINT, TENANT_ID)).thenReturn(true);
            Mockito.when(certificateMgtDAO.addCertificate(BASE64_ENCODED_CERT,ALIAS, END_POINT, TENANT_ID)).thenReturn(true);
        } catch (CertificateManagementException | CertificateAliasExistsException e) {
            e.printStackTrace();
        }
        ResponseCode responseCode = certificateManager.deleteCertificateFromParentNode(ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(ResponseCode.CERTIFICATE_NOT_FOUND, responseCode);
    }

    @Test
    public void testRemoveFromPublisherCertificateManagementException() {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.INTERNAL_SERVER_ERROR);
        try {
            Mockito.when(certificateMgtDAO.deleteCertificate("testRemoveFromPublisherCertificateManagementException",
                    "testRemoveFromPublisherCertificateManagementException", TENANT_ID)).thenReturn(true);
            Mockito.when(certificateMgtDAO.addCertificate(BASE64_ENCODED_CERT,
                    "testRemoveFromPublisherCertificateManagementException",
                    "testRemoveFromPublisherCertificateManagementException", TENANT_ID))
                    .thenThrow(CertificateManagementException.class);
            CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
            certificateMetadataDTO.setEndpoint("testRemoveFromPublisherCertificateManagementException");
            certificateMetadataDTO.setCertificate(BASE64_ENCODED_CERT);
            certificateMetadataDTO.setAlias("testRemoveFromPublisherCertificateManagementException");
            List<CertificateMetadataDTO> certificateMetadataDTOList = new ArrayList<>();
            certificateMetadataDTOList.add(certificateMetadataDTO);
            Mockito.when(certificateMgtDAO
                    .getCertificates("testRemoveFromPublisherCertificateManagementException", null, TENANT_ID))
                    .thenReturn(certificateMetadataDTOList);
        } catch (CertificateManagementException | CertificateAliasExistsException e) {
            e.printStackTrace();
        }
        ResponseCode responseCode = certificateManager
                .deleteCertificateFromParentNode("testRemoveFromPublisherCertificateManagementException",
                        "testRemoveFromPublisherCertificateManagementException", TENANT_ID);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testRemoveFromPublisherWithInternalServerErrorWhenDeleting() {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.INTERNAL_SERVER_ERROR);
        try {
            Mockito.when(certificateMgtDAO
                    .deleteCertificate("testRemoveFromPublisherWithInternalServerErrorWhenDeleting",
                            "testRemoveFromPublisherWithInternalServerErrorWhenDeleting", TENANT_ID)).thenReturn(true);
            Mockito.when(certificateMgtDAO.addCertificate(BASE64_ENCODED_CERT,
                    "testRemoveFromPublisherWithInternalServerErrorWhenDeleting",
                    "testRemoveFromPublisherWithInternalServerErrorWhenDeleting", TENANT_ID)).thenReturn(true);
            CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
            certificateMetadataDTO.setEndpoint("testRemoveFromPublisherWithInternalServerErrorWhenDeleting");
            certificateMetadataDTO.setCertificate(BASE64_ENCODED_CERT);
            certificateMetadataDTO.setAlias("testRemoveFromPublisherWithInternalServerErrorWhenDeleting");
            Mockito.when(certificateMgtDAO.getCertificates("testRemoveFromPublisherWithInternalServerErrorWhenDeleting",
                    null, TENANT_ID)).thenReturn(Arrays.asList(certificateMetadataDTO));

        } catch (CertificateManagementException | CertificateAliasExistsException e) {
            e.printStackTrace();
        }
        ResponseCode responseCode = certificateManager
                .deleteCertificateFromParentNode("testRemoveFromPublisherWithInternalServerErrorWhenDeleting",
                        "testRemoveFromPublisherWithInternalServerErrorWhenDeleting", TENANT_ID);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testAddToGateway() throws IllegalAccessException, NoSuchFieldException {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore",String.class,String.class))
                .toReturn(ResponseCode.SUCCESS);
        boolean result = certificateManager.addCertificateToGateway(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertTrue(result);
    }

    @Test
    public void testAddToGatewayCertificateExistsInTrustStore() throws NoSuchFieldException, IllegalAccessException {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore",String.class,String.class))
                .toReturn(ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE);
        boolean result = certificateManager.addCertificateToGateway(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertTrue(result);
    }

    @Test
    public void testAddToGatewayInternalServerError() throws NoSuchFieldException, IllegalAccessException {

        PowerMockito.stub(PowerMockito
                .method(CertificateMgtUtils.class, "addCertificateToSenderTrustStore", String.class, String.class))
                .toReturn(ResponseCode.INTERNAL_SERVER_ERROR);
        boolean result = certificateManager.addCertificateToGateway(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertFalse(result);
    }

    @Test
    public void testEmptyCertAddToGateway() throws NoSuchFieldException, IllegalAccessException {

        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore("", "testalias");
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode(), responseCode.getResponseCode());
    }


    @Test
    public void testRemoveFromGateway() throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        System.setProperty(CarbonBaseConstants.CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain())
                .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(TENANT_ID);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore",String.class))
                .toReturn(ResponseCode.SUCCESS);
        boolean result = certificateManager.deleteCertificateFromGateway(ALIAS);
        Assert.assertTrue(result);
    }

    @Test
    public void testRemoveFromGatewayIntenalServerError() throws NoSuchFieldException, IllegalAccessException {

        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromSenderTrustStore",String.class))
                .toReturn(ResponseCode.INTERNAL_SERVER_ERROR);
        boolean result = certificateManager.deleteCertificateFromGateway(ALIAS);
        Assert.assertFalse(result);
    }

    @Test
    public void testGetCertificate() throws APIManagementException, CertificateManagementException {

        String alias = "testGetCertificate";
        String endpoint = "testGetCertificate";
        int tenant = 1234;
        CertificateMetadataDTO certificateMetadata = generateMetadata();
        List<CertificateMetadataDTO> certificateMetadataList = new ArrayList<>();
        certificateMetadataList.add(certificateMetadata);
        Mockito.when(certificateMgtDAO.getCertificates(alias, endpoint, tenant)).thenReturn(certificateMetadataList);
        List<CertificateMetadataDTO> result = certificateManager.getCertificates(tenant, alias, endpoint);
        Assert.assertNotNull(result);
        Assert.assertEquals(certificateMetadataList, result);
    }

    @Test
    public void testGetCertificates() {

        List<CertificateMetadataDTO> certificateMetadataList = generateCertificates();
        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "getCertificates", String.class, String.class,
                int.class)).toReturn(certificateMetadataList);
        List<CertificateMetadataDTO> resultMetadataList = certificateManager.getCertificates(TENANT_ID);
        Assert.assertNotNull(resultMetadataList);
    }

    @Test
    public void testIsCertificatePresent() throws APIManagementException, CertificateManagementException {

        CertificateMetadataDTO certificateMetadataDTO = generateMetadata();
        List<CertificateMetadataDTO> certificateMetadataList = new ArrayList<>();
        certificateMetadataList.add(certificateMetadataDTO);

        Mockito.when(certificateMgtDAO.getCertificates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(certificateMetadataList);
        boolean result = certificateManager.isCertificatePresent(TENANT_ID, ALIAS);
        Assert.assertTrue(result);
    }

    @Test
    public void testGetCertificateInformation() throws APIManagementException {

        CertificateInformationDTO certificateInformation = generateCertificateInformationDTO();
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "getCertificateInformation", String.class))
                .toReturn(certificateInformation);

        CertificateInformationDTO certificateInformationDTO = certificateManager.getCertificateInformation(ALIAS);
        Assert.assertNotNull(certificateInformationDTO);

    }

    @Test
    public void testUpdateCertificate() throws APIManagementException {

        PowerMockito
                .stub(PowerMockito.method(CertificateMgtUtils.class, "updateCertificate", String.class, String.class))
                .toReturn(ResponseCode.SUCCESS);

        ResponseCode responseCode = certificateManager.updateCertificate(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertEquals(ResponseCode.SUCCESS.getResponseCode(), responseCode.getResponseCode());
    }

    /**
     * This method tests the behaviour of addClientCertificate method under different conditions.
     */
    @Test
    public void testAddClientCertificate() throws CertificateManagementException {

        PowerMockito
                .stub(PowerMockito.method(CertificateMgtUtils.class, "validateCertificate"))
                .toReturn(ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE);
        ResponseCode responseCode = certificateManager
                .addClientCertificate(null, BASE64_ENCODED_CERT, ALIAS, null, MultitenantConstants.SUPER_TENANT_ID, "org1");
        Assert.assertEquals("Response code was wrong while trying add a client certificate with an existing alias",
                ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode(), responseCode.getResponseCode());
        PowerMockito
                .stub(PowerMockito.method(CertificateMgtUtils.class, "validateCertificate"))
                .toReturn(ResponseCode.SUCCESS);
        Mockito.when(certificateMgtDAO.checkWhetherAliasExist(ALIAS, TENANT_ID)).thenReturn(true);
        responseCode = certificateManager
                .addClientCertificate(null, BASE64_ENCODED_CERT, ALIAS, null, MultitenantConstants.SUPER_TENANT_ID, "org1");
        Assert.assertEquals("Response code was wrong while trying add a client certificate with an existing alias",
                ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode(), responseCode.getResponseCode());
        Mockito.when(certificateMgtDAO.checkWhetherAliasExist(ALIAS, TENANT_ID)).thenReturn(false);
        Mockito.when(certificateMgtDAO
                .addClientCertificate(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
        responseCode = certificateManager
                .addClientCertificate(null, BASE64_ENCODED_CERT, ALIAS, null, MultitenantConstants.SUPER_TENANT_ID, "org1");
        Assert.assertEquals("Response code was wrong while trying add a client certificate",
                ResponseCode.SUCCESS.getResponseCode(), responseCode.getResponseCode());
    }

    /**
     * This method tests the behaviour of updateClientCertificate method.
     */
    @Test
    public void testUpdateClientCertificate() throws APIManagementException {
        String organization = "org1";
        PowerMockito
                .stub(PowerMockito.method(CertificateMgtUtils.class, "validateCertificate"))
                .toReturn(ResponseCode.CERTIFICATE_EXPIRED);
        ResponseCode responseCode = certificateManager
                .updateClientCertificate(BASE64_ENCODED_CERT, ALIAS, null, MultitenantConstants.SUPER_TENANT_ID,
                        organization);
        Assert.assertEquals("Response code was wrong while trying add a expired client certificate",
                ResponseCode.CERTIFICATE_EXPIRED.getResponseCode(), responseCode.getResponseCode());
        PowerMockito
                .stub(PowerMockito.method(CertificateMgtUtils.class, "validateCertificate"))
                .toReturn(ResponseCode.SUCCESS);
        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "updateClientCertificate")).toReturn(false);
        responseCode = certificateManager
                .updateClientCertificate(BASE64_ENCODED_CERT, ALIAS, null, MultitenantConstants.SUPER_TENANT_ID,
                        organization);
        Assert.assertEquals("Response code was wrong, for a failure in update",
                ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode(), responseCode.getResponseCode());
    }

    /**
     * This method tests the behaviour of deleteClientCertificate method.
     */
    @Test
    public void testDeleteClientCertificate() throws CertificateManagementException {

        Mockito.when(certificateMgtDAO
                .deleteClientCertificate(Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(false).thenReturn(true);
        ResponseCode responseCode = certificateManager
                .deleteClientCertificateFromParentNode(null, ALIAS, MultitenantConstants.SUPER_TENANT_ID);
        Assert.assertEquals("Response code was wrong, for a failure in deletion",
                ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode(), responseCode.getResponseCode());
        responseCode = certificateManager
                .deleteClientCertificateFromParentNode(null, ALIAS, MultitenantConstants.SUPER_TENANT_ID);
        Assert.assertEquals("Response code was wrong, for a success in deletion",
                ResponseCode.SUCCESS.getResponseCode(), responseCode.getResponseCode());
    }

    /**
     * This method tests the behaviour of searchClientCertificate method
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testSearchClientCertificate() throws APIManagementException {

        String organization = "org1";
        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "getClientCertificates"))
                .toReturn(new ArrayList<ClientCertificateDTO>());
        Assert.assertNotNull("Client certificate retrieval failed", certificateManager
                .searchClientCertificates(MultitenantConstants.SUPER_TENANT_ID, ALIAS, null, organization));
        Assert.assertNotNull("Client certificate retrieval failed", certificateManager
                .searchClientCertificates(MultitenantConstants.SUPER_TENANT_ID, null, null, organization));
    }

    /**
     * This method tests the behaviour of addClientCertificateToGateway method
     */
    @Test
    public void testAddClientCertificateToGateway() throws NoSuchFieldException, IllegalAccessException {

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore",String.class,String.class))
                .toReturn(ResponseCode.SUCCESS);
        Field field = CertificateManagerImpl.class.getDeclaredField("listenerProfileFilePath");
        field.setAccessible(true);
        field.set(certificateManager, TEST_PATH_NOT_EXISTS);
        boolean result = certificateManager.addClientCertificateToGateway(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertFalse("Client certificate addition to the gateway succeeded with non-existing file", result);

        field.set(certificateManager, TEST_PATH);
        result = certificateManager.addClientCertificateToGateway(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertTrue("Client certificate addition to the gateway failed", result);
    }

    private CertificateMetadataDTO generateMetadata() {

        CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
        certificateMetadataDTO.setAlias(ALIAS);
        certificateMetadataDTO.setEndpoint(END_POINT);
        return certificateMetadataDTO;
    }

    private List<CertificateMetadataDTO> generateCertificates() {

        List<CertificateMetadataDTO> certificateMetadataDTOList = new ArrayList<CertificateMetadataDTO>();
        for (int i = 0; i < 10; i++) {
            CertificateMetadataDTO certificateMetadataDTO = new CertificateMetadataDTO();
            certificateMetadataDTO.setAlias(ALIAS + "_" + i);
            certificateMetadataDTO.setEndpoint(END_POINT + "_" + i);
            certificateMetadataDTOList.add(certificateMetadataDTO);
        }
        return certificateMetadataDTOList;
    }

    private CertificateInformationDTO generateCertificateInformationDTO() {

        CertificateInformationDTO certificateInformationDTO = new CertificateInformationDTO();
        certificateInformationDTO.setStatus("ACTIVE");
        certificateInformationDTO.setVersion("V3");
        certificateInformationDTO.setFrom("");
        certificateInformationDTO.setTo("");
        certificateInformationDTO.setSubject("");
        return certificateInformationDTO;
    }
}
