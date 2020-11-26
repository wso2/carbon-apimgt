package org.wso2.carbon.apimgt.rest.api.service.catalog;

import org.wso2.carbon.apimgt.rest.api.service.catalog.*;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.SettingsDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SettingsApiService {
      public Response getSettings(MessageContext messageContext) throws APIManagementException;
}
