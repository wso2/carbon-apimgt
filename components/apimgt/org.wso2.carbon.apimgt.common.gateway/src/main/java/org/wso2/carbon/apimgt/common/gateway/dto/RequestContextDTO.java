/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.common.gateway.dto;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

/**
 * Representation of Request Information.
 */
public class RequestContextDTO {
    private static final Log log = LogFactory.getLog(RequestContextDTO.class);

    // request message information
    private MsgInfoDTO msgInfo;
    // invoked API request information
    private APIRequestInfoDTO apiRequestInfo;
    // client certificate from transport level
    private Certificate[] clientCerts;
    // custom property map used to populate customProperty key template value
    private Map<String, Object> customProperty;

    public MsgInfoDTO getMsgInfo() {

        return msgInfo;
    }

    public void setMsgInfo(MsgInfoDTO msgInfo) {

        this.msgInfo = msgInfo;
    }

    public APIRequestInfoDTO getApiRequestInfo() {

        return apiRequestInfo;
    }

    public void setApiRequestInfo(APIRequestInfoDTO apiRequestInfo) {

        this.apiRequestInfo = apiRequestInfo;
    }

    @Deprecated
    public X509Certificate[] getClientCerts() {
        X509Certificate[] x509Certificates = new X509Certificate[clientCerts.length];
        for (int i = 0; i < clientCerts.length; i++) {
            try {
                x509Certificates[i] = X509Certificate.getInstance(clientCerts[i].getEncoded());
            } catch (CertificateException | CertificateEncodingException e) {
                log.error("Error while converting client certificate", e);
            }
        }
        return (X509Certificate[]) SerializationUtils.clone(x509Certificates);
    }

    @Deprecated
    public void setClientCerts(X509Certificate[] x509ClientCerts) {
        Certificate[] certificates = new Certificate[x509ClientCerts.length];
        for (int i = 0; i < x509ClientCerts.length; i++) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(x509ClientCerts[i].getEncoded());
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certificates[i] = cf.generateCertificate(inputStream);
            } catch (CertificateException | java.security.cert.CertificateException e) {
                log.error("Error while converting client certificate", e);
            }
        }
        this.clientCerts = (Certificate[]) SerializationUtils.clone(certificates);
    }

    public Certificate[] getClientCertsLatest() {
        return (Certificate[]) SerializationUtils.clone(clientCerts);
    }

    public void setClientCertsLatest(Certificate[] clientCerts) {
        this.clientCerts = (Certificate[]) SerializationUtils.clone(clientCerts);
    }

    public Map<String, Object> getCustomProperty() {

        return customProperty;
    }

    public void setCustomProperty(Map<String, Object> customProperty) {

        this.customProperty = customProperty;
    }
}

