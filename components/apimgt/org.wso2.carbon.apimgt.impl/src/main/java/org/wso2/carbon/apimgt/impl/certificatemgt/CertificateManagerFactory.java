package org.wso2.carbon.apimgt.impl.certificatemgt;

public class CertificateManagerFactory {
    private static CertificateManager certificateManagerInstance = new CertificateManagerImpl();

    public static CertificateManager getCertificateManagerInstance() {
        return certificateManagerInstance;
    }
}
