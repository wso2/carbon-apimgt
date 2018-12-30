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
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.ByteArrayInputStream;
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

    @Before
    public void init() {

        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStore", CERT_PATH.getPath());
        certificateMgtUtils = CertificateMgtUtils.getInstance();
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

    @Test
    public void testGetCertificateInformation() throws CertificateManagementException {

        certificateMgtUtils.addCertificateToTrustStore(BASE64_ENCODED_CERT_STRING,
                ALIAS);
        CertificateInformationDTO certificateInformationDTO = certificateMgtUtils.getCertificateInformation(ALIAS);
        Assert.assertNotNull(certificateInformationDTO);
    }

    @Test
    public void testUpdateCertificate() throws CertificateManagementException {

        ResponseCode responseCode = certificateMgtUtils.updateCertificate(BASE64_ENCODED_CERT_STRING, ALIAS);
        Assert.assertEquals(ResponseCode.SUCCESS, responseCode);
    }

    @Test
    public void testUpdateCertificateWithCertificateException() {

        try {
            ResponseCode responseCode = certificateMgtUtils.updateCertificate(BASE64_ENCODED_ERROR_CERT, ALIAS);
        } catch (CertificateManagementException e) {
            Assert.assertEquals(CertificateManagementException.class, e.getClass());
        }
    }

    @Test
    public void testUpdateCertificateWithExpiredCertificate() throws CertificateManagementException {

        ResponseCode responseCode = certificateMgtUtils.updateCertificate(EXPIRED_CERTIFICATE, ALIAS);
        Assert.assertEquals(ResponseCode.CERTIFICATE_EXPIRED, responseCode);
    }

    @Test
    public void testUpdateCertificateWithCertificateNotFound() throws CertificateManagementException {

        ResponseCode responseCode = certificateMgtUtils.updateCertificate(BASE64_ENCODED_CERT_STRING, ALIAS_NOT_EXIST);
        Assert.assertEquals(ResponseCode.CERTIFICATE_NOT_FOUND, responseCode);
    }

    @Test
    public void testUpdateCertificateWithEmptyCertificate() throws CertificateManagementException {

        ResponseCode responseCode = certificateMgtUtils.updateCertificate("", ALIAS);
        Assert.assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, responseCode);
    }

    @Test
    public void testGetCertificateContent() throws CertificateManagementException {

        certificateMgtUtils.addCertificateToTrustStore(BASE64_ENCODED_CERT_STRING, ALIAS);
        Object certificateStream = certificateMgtUtils.getCertificateContent(ALIAS);
        Assert.assertNotNull(certificateStream);
        Assert.assertEquals(certificateStream.getClass().getName(), ByteArrayInputStream.class.getName());
    }

    /**
     * This method tests the validateCertificate method's behaviour, during different conditions
     */
    @Test
    public void testValidateCertificate() {
        certificateMgtUtils.addCertificateToTrustStore(BASE64_ENCODED_CERT_STRING,
                ALIAS + "_" + MultitenantConstants.SUPER_TENANT_ID);
        ResponseCode responseCode = certificateMgtUtils
                .validateCertificate(ALIAS, MultitenantConstants.SUPER_TENANT_ID, BASE64_ENCODED_CERT_STRING);
        Assert.assertEquals("Validation succeeded, even though certificate with same alias exist already",
                ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE, responseCode);
        certificateMgtUtils.removeCertificateFromTrustStore(ALIAS + "_" + MultitenantConstants.SUPER_TENANT_ID);
        responseCode = certificateMgtUtils
                .validateCertificate(ALIAS, MultitenantConstants.SUPER_TENANT_ID, EXPIRED_CERTIFICATE);
        Assert.assertEquals("Validation succeeded for an expired certificate", ResponseCode.CERTIFICATE_EXPIRED,
                responseCode);
        responseCode = certificateMgtUtils
                .validateCertificate(ALIAS, MultitenantConstants.SUPER_TENANT_ID, BASE64_ENCODED_CERT_STRING);
        Assert.assertEquals("Validation failed for a valid certificate", ResponseCode.SUCCESS, responseCode);
    }

    /**
     * Tests the behaviour of getCertificateInfo method.
     */
    @Test
    public void testGetCertificateInfo() {
        Assert.assertNotNull("A valid certificate info retrieval failed",
                certificateMgtUtils.getCertificateInfo(BASE64_ENCODED_CERT_STRING));
    }
}
