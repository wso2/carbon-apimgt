/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.common.gateway.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * CertificateMgtUtils contains the utility methods related to SSL certificates.
 */
public class CertificateMgtUtils {

    private static final Log log = LogFactory.getLog(CertificateMgtUtils.class);
    /**
     * Convert javax.security.cert.X509Certificate to java.security.cert.X509Certificate
     *
     * @param cert the certificate to be converted
     * @return java.security.cert.X509Certificate type certificate
     */
    public static Optional<X509Certificate> convert(Certificate cert) {

        if (cert != null) {
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cert.getEncoded())) {

                java.security.cert.CertificateFactory certificateFactory
                        = java.security.cert.CertificateFactory.getInstance("X.509");
                return Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                        byteArrayInputStream));
            } catch (java.security.cert.CertificateException e) {
                log.error("Error while generating the certificate", e);
            } catch (IOException e) {
                log.error("Error while retrieving the encoded certificate", e);
            }
        }
        return Optional.ofNullable(null);
    }
}
