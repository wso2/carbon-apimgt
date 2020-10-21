package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MediationListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface PoliciesApiService {
      public Response policiesMediationGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response policiesMediationMediationPolicyIdDelete(String mediationPolicyId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response policiesMediationMediationPolicyIdGet(String mediationPolicyId, String accept, String ifNoneMatch, String ifModifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response policiesMediationMediationPolicyIdPut(String mediationPolicyId, MediationDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
      public Response policiesMediationPost(MediationDTO body, String contentType, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) throws APIManagementException;
}
