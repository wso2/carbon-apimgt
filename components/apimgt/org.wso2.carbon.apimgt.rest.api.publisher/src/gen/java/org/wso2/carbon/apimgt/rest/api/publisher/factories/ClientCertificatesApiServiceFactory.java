package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ClientCertificatesApiServiceImpl;

public class ClientCertificatesApiServiceFactory {

   private final static ClientCertificatesApiService service = new ClientCertificatesApiServiceImpl();

   public static ClientCertificatesApiService getClientCertificatesApi()
   {
      return service;
   }
}
