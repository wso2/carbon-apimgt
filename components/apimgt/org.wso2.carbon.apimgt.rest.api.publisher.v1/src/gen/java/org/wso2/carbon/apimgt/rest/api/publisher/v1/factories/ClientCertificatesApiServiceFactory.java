package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ClientCertificatesApiServiceImpl;

public class ClientCertificatesApiServiceFactory {

   private final static ClientCertificatesApiService service = new ClientCertificatesApiServiceImpl();

   public static ClientCertificatesApiService getClientCertificatesApi()
   {
      return service;
   }
}
