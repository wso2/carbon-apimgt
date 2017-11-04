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
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.base.MultitenantConstants;

import java.lang.reflect.Field;

/**
 * This class contains unit tests for CertificateManagerImpl class.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateMgtUtils.class, CertificateMgtDAO.class})
public class CertificateManagerImplTest {

    private static CertificateManager certificateManager;
    private static final String END_POINT = "TEST_ENDPOINT";
    private static final String ALIAS = "TEST_ALIAS";
    private static final int TENANT_ID = MultitenantConstants.SUPER_TENANT_ID;
    private static final int TENANT_2 = 1;
    private static final String TEST_PATH = CertificateManagerImplTest.class.getClassLoader().getResource
            ("security/sslprofiles.xml").getPath();
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

    @BeforeClass
    public static void init() {
        MockitoAnnotations.initMocks(CertificateManagerImplTest.class);
        certificateManager = new CertificateManagerImpl();
    }

    @Test
    public void testAddToPublisher() {
        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "addCertificate")).toReturn(true);
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore"))
                .toReturn(ResponseCode.SUCCESS);
        ResponseCode result =
                certificateManager.addCertificateToPublisher(BASE64_ENCODED_CERT, ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(result, ResponseCode.SUCCESS);
    }

    @Test
    public void testRemoveFromPublisher() {
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore"))
                .toReturn(ResponseCode.SUCCESS);
        PowerMockito.stub(PowerMockito.method(CertificateMgtDAO.class, "deleteCertificate"))
                .toReturn(true);
        ResponseCode result = certificateManager.deleteCertificateFromPublisher(ALIAS, END_POINT, TENANT_ID);
        Assert.assertEquals(result, ResponseCode.SUCCESS);
    }

    @Test
    public void testAddToGateway() throws IllegalAccessException, NoSuchFieldException {
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "addCertificateToTrustStore"))
                .toReturn(ResponseCode.SUCCESS);
        Field field = CertificateManagerImpl.class.getDeclaredField("SSL_PROFILE_FILE_PATH");
        field.setAccessible(true);
        field.set(certificateManager, TEST_PATH);
        boolean result = certificateManager.addCertificateToGateways(BASE64_ENCODED_CERT, ALIAS);
        Assert.assertTrue(result);
    }

    @Test
    public void testRemoveFromGateway() throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        PowerMockito.stub(PowerMockito.method(CertificateMgtUtils.class, "removeCertificateFromTrustStore"))
                .toReturn(ResponseCode.SUCCESS);
        Field field = CertificateManagerImpl.class.getDeclaredField("SSL_PROFILE_FILE_PATH");
        field.setAccessible(true);
        field.set(certificateManager, TEST_PATH);
        boolean result = certificateManager.deleteCertificateFromGateways(ALIAS);
        Assert.assertTrue(result);
    }
}
