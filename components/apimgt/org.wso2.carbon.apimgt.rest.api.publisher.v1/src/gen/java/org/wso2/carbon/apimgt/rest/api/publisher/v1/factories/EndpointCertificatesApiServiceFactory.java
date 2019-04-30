package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.EndpointCertificatesApiServiceImpl;

public class EndpointCertificatesApiServiceFactory {

   private final static EndpointCertificatesApiService service = new EndpointCertificatesApiServiceImpl();

   public static EndpointCertificatesApiService getEndpointCertificatesApi()
   {
      return service;
   }
}
