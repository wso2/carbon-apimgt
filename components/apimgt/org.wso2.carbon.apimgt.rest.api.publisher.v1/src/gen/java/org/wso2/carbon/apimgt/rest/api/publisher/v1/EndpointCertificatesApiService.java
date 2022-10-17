package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import java.io.InputStream;
import javax.ws.rs.core.Response;

public interface EndpointCertificatesApiService {
      public Response addEndpointCertificate(InputStream certificateInputStream, Attachment certificateDetail, String alias, String endpoint, MessageContext messageContext) throws APIManagementException;
      public Response deleteEndpointCertificateByAlias(String alias, MessageContext messageContext) throws APIManagementException;
      public Response getCertificateUsageByAlias(String alias, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response getEndpointCertificateByAlias(String alias, MessageContext messageContext) throws APIManagementException;
      public Response getEndpointCertificateContentByAlias(String alias, MessageContext messageContext) throws APIManagementException;
      public Response getEndpointCertificates(Integer limit, Integer offset, String alias, String endpoint, MessageContext messageContext) throws APIManagementException;
      public Response updateEndpointCertificateByAlias(String alias, InputStream certificateInputStream, Attachment certificateDetail, MessageContext messageContext) throws APIManagementException;
}
