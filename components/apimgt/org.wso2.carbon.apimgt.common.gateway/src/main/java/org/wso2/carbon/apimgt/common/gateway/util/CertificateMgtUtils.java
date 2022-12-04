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
