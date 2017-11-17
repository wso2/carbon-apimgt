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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;

import java.lang.reflect.Field;
import java.net.URL;

/**
 * This class contains the unit tests for CertificateMgtUtil class.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CertificateMgtUtilTest {

    private static CertificateMgtUtils certificateMgtUtils;
    private static final String ALIAS = "TEST_ALIAS";
    private static final URL CERT_PATH = CertificateMgtUtilTest.class.getClassLoader().getResource
            ("security/client-truststore.jks");
    private static final String ALIAS_NOT_EXIST = "TEST_ALIAS_NOT";
    private static final String ALIAS_EXPIRED = "TEST_ALIAS_EXPIRED";
    private static final String TRUST_STORE_FIELD = "TRUST_STORE";
    private static final String INVALID_TRUST_STORE_FILE = "/abc.jks";
    private static final String BASE64_ENCODED_CERT_STRING =
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

    private static final String BASE64_ENCODED_ERROR_CERT =
            "U3lzdGVtLnNldFByb3BlcnR5KCJqYXZheC5uZXQuc3NsLnRydXN0U3RvcmV" +
                    "QYXNzd29yZCIsICJ3c28yY2FyYm9uIik7DQogICAgICAgIFVSTCBjZXJ0UGF0aCA9IENlcnRpZmlj" +
                    "YXRlTWd0VXRpbFRlc3QuY2xhc3MuZ2V0Q2xhc3NMb2FkZXIoKS5nZXRSZXNvdXJjZSgic" +
                    "2VjdXJpdHkvY2xpZW50LXRydXN0c3RvcmUuamtzIik7DQogICAgICAgIFN5c3RlbS5zZXR" +
                    "Qcm9wZXJ0eSgiamF2YXgubmV0LnNzbC50cnVzdFN0b3JlIiwgY2VydFBhdGguZ2V0UGF0a" +
                    "CgpKTsNCiAgICAgICAgY2VydGlmaWNhdGVNZ3RVdGlscyA9IG5ldyBDZXJ0aWZpY2F0ZU1" +
                    "ndFV0aWxzKCk7";

    private static final String EXPIRED_CERTIFICATE =
            "MIIDbTCCAlWgAwIBAgIEHH2kwDANBgkqhkiG9w0BAQsFADBnMQswCQYDVQQGEwJsazEQMA4GA1UE\n" +
                    "CBMHY29sb21ibzEQMA4GA1UEBxMHd2VzdGVybjENMAsGA1UEChMEd3NvMjENMAsGA1UECxMEd3Nv\n" +
                    "MjEWMBQGA1UEAxMNMTkyLjE2OC4xLjEwODAeFw0xNzExMTAxNDI1MDlaFw0xNzExMTExNDI1MDla\n" +
                    "MGcxCzAJBgNVBAYTAmxrMRAwDgYDVQQIEwdjb2xvbWJvMRAwDgYDVQQHEwd3ZXN0ZXJuMQ0wCwYD\n" +
                    "VQQKEwR3c28yMQ0wCwYDVQQLEwR3c28yMRYwFAYDVQQDEw0xOTIuMTY4LjEuMTA4MIIBIjANBgkq\n" +
                    "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7ew8Vkyjum0EWoRaHTC7VceYFcCdyJ4hNy+kVro9nRCO\n" +
                    "UHcbm1OCjG8AqOlMF2Scg4aAAqlRcmsGJ5lRQKGNBk117V/IPno1k8NeBg7PQneWy4KS17hCtNi+\n" +
                    "nCooRFAbhnYdX1CGDf7pyTqEBT4y85WTtNNO+LkkLdb0GxR+3E6PEfR+2D8m/kt/DRKmSTIhw/t/\n" +
                    "PDHN/jgyhi/SwuObBkb48LWiGnNVe5TvEa0cUXtHlqYuT8cYuch38+cgPNYxcPYeSt9KH7aB0yNj\n" +
                    "gCZDiBtlvWoJCUEeEWNHba2pB8eYSOdym/JEIkKs3HtKnBkp+enE4qB0wNqnkKltDz+2+QIDAQAB\n" +
                    "oyEwHzAdBgNVHQ4EFgQURHq9dv2saV12L23+evy/zzBmuicwDQYJKoZIhvcNAQELBQADggEBAFCd\n" +
                    "HYeytZRcnS7QonRDO0a15x9lDT/wDpqxDaKTC3YMG8/QxN04t/cDrHt0m1LkKgLCt+Zz4PvN1UX1\n" +
                    "Psck+pNaXVAn7VbvjXp//davZXc7j4cS4bohEt0EuwBRLLhNgA0QmzMld1LteqLD1+15Vmlv2KQ2\n" +
                    "Up4gqIbzlWYbyL0JW8UKmMAs7x3kvsgnwdf85B92KyxcgG5iI4T8VY1rMuH3LMPOU12jF9S2x9xg\n" +
                    "2/Qh4Zy17IIUltCQEOIpVsS07wf/sU16c4v6xZQogxKvBLMTLKW2NzLIxA2/3xV5v1loyY/vVQ85\n" +
                    "Q877zXfwTUGO5ZN3QCGncCpCEfWbnj6pxAM=";

    @BeforeClass
    public static void init() {
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore", CERT_PATH.getPath());
        certificateMgtUtils = new CertificateMgtUtils();
    }

    @Test
    public void testAddCertificateToTrustStore() {
        ResponseCode result = certificateMgtUtils.addCertificateToTrustStore(BASE64_ENCODED_CERT_STRING, ALIAS);
        Assert.assertEquals(result, ResponseCode.SUCCESS);
    }

    @Test
    public void testAddExistingCertificate() {
        ResponseCode result = certificateMgtUtils.addCertificateToTrustStore(BASE64_ENCODED_CERT_STRING, ALIAS);
        Assert.assertEquals(result, ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE);
    }

    @Test
    public void testDeleteCertificateFromTrustStore() {
        certificateMgtUtils.addCertificateToTrustStore(ALIAS, BASE64_ENCODED_CERT_STRING);
        ResponseCode responseCode = certificateMgtUtils.removeCertificateFromTrustStore(ALIAS);
        Assert.assertEquals(responseCode, ResponseCode.SUCCESS);
    }

    @Test
    public void testDeleteNonExistingCertificate() {
        ResponseCode responseCode = certificateMgtUtils.removeCertificateFromTrustStore(ALIAS_NOT_EXIST);
        Assert.assertEquals(responseCode, ResponseCode.CERTIFICATE_NOT_FOUND);
    }

    @Test
    public void testAddCertificateWithCertificateException() {
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(ALIAS, BASE64_ENCODED_ERROR_CERT);
        Assert.assertEquals(responseCode, ResponseCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testAddCertificateWithFileNotFoundException() throws NoSuchFieldException, IllegalAccessException {
        Field field = CertificateMgtUtils.class.getDeclaredField(TRUST_STORE_FIELD);
        field.setAccessible(true);
        field.set(certificateMgtUtils, INVALID_TRUST_STORE_FILE);
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(ALIAS, BASE64_ENCODED_ERROR_CERT);
        field.set(certificateMgtUtils, CERT_PATH.getPath());
        Assert.assertEquals(responseCode, ResponseCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testAddExpiredCertificate() {
        ResponseCode responseCode = certificateMgtUtils.addCertificateToTrustStore(EXPIRED_CERTIFICATE,
                ALIAS_EXPIRED);
        Assert.assertEquals(responseCode, ResponseCode.CERTIFICATE_EXPIRED);
    }
}
