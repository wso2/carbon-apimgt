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
import java.util.Set;

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
    private static final String TRUST_STORE_FIELD = "trustStoreLocation";
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

    // Certificate with wildcard SAN *.example.com and exact SANs api1.hello.com, api2.hello.com
    // CN: simple.example.com (should be ignored because SANs are present)
    private static final String CERT_WITH_DNS_SANS =
            "MIIDdTCCAl2gAwIBAgIUQmUApCllap+dKaaXsTk7dR0gDsQwDQYJKoZIhvcNAQELBQAwLDEbMBkG" +
            "A1UEAwwSc2ltcGxlLmV4YW1wbGUuY29tMQ0wCwYDVQQKDARUZXN0MB4XDTI2MDYwNTA1NTcxMloX" +
            "DTM2MDYwMjA1NTcxMlowLDEbMBkGA1UEAwwSc2ltcGxlLmV4YW1wbGUuY29tMQ0wCwYDVQQKDARU" +
            "ZXN0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8ur/uoiIoZ8yW4D0FZOAwaPmH8sk" +
            "MZje4vW3ILxkLtgD8PjjiA4XQihrYuxrMBBS5Jlna8EmEjnP4ygg1mZ2AfHFQCMwJ4RlInxKEfr3" +
            "ElFLLpPtBzFYjrlUzA7ZRxWrX9upeDOrsTytIxAvpdPbWrUsFKcYL3tq1GB4hE6GICs9VCkBTF58" +
            "9loYZ3bH6T6epijAP0vRrSMIMD1CZrrvFilV0W6IohY56CF84mQWn3JqZbj2/FqkpMJ/VO0bJ6fK" +
            "sup6nn0GqY7DNG6MglrvS+pkturYAXA26f5uh34YEuRnAzAdVLFtogJYrUWgk4yVB+7b0H4FEGTF" +
            "DKQlBvErJQIDAQABo4GOMIGLMB0GA1UdDgQWBBTAXsL6ojyS6e42f69OIXTOzKt0ijAfBgNVHSME" +
            "GDAWgBTAXsL6ojyS6e42f69OIXTOzKt0ijAPBgNVHRMBAf8EBTADAQH/MDgGA1UdEQQxMC+CDSou" +
            "ZXhhbXBsZS5jb22CDmFwaTEuaGVsbG8uY29tgg5hcGkyLmhlbGxvLmNvbTANBgkqhkiG9w0BAQsF" +
            "AAOCAQEATjPCm++GvSkY5IoBeZqAN3pIpjtZqTGj+tAHy2X+veRpUda9PEajV1kKpfB34ZoOcHrS" +
            "Gb5y5VYojaAZotOZp+2ilmLqujfT2Q8+XQ0EHjpEPzuDq9koUeSJPY0w8m/TToldd1MtdDWXk0V9" +
            "vdV41Gi/+VyOajdOtzdGKbEciJsq5sd5gyFRBxjo5gSPqqJi+L09Ig3g+c6faUJ/JI5e2Fbv53cc" +
            "rBTc2XgsY2eKQLbIcgiEu/LjXyZ1mUvFv7LyzXRnWj3+w+ek3EPiRggQcbBPOHuPGaNrTzkwLIll" +
            "chF2eAlffVjpWA3NzL/Q5tRXTFitbBozfHGl7nzN8QxtKQ==";

    // Certificate with no SANs, CN=backend.example.com
    private static final String CERT_CN_ONLY =
            "MIIDOzCCAiOgAwIBAgIUFB8gC/GSC04yMpVppR2GSbQuEd8wDQYJKoZIhvcNAQELBQAwLTEcMBoG" +
            "A1UEAwwTYmFja2VuZC5leGFtcGxlLmNvbTENMAsGA1UECgwEVGVzdDAeFw0yNjA2MDUwNTU3MjBa" +
            "Fw0zNjA2MDIwNTU3MjBaMC0xHDAaBgNVBAMME2JhY2tlbmQuZXhhbXBsZS5jb20xDTALBgNVBAoM" +
            "BFRlc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCjfoX3PHePUrDlEQ78szKEhw8l" +
            "8PHk1JgNPm/u1CtNz+JT/8zrfBw/+xZWCeNZ2fxgVN1wnl0h4LJogmSsQwYQe7naURxhomwQ+6Y" +
            "rOXpCUSUYVvAI0ZsKUzDWFmssvo7QQ3lGpCC/nvjGtUBoE9Gjwbv45SYbzCWmCl1yRs4RxUMd5UC" +
            "WF9GmrhiWnfAYpu825NEiT/yVlQBuHu/KoL7054WewQjsAX86HdNFS0r35wXp/qdk5pqauD0Jnff" +
            "f8PZf/asrBoMA3Lx0bIlHX2KfgAN+PyXzDksK/y2CqiDdA0h5x/+FBSuh9d0rlK/v3nfPzF7VYfr" +
            "xbLfkuRSf+ZY1AgMBAAGjUzBRMB0GA1UdDgQWBBTDJjRTndFSqtc0ydL5tdDexBqe4zAfBgNVHSME" +
            "GDAWgBTDJjRTndFSqtc0ydL5tdDexBqe4zAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUA" +
            "A4IBAQCJAX2GciKCBtq8turwKXPxFuzlwwq44aqXMzQ2SxbQ/Z9hoLlpyGgQXeCQsL1KT4XWAvqf" +
            "DrDuN+HyX6lnLfjnx/pU5z62fOg54UbpX++8Qz0/buYbMzh/BlvqW8B1HQ2OhUVaj1FMTCF984Rs" +
            "AQgu1iP2xtatHbdVzjt7KmLot9CQzxTIIo0z75MqcqM+0az8VW+e2TDAEtzfSay3rh5X+YBJipk5" +
            "V2+DWlF7yc8U3QSF/1rRmbefnIdfzaAoFkH6G5u8TUjYE/Tt3cSQiylPlhUiIwPgG1XyaVu9i2YM" +
            "gv+Kp/g3ROxKCpqMv98hRw6MhzITCI5WUsd4dSfv5atg";

    // Certificate with IP SAN 10.0.0.1 and DNS SAN api.internal.com
    private static final String CERT_WITH_IP_SAN =
            "MIIDSDCCAjCgAwIBAgIUKXzufasLHJEeGvHARI9PRFf4gTcwDQYJKoZIhvcNAQELBQAwIjERMA8G" +
            "A1UEAwwIMTAuMC4wLjExDTALBgNVBAoMBFRlc3QwHhcNMjYwNjA1MDU1NzIwWhcNMzYwNjAyMDU1" +
            "NzIwWjAiMREwDwYDVQQDDAgxMC4wLjAuMTENMAsGA1UECgwEVGVzdDCCASIwDQYJKoZIhvcNAQEB" +
            "BQADggEPADCCAQoCggEBAMuZ37+mcpa7r1223kp8wVXswdIJ0sjYA+LHPeTfHeytiFKvz1m0a2UD" +
            "AUyhbVM2PVH0Z9elolCG1FjxUqKw/FVcYT8c4OCPqe/TaFbTc4dNrIcq7a4IfuFpQmp1pTL9pVzA" +
            "oUfGkO2SbJD4xHgD35QL2Viq9TS6mAgZQjReLEcdrYvfaZLs1T6q73zeA5zJaqR7xSRevtJe/bXc" +
            "OLeYVl7X+fzpI7QtjnAb59Aykbqbm6eYfLuXoTi1XE9NE3aL8nRtOgZX8MOblTYx79s7ubCyD5qL" +
            "2ktBubcTa1WF4NE30aRw5tyALam/t5bea6Mu/oVIUtkzNfISHdoOz5PQPdsCAwEAAaN2MHQwHQYD" +
            "VR0OBBYEFGrE3X8QzQRS+f8jg5MwMQyAF3tiMB8GA1UdIwQYMBaAFGrE3X8QzQRS+f8jg5MwMQyA" +
            "F3tiMA8GA1UdEwEB/wQFMAMBAf8wIQYDVR0RBBowGIcECgAAAYIQYXBpLmludGVybmFsLmNvbTAN" +
            "BgkqhkiG9w0BAQsFAAOCAQEAnvXK1z3IxCBKyaPsmYrEGKeZq9ajyrC1sJZwa8DcbX0KVmbMDwLs" +
            "kLdXWlUyN/mzUiz62y0ow+ocK+3GLM5cfz3g3Fihm4RyllHFleMo70VfYolKAfDT2DopRjExWAnR" +
            "0C35VkcNO+Waj4Vwp7lN6DhEzgeMvkmRIDpygpVfl70AECIj1Py8n2PbXP+QbxOFPcmm0smY4MyC" +
            "PlRtXJCPz7D3GmLexVfrFcx+ofSvBx7OJZIwGzB7Mb4f9AP9YjVtV112T5RkGANn8+VCHhj9WSqy" +
            "hl20UJWJVSqrPqUYD45oSAq4bVC3lSNzGkaLc5dRRMTmEQ9SkeNwve0ZRGPHVA==";

    @Test
    public void testGetEndpointSearchTermsWithDNSSANs() {
        Set<String> terms = CertificateMgtUtils.getEndpointSearchTermsFromCertificate(CERT_WITH_DNS_SANS);
        Assert.assertEquals("Wildcard SAN *.example.com should contribute one term stripped of *", 3, terms.size());
        Assert.assertTrue("Wildcard SAN *.example.com should produce .example.com", terms.contains(".example.com"));
        Assert.assertTrue("Exact SAN api1.hello.com should be included as-is", terms.contains("api1.hello.com"));
        Assert.assertTrue("Exact SAN api2.hello.com should be included as-is", terms.contains("api2.hello.com"));
        Assert.assertFalse("Original wildcard form *.example.com must not appear", terms.contains("*.example.com"));
        Assert.assertFalse("CN should be ignored when SANs are present", terms.contains("simple.example.com"));
    }

    @Test
    public void testGetEndpointSearchTermsWithCNFallback() {
        Set<String> terms = CertificateMgtUtils.getEndpointSearchTermsFromCertificate(CERT_CN_ONLY);
        Assert.assertEquals("CN should be the only term when no SANs are present", 1, terms.size());
        Assert.assertTrue("CN backend.example.com should be used as fallback", terms.contains("backend.example.com"));
    }

    @Test
    public void testGetEndpointSearchTermsWithIPSAN() {
        Set<String> terms = CertificateMgtUtils.getEndpointSearchTermsFromCertificate(CERT_WITH_IP_SAN);
        Assert.assertEquals("IP SAN and DNS SAN should each contribute one term", 2, terms.size());
        Assert.assertTrue("IP SAN 10.0.0.1 should be included", terms.contains("10.0.0.1"));
        Assert.assertTrue("DNS SAN api.internal.com should be included", terms.contains("api.internal.com"));
    }

    @Test
    public void testGetEndpointSearchTermsWithInvalidCert() {
        Set<String> terms = CertificateMgtUtils.getEndpointSearchTermsFromCertificate("not-a-valid-cert");
        Assert.assertTrue("Invalid cert should yield empty set without throwing", terms.isEmpty());
    }

    @Test
    public void testGetEndpointSearchTermsWithNullCert() {
        Set<String> terms = CertificateMgtUtils.getEndpointSearchTermsFromCertificate(null);
        Assert.assertTrue("Null cert should yield empty set without throwing", terms.isEmpty());
    }
}
