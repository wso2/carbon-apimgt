package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ClientCertificatesApiService {
      public Response clientCertificatesAliasContentGet(String alias, MessageContext messageContext) throws APIManagementException;
      public Response clientCertificatesAliasDelete(String alias, MessageContext messageContext) throws APIManagementException;
      public Response clientCertificatesAliasGet(String alias, MessageContext messageContext) throws APIManagementException;
      public Response clientCertificatesAliasPut(String alias, InputStream certificateInputStream, Attachment certificateDetail, String tier, MessageContext messageContext) throws APIManagementException;
      public Response clientCertificatesGet(Integer limit, Integer offset, String alias, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response clientCertificatesPost(InputStream certificateInputStream, Attachment certificateDetail, String alias, String apiId, String tier, MessageContext messageContext) throws APIManagementException;
}
