package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface LabelsApiService {
      public Response createLabel(LabelDTO labelDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteLabel(String labelId, MessageContext messageContext) throws APIManagementException;
      public Response getAllLabels(MessageContext messageContext) throws APIManagementException;
      public Response getLabelUsage(String labelId, MessageContext messageContext) throws APIManagementException;
      public Response updateLabel(String labelId, LabelDTO labelDTO, MessageContext messageContext) throws APIManagementException;
}
