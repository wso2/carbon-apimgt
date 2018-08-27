package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.CertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.CertificatesApiServiceImpl;

public class CertificatesApiServiceFactory {

   private final static CertificatesApiService service = new CertificatesApiServiceImpl();

   public static CertificatesApiService getCertificatesApi()
   {
      return service;
   }
}
